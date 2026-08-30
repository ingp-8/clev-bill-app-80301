package com.clevstack.clevbill.controller;

import com.clevstack.clevbill.dto.ItemRequest;
import com.clevstack.clevbill.dto.ItemResponse;
import com.clevstack.clevbill.service.ItemService;
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
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PreAuthorize("hasAuthority('MASTERS_ITEM:VIEW')")
    @GetMapping("/api/v1/properties/{propertyId}/items")
    public List<ItemResponse> listByProperty(@PathVariable Long propertyId) {
        return itemService.listByProperty(propertyId);
    }

    @PreAuthorize("hasAuthority('MASTERS_ITEM:VIEW')")
    @GetMapping("/api/v1/items/{id}")
    public ItemResponse get(@PathVariable Long id) {
        return itemService.get(id);
    }

    @PreAuthorize("hasAuthority('MASTERS_ITEM:CREATE')")
    @PostMapping("/api/v1/properties/{propertyId}/items")
    public ItemResponse create(@PathVariable Long propertyId, @Valid @RequestBody ItemRequest request) {
        return itemService.create(propertyId, request);
    }

    @PreAuthorize("hasAuthority('MASTERS_ITEM:EDIT')")
    @PutMapping("/api/v1/items/{id}")
    public ItemResponse update(@PathVariable Long id, @Valid @RequestBody ItemRequest request) {
        return itemService.update(id, request);
    }

    @PreAuthorize("hasAuthority('MASTERS_ITEM:DELETE')")
    @DeleteMapping("/api/v1/items/{id}")
    public void delete(@PathVariable Long id) {
        itemService.delete(id);
    }
}
