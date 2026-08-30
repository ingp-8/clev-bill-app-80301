package com.clevstack.clevbill.service;

import com.clevstack.clevbill.dto.PropertyRequest;
import com.clevstack.clevbill.dto.PropertyResponse;
import com.clevstack.clevbill.exception.ResourceNotFoundException;
import com.clevstack.clevbill.model.Client;
import com.clevstack.clevbill.model.Property;
import com.clevstack.clevbill.repository.PropertyRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final ClientService clientService;

    public PropertyService(PropertyRepository propertyRepository, ClientService clientService) {
        this.propertyRepository = propertyRepository;
        this.clientService = clientService;
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> listByClient(Long clientId) {
        clientService.findEntity(clientId);
        return propertyRepository.findByClientId(clientId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PropertyResponse get(Long id) {
        return toResponse(findEntity(id));
    }

    public PropertyResponse create(Long clientId, PropertyRequest request) {
        Client client = clientService.findEntity(clientId);
        Property property = new Property();
        property.setClient(client);
        applyRequest(property, request);
        return toResponse(propertyRepository.save(property));
    }

    public PropertyResponse update(Long id, PropertyRequest request) {
        Property property = findEntity(id);
        applyRequest(property, request);
        return toResponse(propertyRepository.save(property));
    }

    public void delete(Long id) {
        propertyRepository.delete(findEntity(id));
    }

    Property findEntity(Long id) {
        return propertyRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + id));
    }

    private void applyRequest(Property property, PropertyRequest request) {
        property.setPropertyName(request.propertyName());
        property.setAddress(request.address());
        property.setGstin(request.gstin());
        property.setInvoiceSeriesPrefix(request.invoiceSeriesPrefix());
        property.setDefaultCgstRate(request.defaultCgstRate());
        property.setDefaultSgstRate(request.defaultSgstRate());
        property.setDefaultIgstRate(request.defaultIgstRate());
        property.setEInvoiceEnabled(request.eInvoiceEnabled());
        property.setActive(request.active());
    }

    private PropertyResponse toResponse(Property property) {
        return new PropertyResponse(
                property.getId(),
                property.getClient().getId(),
                property.getClient().getClientName(),
                property.getPropertyName(),
                property.getAddress(),
                property.getGstin(),
                property.getInvoiceSeriesPrefix(),
                property.getDefaultCgstRate(),
                property.getDefaultSgstRate(),
                property.getDefaultIgstRate(),
                property.isEInvoiceEnabled(),
                property.isActive(),
                property.getCreatedAt(),
                property.getUpdatedAt());
    }
}
