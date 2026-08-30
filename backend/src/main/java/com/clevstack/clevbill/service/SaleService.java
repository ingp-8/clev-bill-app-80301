package com.clevstack.clevbill.service;

import com.clevstack.clevbill.dto.CheckoutItemRequest;
import com.clevstack.clevbill.dto.CheckoutRequest;
import com.clevstack.clevbill.dto.LineTax;
import com.clevstack.clevbill.dto.MasterRefResponse;
import com.clevstack.clevbill.dto.PaymentRequest;
import com.clevstack.clevbill.dto.PaymentResponse;
import com.clevstack.clevbill.dto.SaleItemResponse;
import com.clevstack.clevbill.dto.SaleResponse;
import com.clevstack.clevbill.exception.ResourceNotFoundException;
import com.clevstack.clevbill.model.Customer;
import com.clevstack.clevbill.model.InventoryChangeReason;
import com.clevstack.clevbill.model.Item;
import com.clevstack.clevbill.model.Payment;
import com.clevstack.clevbill.model.PosTerminal;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.model.Sale;
import com.clevstack.clevbill.model.SaleItem;
import com.clevstack.clevbill.model.User;
import com.clevstack.clevbill.repository.CustomerRepository;
import com.clevstack.clevbill.repository.ItemRepository;
import com.clevstack.clevbill.repository.SaleRepository;
import com.clevstack.clevbill.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SaleService {

    private final SaleRepository saleRepository;
    private final ItemRepository itemRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PropertyService propertyService;
    private final PosTerminalService posTerminalService;
    private final BillNumberService billNumberService;
    private final TaxCalculationService taxCalculationService;
    private final EInvoiceService eInvoiceService;
    private final InventoryService inventoryService;
    private final ApplicationEventPublisher eventPublisher;

    public SaleService(
            SaleRepository saleRepository,
            ItemRepository itemRepository,
            CustomerRepository customerRepository,
            UserRepository userRepository,
            PropertyService propertyService,
            PosTerminalService posTerminalService,
            BillNumberService billNumberService,
            TaxCalculationService taxCalculationService,
            EInvoiceService eInvoiceService,
            InventoryService inventoryService,
            ApplicationEventPublisher eventPublisher) {
        this.saleRepository = saleRepository;
        this.itemRepository = itemRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.propertyService = propertyService;
        this.posTerminalService = posTerminalService;
        this.billNumberService = billNumberService;
        this.taxCalculationService = taxCalculationService;
        this.inventoryService = inventoryService;
        this.eInvoiceService = eInvoiceService;
        this.eventPublisher = eventPublisher;
    }

    public SaleResponse checkout(CheckoutRequest request, String cashierUsername) {
        User cashier = userRepository
                .findByUsername(cashierUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + cashierUsername));

        Property property = propertyService.findEntity(request.propertyId());
        PosTerminal posTerminal = posTerminalService.findEntity(request.posId());
        if (!posTerminal.getProperty().getId().equals(property.getId())) {
            throw new IllegalArgumentException(
                    "POS %d does not belong to property %d".formatted(request.posId(), request.propertyId()));
        }

        Customer customer = request.customerId() == null
                ? null
                : customerRepository
                        .findById(request.customerId())
                        .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + request.customerId()));

        Sale sale = new Sale();
        sale.setCustomer(customer);
        sale.setCashier(cashier);
        sale.setProperty(property);
        sale.setClient(property.getClient());
        sale.setPosTerminal(posTerminal);

        String storeGstin = property.getGstin();
        String customerGstin = customer == null ? null : customer.getGstin();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        for (CheckoutItemRequest line : request.items()) {
            SaleItem saleItem = buildSaleItem(property, line, storeGstin, customerGstin);
            sale.addItem(saleItem);
            subtotal = subtotal.add(saleItem.getLineSubtotal());
            tax = tax.add(saleItem.getCgstAmount()).add(saleItem.getSgstAmount()).add(saleItem.getIgstAmount());
        }
        BigDecimal total = money(subtotal.add(tax));

        BigDecimal paidTotal = request.payments().stream()
                .map(PaymentRequest::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (paidTotal.compareTo(total) != 0) {
            throw new IllegalArgumentException(
                    "Payment total (%s) does not match sale total (%s)".formatted(paidTotal, total));
        }

        sale.setSubtotalAmount(money(subtotal));
        sale.setTaxAmount(money(tax));
        sale.setTotalAmount(total);
        sale.setBillNumber(billNumberService.nextBillNumber(property));

        for (PaymentRequest paymentRequest : request.payments()) {
            Payment payment = new Payment();
            payment.setMethod(paymentRequest.method());
            payment.setAmount(paymentRequest.amount());
            sale.addPayment(payment);
        }

        Sale savedSale = saleRepository.save(sale);
        eInvoiceService.queueIfEnabled(savedSale);
        eventPublisher.publishEvent(new SaleCreatedEvent(savedSale.getId()));

        for (SaleItem saleItem : savedSale.getItems()) {
            inventoryService.applyChange(
                    saleItem.getItem().getId(),
                    saleItem.getQuantity().negate(),
                    InventoryChangeReason.SALE,
                    savedSale.getId(),
                    null,
                    cashier);
        }

        return toResponse(savedSale);
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> listByProperty(Long propertyId) {
        propertyService.findEntity(propertyId);
        return saleRepository.findByPropertyId(propertyId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SaleResponse get(Long id) {
        return toResponse(findEntity(id));
    }

    Sale findEntity(Long id) {
        return saleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sale not found: " + id));
    }

    private SaleItem buildSaleItem(Property property, CheckoutItemRequest line, String storeGstin, String customerGstin) {
        Item item = itemRepository
                .findById(line.itemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + line.itemId()));
        if (!item.getProperty().getId().equals(property.getId())) {
            throw new IllegalArgumentException("Item %d does not belong to property %d".formatted(item.getId(), property.getId()));
        }

        BigDecimal quantity = line.quantity().setScale(3, RoundingMode.HALF_UP);
        BigDecimal unitPrice = item.getSellingPrice();
        BigDecimal lineSubtotal = money(unitPrice.multiply(quantity));
        LineTax lineTax = taxCalculationService.calculate(lineSubtotal, item.getTaxRate(), storeGstin, customerGstin);

        SaleItem saleItem = new SaleItem();
        saleItem.setItem(item);
        saleItem.setSkuSnapshot(item.getSku());
        saleItem.setNameSnapshot(item.getName());
        saleItem.setQuantity(quantity);
        saleItem.setUnitPrice(unitPrice);
        saleItem.setCgstRate(item.getTaxRate().getCgstRate());
        saleItem.setSgstRate(item.getTaxRate().getSgstRate());
        saleItem.setIgstRate(item.getTaxRate().getIgstRate());
        saleItem.setCgstAmount(lineTax.cgstAmount());
        saleItem.setSgstAmount(lineTax.sgstAmount());
        saleItem.setIgstAmount(lineTax.igstAmount());
        saleItem.setLineSubtotal(lineSubtotal);
        saleItem.setLineTotal(money(lineSubtotal
                .add(lineTax.cgstAmount())
                .add(lineTax.sgstAmount())
                .add(lineTax.igstAmount())));
        return saleItem;
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private SaleResponse toResponse(Sale sale) {
        List<SaleItemResponse> items = sale.getItems().stream()
                .map(saleItem -> new SaleItemResponse(
                        saleItem.getId(),
                        new MasterRefResponse(saleItem.getItem().getId(), saleItem.getItem().getName()),
                        saleItem.getSkuSnapshot(),
                        saleItem.getNameSnapshot(),
                        saleItem.getQuantity(),
                        saleItem.getUnitPrice(),
                        saleItem.getCgstAmount(),
                        saleItem.getSgstAmount(),
                        saleItem.getIgstAmount(),
                        saleItem.getLineSubtotal(),
                        saleItem.getLineTotal()))
                .toList();

        List<PaymentResponse> payments = sale.getPayments().stream()
                .map(payment -> new PaymentResponse(
                        payment.getId(), payment.getMethod(), payment.getAmount(), payment.getCreatedAt()))
                .toList();

        MasterRefResponse customer =
                sale.getCustomer() == null ? null : new MasterRefResponse(sale.getCustomer().getId(), sale.getCustomer().getName());
        MasterRefResponse cashier = new MasterRefResponse(sale.getCashier().getId(), sale.getCashier().getFullName());

        return new SaleResponse(
                sale.getId(),
                sale.getBillNumber(),
                sale.getProperty().getId(),
                sale.getPosTerminal().getId(),
                sale.getPosTerminal().getPosName(),
                customer,
                cashier,
                items,
                payments,
                sale.getSubtotalAmount(),
                sale.getTaxAmount(),
                sale.getTotalAmount(),
                sale.getCreatedAt());
    }
}
