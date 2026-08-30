package com.clevstack.clevbill.service;

import com.clevstack.clevbill.dto.TaxRateRequest;
import com.clevstack.clevbill.dto.TaxRateResponse;
import com.clevstack.clevbill.exception.ResourceNotFoundException;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.model.TaxRate;
import com.clevstack.clevbill.repository.TaxRateRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaxRateService {

    private final TaxRateRepository taxRateRepository;
    private final PropertyService propertyService;

    public TaxRateService(TaxRateRepository taxRateRepository, PropertyService propertyService) {
        this.taxRateRepository = taxRateRepository;
        this.propertyService = propertyService;
    }

    @Transactional(readOnly = true)
    public List<TaxRateResponse> listByProperty(Long propertyId) {
        propertyService.findEntity(propertyId);
        return taxRateRepository.findByPropertyId(propertyId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TaxRateResponse get(Long id) {
        return toResponse(findEntity(id));
    }

    public TaxRateResponse create(Long propertyId, TaxRateRequest request) {
        Property property = propertyService.findEntity(propertyId);
        TaxRate taxRate = new TaxRate();
        taxRate.setProperty(property);
        taxRate.setClient(property.getClient());
        applyRequest(taxRate, request);
        return toResponse(taxRateRepository.save(taxRate));
    }

    public TaxRateResponse update(Long id, TaxRateRequest request) {
        TaxRate taxRate = findEntity(id);
        applyRequest(taxRate, request);
        return toResponse(taxRateRepository.save(taxRate));
    }

    public void delete(Long id) {
        taxRateRepository.delete(findEntity(id));
    }

    TaxRate findEntity(Long id) {
        return taxRateRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tax rate not found: " + id));
    }

    private void applyRequest(TaxRate taxRate, TaxRateRequest request) {
        taxRate.setName(request.name());
        taxRate.setCgstRate(request.cgstRate());
        taxRate.setSgstRate(request.sgstRate());
        taxRate.setIgstRate(request.igstRate());
        taxRate.setActive(request.active());
    }

    private TaxRateResponse toResponse(TaxRate taxRate) {
        return new TaxRateResponse(
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
    }
}
