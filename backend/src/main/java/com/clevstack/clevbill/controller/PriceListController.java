package com.clevstack.clevbill.controller;

import com.clevstack.clevbill.dto.PriceListRequest;
import com.clevstack.clevbill.dto.PriceListResponse;
import com.clevstack.clevbill.service.PriceListService;
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
public class PriceListController {

    private final PriceListService priceListService;

    public PriceListController(PriceListService priceListService) {
        this.priceListService = priceListService;
    }

    @PreAuthorize("hasAuthority('MASTERS_PRICE_LIST:VIEW')")
    @GetMapping("/api/v1/properties/{propertyId}/price-lists")
    public List<PriceListResponse> listByProperty(@PathVariable Long propertyId) {
        return priceListService.listByProperty(propertyId);
    }

    @PreAuthorize("hasAuthority('MASTERS_PRICE_LIST:VIEW')")
    @GetMapping("/api/v1/price-lists/{id}")
    public PriceListResponse get(@PathVariable Long id) {
        return priceListService.get(id);
    }

    @PreAuthorize("hasAuthority('MASTERS_PRICE_LIST:CREATE')")
    @PostMapping("/api/v1/properties/{propertyId}/price-lists")
    public PriceListResponse create(@PathVariable Long propertyId, @Valid @RequestBody PriceListRequest request) {
        return priceListService.create(propertyId, request);
    }

    @PreAuthorize("hasAuthority('MASTERS_PRICE_LIST:EDIT')")
    @PutMapping("/api/v1/price-lists/{id}")
    public PriceListResponse update(@PathVariable Long id, @Valid @RequestBody PriceListRequest request) {
        return priceListService.update(id, request);
    }

    @PreAuthorize("hasAuthority('MASTERS_PRICE_LIST:DELETE')")
    @DeleteMapping("/api/v1/price-lists/{id}")
    public void delete(@PathVariable Long id) {
        priceListService.delete(id);
    }
}
