package com.clevstack.clevbill.service;

import com.clevstack.clevbill.dto.BrandRequest;
import com.clevstack.clevbill.dto.BrandResponse;
import com.clevstack.clevbill.exception.ResourceNotFoundException;
import com.clevstack.clevbill.model.Brand;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.repository.BrandRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BrandService {

    private final BrandRepository brandRepository;
    private final PropertyService propertyService;

    public BrandService(BrandRepository brandRepository, PropertyService propertyService) {
        this.brandRepository = brandRepository;
        this.propertyService = propertyService;
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> listByProperty(Long propertyId) {
        propertyService.findEntity(propertyId);
        return brandRepository.findByPropertyId(propertyId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BrandResponse get(Long id) {
        return toResponse(findEntity(id));
    }

    public BrandResponse create(Long propertyId, BrandRequest request) {
        Property property = propertyService.findEntity(propertyId);
        Brand brand = new Brand();
        brand.setProperty(property);
        brand.setClient(property.getClient());
        applyRequest(brand, request);
        return toResponse(brandRepository.save(brand));
    }

    public BrandResponse update(Long id, BrandRequest request) {
        Brand brand = findEntity(id);
        applyRequest(brand, request);
        return toResponse(brandRepository.save(brand));
    }

    public void delete(Long id) {
        brandRepository.delete(findEntity(id));
    }

    Brand findEntity(Long id) {
        return brandRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Brand not found: " + id));
    }

    private void applyRequest(Brand brand, BrandRequest request) {
        brand.setName(request.name());
        brand.setActive(request.active());
    }

    private BrandResponse toResponse(Brand brand) {
        return new BrandResponse(
                brand.getId(),
                brand.getProperty().getId(),
                brand.getClient().getId(),
                brand.getName(),
                brand.isActive(),
                brand.getCreatedAt(),
                brand.getUpdatedAt());
    }
}
