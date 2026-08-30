package com.clevstack.clevbill.service;

import com.clevstack.clevbill.dto.PosTerminalRequest;
import com.clevstack.clevbill.dto.PosTerminalResponse;
import com.clevstack.clevbill.exception.ResourceNotFoundException;
import com.clevstack.clevbill.model.PosTerminal;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.repository.PosTerminalRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PosTerminalService {

    private final PosTerminalRepository posTerminalRepository;
    private final PropertyService propertyService;

    public PosTerminalService(PosTerminalRepository posTerminalRepository, PropertyService propertyService) {
        this.posTerminalRepository = posTerminalRepository;
        this.propertyService = propertyService;
    }

    @Transactional(readOnly = true)
    public List<PosTerminalResponse> listByProperty(Long propertyId) {
        propertyService.findEntity(propertyId);
        return posTerminalRepository.findByPropertyId(propertyId).stream().map(this::toResponse).toList();
    }

    public PosTerminalResponse create(Long propertyId, PosTerminalRequest request) {
        Property property = propertyService.findEntity(propertyId);
        PosTerminal posTerminal = new PosTerminal();
        posTerminal.setProperty(property);
        posTerminal.setClient(property.getClient());
        applyRequest(posTerminal, request);
        return toResponse(posTerminalRepository.save(posTerminal));
    }

    public PosTerminalResponse update(Long id, PosTerminalRequest request) {
        PosTerminal posTerminal = findEntity(id);
        applyRequest(posTerminal, request);
        return toResponse(posTerminalRepository.save(posTerminal));
    }

    public void delete(Long id) {
        posTerminalRepository.delete(findEntity(id));
    }

    PosTerminal findEntity(Long id) {
        return posTerminalRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("POS terminal not found: " + id));
    }

    private void applyRequest(PosTerminal posTerminal, PosTerminalRequest request) {
        posTerminal.setPosName(request.posName());
        posTerminal.setActive(request.active());
    }

    private PosTerminalResponse toResponse(PosTerminal posTerminal) {
        return new PosTerminalResponse(
                posTerminal.getId(),
                posTerminal.getProperty().getId(),
                posTerminal.getProperty().getPropertyName(),
                posTerminal.getPosName(),
                posTerminal.isActive(),
                posTerminal.getCreatedAt(),
                posTerminal.getUpdatedAt());
    }
}
