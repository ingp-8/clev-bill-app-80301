package com.clevstack.clevbill.service;

import com.clevstack.clevbill.dto.PriceListRequest;
import com.clevstack.clevbill.dto.PriceListResponse;
import com.clevstack.clevbill.exception.ResourceNotFoundException;
import com.clevstack.clevbill.model.PriceList;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.repository.PriceListRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PriceListService {

    private final PriceListRepository priceListRepository;
    private final PropertyService propertyService;

    public PriceListService(PriceListRepository priceListRepository, PropertyService propertyService) {
        this.priceListRepository = priceListRepository;
        this.propertyService = propertyService;
    }

    @Transactional(readOnly = true)
    public List<PriceListResponse> listByProperty(Long propertyId) {
        propertyService.findEntity(propertyId);
        return priceListRepository.findByPropertyId(propertyId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PriceListResponse get(Long id) {
        return toResponse(findEntity(id));
    }

    public PriceListResponse create(Long propertyId, PriceListRequest request) {
        Property property = propertyService.findEntity(propertyId);
        PriceList priceList = new PriceList();
        priceList.setProperty(property);
        priceList.setClient(property.getClient());
        applyRequest(priceList, request);
        return toResponse(priceListRepository.save(priceList));
    }

    public PriceListResponse update(Long id, PriceListRequest request) {
        PriceList priceList = findEntity(id);
        applyRequest(priceList, request);
        return toResponse(priceListRepository.save(priceList));
    }

    public void delete(Long id) {
        priceListRepository.delete(findEntity(id));
    }

    PriceList findEntity(Long id) {
        return priceListRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Price list not found: " + id));
    }

    private void applyRequest(PriceList priceList, PriceListRequest request) {
        priceList.setName(request.name());
        priceList.setDefault(request.isDefault());
        priceList.setActive(request.active());
    }

    private PriceListResponse toResponse(PriceList priceList) {
        return new PriceListResponse(
                priceList.getId(),
                priceList.getProperty().getId(),
                priceList.getClient().getId(),
                priceList.getName(),
                priceList.isDefault(),
                priceList.isActive(),
                priceList.getCreatedAt(),
                priceList.getUpdatedAt());
    }
}
