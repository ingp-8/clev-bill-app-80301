package com.clevstack.clevbill.controller;

import com.clevstack.clevbill.dto.TaxRateRequest;
import com.clevstack.clevbill.dto.TaxRateResponse;
import com.clevstack.clevbill.service.TaxRateService;
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
public class TaxRateController {

    private final TaxRateService taxRateService;

    public TaxRateController(TaxRateService taxRateService) {
        this.taxRateService = taxRateService;
    }

    @PreAuthorize("hasAuthority('MASTERS_TAX_RATE:VIEW')")
    @GetMapping("/api/v1/properties/{propertyId}/tax-rates")
    public List<TaxRateResponse> listByProperty(@PathVariable Long propertyId) {
        return taxRateService.listByProperty(propertyId);
    }

    @PreAuthorize("hasAuthority('MASTERS_TAX_RATE:VIEW')")
    @GetMapping("/api/v1/tax-rates/{id}")
    public TaxRateResponse get(@PathVariable Long id) {
        return taxRateService.get(id);
    }

    @PreAuthorize("hasAuthority('MASTERS_TAX_RATE:CREATE')")
    @PostMapping("/api/v1/properties/{propertyId}/tax-rates")
    public TaxRateResponse create(@PathVariable Long propertyId, @Valid @RequestBody TaxRateRequest request) {
        return taxRateService.create(propertyId, request);
    }

    @PreAuthorize("hasAuthority('MASTERS_TAX_RATE:EDIT')")
    @PutMapping("/api/v1/tax-rates/{id}")
    public TaxRateResponse update(@PathVariable Long id, @Valid @RequestBody TaxRateRequest request) {
        return taxRateService.update(id, request);
    }

    @PreAuthorize("hasAuthority('MASTERS_TAX_RATE:DELETE')")
    @DeleteMapping("/api/v1/tax-rates/{id}")
    public void delete(@PathVariable Long id) {
        taxRateService.delete(id);
    }
}
