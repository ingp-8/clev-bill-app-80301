package com.clevstack.clevbill.controller;

import com.clevstack.clevbill.dto.InventoryAdjustmentRequest;
import com.clevstack.clevbill.dto.InventoryResponse;
import com.clevstack.clevbill.dto.InventoryTransactionResponse;
import com.clevstack.clevbill.exception.ResourceNotFoundException;
import com.clevstack.clevbill.model.User;
import com.clevstack.clevbill.repository.UserRepository;
import com.clevstack.clevbill.service.InventoryService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final UserRepository userRepository;

    public InventoryController(InventoryService inventoryService, UserRepository userRepository) {
        this.inventoryService = inventoryService;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAuthority('INVENTORY:VIEW')")
    @GetMapping
    public List<InventoryResponse> list() {
        return inventoryService.listAll();
    }

    @PreAuthorize("hasAuthority('INVENTORY:VIEW')")
    @GetMapping("/{itemId}")
    public InventoryResponse get(@PathVariable Long itemId) {
        return inventoryService.getStock(itemId);
    }

    @PreAuthorize("hasAuthority('INVENTORY:VIEW')")
    @GetMapping("/{itemId}/transactions")
    public List<InventoryTransactionResponse> transactions(@PathVariable Long itemId) {
        return inventoryService.getTransactions(itemId);
    }

    @PreAuthorize("hasAuthority('INVENTORY:EDIT')")
    @PostMapping("/adjustments")
    public InventoryResponse adjust(@Valid @RequestBody InventoryAdjustmentRequest request, Principal principal) {
        User actor = userRepository
                .findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));
        return inventoryService.adjust(request.itemId(), request.quantityDelta(), request.note(), actor);
    }
}
