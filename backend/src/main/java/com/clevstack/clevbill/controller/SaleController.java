package com.clevstack.clevbill.controller;

import com.clevstack.clevbill.dto.CheckoutRequest;
import com.clevstack.clevbill.dto.SaleResponse;
import com.clevstack.clevbill.service.SaleService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PreAuthorize("hasAuthority('BILLING:CREATE')")
    @PostMapping("/api/v1/sales")
    public SaleResponse checkout(@Valid @RequestBody CheckoutRequest request, Principal principal) {
        return saleService.checkout(request, principal.getName());
    }

    @PreAuthorize("hasAuthority('BILLING:VIEW')")
    @GetMapping("/api/v1/properties/{propertyId}/sales")
    public List<SaleResponse> listByProperty(@PathVariable Long propertyId) {
        return saleService.listByProperty(propertyId);
    }

    @PreAuthorize("hasAuthority('BILLING:VIEW')")
    @GetMapping("/api/v1/sales/{id}")
    public SaleResponse get(@PathVariable Long id) {
        return saleService.get(id);
    }
}
