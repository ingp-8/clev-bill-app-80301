package com.clevstack.clevbill.service;

import com.clevstack.clevbill.dto.ReturnItemRequest;
import com.clevstack.clevbill.dto.ReturnItemResponse;
import com.clevstack.clevbill.dto.ReturnRequest;
import com.clevstack.clevbill.dto.ReturnResponse;
import com.clevstack.clevbill.exception.ResourceNotFoundException;
import com.clevstack.clevbill.model.InventoryChangeReason;
import com.clevstack.clevbill.model.ReturnItem;
import com.clevstack.clevbill.model.Sale;
import com.clevstack.clevbill.model.SaleItem;
import com.clevstack.clevbill.model.SaleReturn;
import com.clevstack.clevbill.model.User;
import com.clevstack.clevbill.repository.ReturnItemRepository;
import com.clevstack.clevbill.repository.SaleItemRepository;
import com.clevstack.clevbill.repository.SaleReturnRepository;
import com.clevstack.clevbill.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReturnService {

    private final SaleReturnRepository saleReturnRepository;
    private final SaleService saleService;
    private final SaleItemRepository saleItemRepository;
    private final ReturnItemRepository returnItemRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;

    public ReturnService(
            SaleReturnRepository saleReturnRepository,
            SaleService saleService,
            SaleItemRepository saleItemRepository,
            ReturnItemRepository returnItemRepository,
            UserRepository userRepository,
            InventoryService inventoryService) {
        this.saleReturnRepository = saleReturnRepository;
        this.saleService = saleService;
        this.saleItemRepository = saleItemRepository;
        this.returnItemRepository = returnItemRepository;
        this.userRepository = userRepository;
        this.inventoryService = inventoryService;
    }

    public ReturnResponse create(Long saleId, ReturnRequest request, String username) {
        Sale sale = saleService.findEntity(saleId);
        User createdBy = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        SaleReturn saleReturn = new SaleReturn();
        saleReturn.setSale(sale);
        saleReturn.setReason(request.reason());
        saleReturn.setCreatedBy(createdBy);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ReturnItemRequest line : request.items()) {
            ReturnItem returnItem = buildReturnItem(sale, line);
            saleReturn.addItem(returnItem);
            totalAmount = totalAmount.add(returnItem.getAmount());
        }
        saleReturn.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));

        SaleReturn savedReturn = saleReturnRepository.save(saleReturn);

        for (ReturnItem returnItem : savedReturn.getItems()) {
            inventoryService.applyChange(
                    returnItem.getSaleItem().getItem().getId(),
                    returnItem.getQuantity(),
                    InventoryChangeReason.RETURN,
                    savedReturn.getId(),
                    null,
                    createdBy);
        }

        return toResponse(savedReturn);
    }

    @Transactional(readOnly = true)
    public List<ReturnResponse> list(Long saleId) {
        saleService.findEntity(saleId);
        return saleReturnRepository.findBySaleId(saleId).stream().map(this::toResponse).toList();
    }

    private ReturnItem buildReturnItem(Sale sale, ReturnItemRequest line) {
        SaleItem saleItem = saleItemRepository
                .findById(line.saleItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Sale item not found: " + line.saleItemId()));

        if (!saleItem.getSale().getId().equals(sale.getId())) {
            throw new ResourceNotFoundException(
                    "Sale item %d does not belong to sale %d".formatted(line.saleItemId(), sale.getId()));
        }

        BigDecimal alreadyReturned = returnItemRepository.sumReturnedQuantity(saleItem.getId());
        BigDecimal remaining = saleItem.getQuantity().subtract(alreadyReturned);
        if (line.quantity().compareTo(remaining) > 0) {
            throw new IllegalArgumentException(
                    "Return quantity %s exceeds remaining returnable quantity %s for sale item %d"
                            .formatted(line.quantity(), remaining, saleItem.getId()));
        }

        BigDecimal unitTotalPrice = saleItem.getLineTotal().divide(saleItem.getQuantity(), 6, RoundingMode.HALF_UP);
        BigDecimal amount = unitTotalPrice.multiply(line.quantity()).setScale(2, RoundingMode.HALF_UP);

        ReturnItem returnItem = new ReturnItem();
        returnItem.setSaleItem(saleItem);
        returnItem.setQuantity(line.quantity());
        returnItem.setAmount(amount);
        return returnItem;
    }

    private ReturnResponse toResponse(SaleReturn saleReturn) {
        List<ReturnItemResponse> items = saleReturn.getItems().stream()
                .map(item -> new ReturnItemResponse(
                        item.getId(),
                        item.getSaleItem().getId(),
                        item.getSaleItem().getNameSnapshot(),
                        item.getQuantity(),
                        item.getAmount()))
                .toList();

        return new ReturnResponse(
                saleReturn.getId(),
                saleReturn.getSale().getId(),
                saleReturn.getReason(),
                saleReturn.getTotalAmount(),
                items,
                saleReturn.getCreatedAt());
    }
}
