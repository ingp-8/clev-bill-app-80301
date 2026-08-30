package com.clevstack.clevbill.controller;

import com.clevstack.clevbill.dto.PriceListItemRequest;
import com.clevstack.clevbill.dto.PriceListItemResponse;
import com.clevstack.clevbill.service.PriceListItemService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/masters/price-lists/{priceListId}/items")
public class PriceListItemController {

    private final PriceListItemService priceListItemService;

    public PriceListItemController(PriceListItemService priceListItemService) {
        this.priceListItemService = priceListItemService;
    }

    @PreAuthorize("hasAuthority('MASTERS_PRICE_LIST:VIEW')")
    @GetMapping
    public List<PriceListItemResponse> list(@PathVariable Long priceListId) {
        return priceListItemService.list(priceListId);
    }

    @PreAuthorize("hasAuthority('MASTERS_PRICE_LIST:CREATE')")
    @PostMapping
    public PriceListItemResponse create(
            @PathVariable Long priceListId, @Valid @RequestBody PriceListItemRequest request) {
        return priceListItemService.create(priceListId, request);
    }

    @PreAuthorize("hasAuthority('MASTERS_PRICE_LIST:EDIT')")
    @PutMapping("/{id}")
    public PriceListItemResponse update(
            @PathVariable Long priceListId, @PathVariable Long id, @Valid @RequestBody PriceListItemRequest request) {
        return priceListItemService.update(priceListId, id, request);
    }

    @PreAuthorize("hasAuthority('MASTERS_PRICE_LIST:DELETE')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long priceListId, @PathVariable Long id) {
        priceListItemService.delete(priceListId, id);
    }
}
