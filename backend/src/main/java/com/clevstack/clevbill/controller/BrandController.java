package com.clevstack.clevbill.controller;

import com.clevstack.clevbill.dto.BrandRequest;
import com.clevstack.clevbill.dto.BrandResponse;
import com.clevstack.clevbill.service.BrandService;
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
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PreAuthorize("hasAuthority('MASTERS_BRAND:VIEW')")
    @GetMapping("/api/v1/properties/{propertyId}/brands")
    public List<BrandResponse> listByProperty(@PathVariable Long propertyId) {
        return brandService.listByProperty(propertyId);
    }

    @PreAuthorize("hasAuthority('MASTERS_BRAND:VIEW')")
    @GetMapping("/api/v1/brands/{id}")
    public BrandResponse get(@PathVariable Long id) {
        return brandService.get(id);
    }

    @PreAuthorize("hasAuthority('MASTERS_BRAND:CREATE')")
    @PostMapping("/api/v1/properties/{propertyId}/brands")
    public BrandResponse create(@PathVariable Long propertyId, @Valid @RequestBody BrandRequest request) {
        return brandService.create(propertyId, request);
    }

    @PreAuthorize("hasAuthority('MASTERS_BRAND:EDIT')")
    @PutMapping("/api/v1/brands/{id}")
    public BrandResponse update(@PathVariable Long id, @Valid @RequestBody BrandRequest request) {
        return brandService.update(id, request);
    }

    @PreAuthorize("hasAuthority('MASTERS_BRAND:DELETE')")
    @DeleteMapping("/api/v1/brands/{id}")
    public void delete(@PathVariable Long id) {
        brandService.delete(id);
    }
}
