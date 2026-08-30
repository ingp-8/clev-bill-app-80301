package com.clevstack.clevbill.controller;

import com.clevstack.clevbill.dto.PropertyRequest;
import com.clevstack.clevbill.dto.PropertyResponse;
import com.clevstack.clevbill.service.PropertyService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @PreAuthorize("hasAuthority('PROPERTY_MGMT:VIEW')")
    @GetMapping("/api/v1/clients/{clientId}/properties")
    public List<PropertyResponse> listByClient(@PathVariable Long clientId) {
        return propertyService.listByClient(clientId);
    }

    @PreAuthorize("hasAuthority('PROPERTY_MGMT:CREATE')")
    @PostMapping("/api/v1/clients/{clientId}/properties")
    public PropertyResponse create(@PathVariable Long clientId, @Valid @RequestBody PropertyRequest request) {
        return propertyService.create(clientId, request);
    }

    @PreAuthorize("hasAuthority('PROPERTY_MGMT:VIEW')")
    @GetMapping("/api/v1/properties/{id}")
    public PropertyResponse get(@PathVariable Long id) {
        return propertyService.get(id);
    }

    @PreAuthorize("hasAuthority('PROPERTY_MGMT:EDIT')")
    @PutMapping("/api/v1/properties/{id}")
    public PropertyResponse update(@PathVariable Long id, @Valid @RequestBody PropertyRequest request) {
        return propertyService.update(id, request);
    }

    @PreAuthorize("hasAuthority('PROPERTY_MGMT:DELETE')")
    @DeleteMapping("/api/v1/properties/{id}")
    public void delete(@PathVariable Long id) {
        propertyService.delete(id);
    }
}
