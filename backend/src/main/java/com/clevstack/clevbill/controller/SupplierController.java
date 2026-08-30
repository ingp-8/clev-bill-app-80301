package com.clevstack.clevbill.controller;

import com.clevstack.clevbill.dto.SupplierRequest;
import com.clevstack.clevbill.dto.SupplierResponse;
import com.clevstack.clevbill.service.SupplierService;
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
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PreAuthorize("hasAuthority('MASTERS_SUPPLIER:VIEW')")
    @GetMapping("/api/v1/clients/{clientId}/suppliers")
    public List<SupplierResponse> listByClient(@PathVariable Long clientId) {
        return supplierService.listByClient(clientId);
    }

    @PreAuthorize("hasAuthority('MASTERS_SUPPLIER:VIEW')")
    @GetMapping("/api/v1/suppliers/{id}")
    public SupplierResponse get(@PathVariable Long id) {
        return supplierService.get(id);
    }

    @PreAuthorize("hasAuthority('MASTERS_SUPPLIER:CREATE')")
    @PostMapping("/api/v1/clients/{clientId}/suppliers")
    public SupplierResponse create(@PathVariable Long clientId, @Valid @RequestBody SupplierRequest request) {
        return supplierService.create(clientId, request);
    }

    @PreAuthorize("hasAuthority('MASTERS_SUPPLIER:EDIT')")
    @PutMapping("/api/v1/suppliers/{id}")
    public SupplierResponse update(@PathVariable Long id, @Valid @RequestBody SupplierRequest request) {
        return supplierService.update(id, request);
    }

    @PreAuthorize("hasAuthority('MASTERS_SUPPLIER:DELETE')")
    @DeleteMapping("/api/v1/suppliers/{id}")
    public void delete(@PathVariable Long id) {
        supplierService.delete(id);
    }
}
