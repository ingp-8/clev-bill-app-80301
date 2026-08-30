package com.clevstack.clevbill.service;

import com.clevstack.clevbill.dto.HsnCodeRequest;
import com.clevstack.clevbill.dto.HsnCodeResponse;
import com.clevstack.clevbill.exception.ResourceNotFoundException;
import com.clevstack.clevbill.model.HsnCode;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.repository.HsnCodeRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class HsnCodeService {

    private final HsnCodeRepository hsnCodeRepository;
    private final PropertyService propertyService;

    public HsnCodeService(HsnCodeRepository hsnCodeRepository, PropertyService propertyService) {
        this.hsnCodeRepository = hsnCodeRepository;
        this.propertyService = propertyService;
    }

    @Transactional(readOnly = true)
    public List<HsnCodeResponse> listByProperty(Long propertyId) {
        propertyService.findEntity(propertyId);
        return hsnCodeRepository.findByPropertyId(propertyId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public HsnCodeResponse get(Long id) {
        return toResponse(findEntity(id));
    }

    public HsnCodeResponse create(Long propertyId, HsnCodeRequest request) {
        Property property = propertyService.findEntity(propertyId);
        HsnCode hsnCode = new HsnCode();
        hsnCode.setProperty(property);
        hsnCode.setClient(property.getClient());
        applyRequest(hsnCode, request);
        return toResponse(hsnCodeRepository.save(hsnCode));
    }

    public HsnCodeResponse update(Long id, HsnCodeRequest request) {
        HsnCode hsnCode = findEntity(id);
        applyRequest(hsnCode, request);
        return toResponse(hsnCodeRepository.save(hsnCode));
    }

    public void delete(Long id) {
        hsnCodeRepository.delete(findEntity(id));
    }

    HsnCode findEntity(Long id) {
        return hsnCodeRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HSN code not found: " + id));
    }

    private void applyRequest(HsnCode hsnCode, HsnCodeRequest request) {
        hsnCode.setCode(request.code());
        hsnCode.setDescription(request.description());
        hsnCode.setActive(request.active());
    }

    private HsnCodeResponse toResponse(HsnCode hsnCode) {
        return new HsnCodeResponse(
                hsnCode.getId(),
                hsnCode.getProperty().getId(),
                hsnCode.getClient().getId(),
                hsnCode.getCode(),
                hsnCode.getDescription(),
                hsnCode.isActive(),
                hsnCode.getCreatedAt(),
                hsnCode.getUpdatedAt());
    }
}
