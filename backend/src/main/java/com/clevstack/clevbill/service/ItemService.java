package com.clevstack.clevbill.service;

import com.clevstack.clevbill.dto.ItemRequest;
import com.clevstack.clevbill.dto.ItemResponse;
import com.clevstack.clevbill.dto.MasterRefResponse;
import com.clevstack.clevbill.dto.TaxRateResponse;
import com.clevstack.clevbill.exception.ResourceNotFoundException;
import com.clevstack.clevbill.dto.HsnCodeResponse;
import com.clevstack.clevbill.model.Brand;
import com.clevstack.clevbill.model.Category;
import com.clevstack.clevbill.model.HsnCode;
import com.clevstack.clevbill.model.Item;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.model.TaxRate;
import com.clevstack.clevbill.repository.BrandRepository;
import com.clevstack.clevbill.repository.CategoryRepository;
import com.clevstack.clevbill.repository.HsnCodeRepository;
import com.clevstack.clevbill.repository.ItemRepository;
import com.clevstack.clevbill.repository.TaxRateRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final TaxRateRepository taxRateRepository;
    private final HsnCodeRepository hsnCodeRepository;
    private final PropertyService propertyService;

    public ItemService(
            ItemRepository itemRepository,
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            TaxRateRepository taxRateRepository,
            HsnCodeRepository hsnCodeRepository,
            PropertyService propertyService) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.taxRateRepository = taxRateRepository;
        this.hsnCodeRepository = hsnCodeRepository;
        this.propertyService = propertyService;
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> listByProperty(Long propertyId) {
        propertyService.findEntity(propertyId);
        return itemRepository.findByPropertyId(propertyId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ItemResponse get(Long id) {
        return toResponse(findEntity(id));
    }

    public ItemResponse create(Long propertyId, ItemRequest request) {
        Property property = propertyService.findEntity(propertyId);
        Item item = new Item();
        item.setProperty(property);
        item.setClient(property.getClient());
        applyRequest(item, property, request);
        return toResponse(itemRepository.save(item));
    }

    public ItemResponse update(Long id, ItemRequest request) {
        Item item = findEntity(id);
        applyRequest(item, item.getProperty(), request);
        return toResponse(itemRepository.save(item));
    }

    public void delete(Long id) {
        itemRepository.delete(findEntity(id));
    }

    Item findEntity(Long id) {
        return itemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Item not found: " + id));
    }

    private void applyRequest(Item item, Property property, ItemRequest request) {
        item.setSku(request.sku());
        item.setBarcode(request.barcode());
        item.setName(request.name());
        item.setCategory(request.categoryId() == null ? null : findCategory(property, request.categoryId()));
        item.setBrand(request.brandId() == null ? null : findBrand(property, request.brandId()));
        item.setTaxRate(findTaxRate(property, request.taxRateId()));
        item.setHsnCode(findHsnCode(property, request.hsnCodeId()));
        item.setUnit(request.unit());
        item.setSellingPrice(request.sellingPrice());
        item.setCostPrice(request.costPrice());
        item.setActive(request.active());
    }

    private Category findCategory(Property property, Long id) {
        Category category = categoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        requireSameProperty(property, category.getProperty().getId(), "Category");
        return category;
    }

    private Brand findBrand(Property property, Long id) {
        Brand brand = brandRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Brand not found: " + id));
        requireSameProperty(property, brand.getProperty().getId(), "Brand");
        return brand;
    }

    private TaxRate findTaxRate(Property property, Long id) {
        TaxRate taxRate = taxRateRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tax rate not found: " + id));
        requireSameProperty(property, taxRate.getProperty().getId(), "Tax rate");
        return taxRate;
    }

    private HsnCode findHsnCode(Property property, Long id) {
        HsnCode hsnCode = hsnCodeRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HSN code not found: " + id));
        requireSameProperty(property, hsnCode.getProperty().getId(), "HSN code");
        return hsnCode;
    }

    private void requireSameProperty(Property property, Long referencedPropertyId, String what) {
        if (!property.getId().equals(referencedPropertyId)) {
            throw new IllegalArgumentException(what + " does not belong to property " + property.getId());
        }
    }

    private ItemResponse toResponse(Item item) {
        MasterRefResponse category =
                item.getCategory() == null ? null : new MasterRefResponse(item.getCategory().getId(), item.getCategory().getName());
        MasterRefResponse brand =
                item.getBrand() == null ? null : new MasterRefResponse(item.getBrand().getId(), item.getBrand().getName());
        TaxRate taxRate = item.getTaxRate();
        TaxRateResponse taxRateResponse = new TaxRateResponse(
                taxRate.getId(),
                taxRate.getProperty().getId(),
                taxRate.getClient().getId(),
                taxRate.getName(),
                taxRate.getCgstRate(),
                taxRate.getSgstRate(),
                taxRate.getIgstRate(),
                taxRate.isActive(),
                taxRate.getCreatedAt(),
                taxRate.getUpdatedAt());
        HsnCode hsnCode = item.getHsnCode();
        HsnCodeResponse hsnCodeResponse = new HsnCodeResponse(
                hsnCode.getId(),
                hsnCode.getProperty().getId(),
                hsnCode.getClient().getId(),
                hsnCode.getCode(),
                hsnCode.getDescription(),
                hsnCode.isActive(),
                hsnCode.getCreatedAt(),
                hsnCode.getUpdatedAt());

        return new ItemResponse(
                item.getId(),
                item.getProperty().getId(),
                item.getClient().getId(),
                item.getSku(),
                item.getBarcode(),
                item.getName(),
                category,
                brand,
                taxRateResponse,
                hsnCodeResponse,
                item.getUnit(),
                item.getSellingPrice(),
                item.getCostPrice(),
                item.isActive(),
                item.getCreatedAt(),
                item.getUpdatedAt());
    }
}
