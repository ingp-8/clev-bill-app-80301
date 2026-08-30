package com.clevstack.clevbill.controller;

import com.clevstack.clevbill.dto.PosTerminalRequest;
import com.clevstack.clevbill.dto.PosTerminalResponse;
import com.clevstack.clevbill.service.PosTerminalService;
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
public class PosTerminalController {

    private final PosTerminalService posTerminalService;

    public PosTerminalController(PosTerminalService posTerminalService) {
        this.posTerminalService = posTerminalService;
    }

    @PreAuthorize("hasAuthority('POS_MGMT:VIEW')")
    @GetMapping("/api/v1/properties/{propertyId}/pos")
    public List<PosTerminalResponse> listByProperty(@PathVariable Long propertyId) {
        return posTerminalService.listByProperty(propertyId);
    }

    @PreAuthorize("hasAuthority('POS_MGMT:CREATE')")
    @PostMapping("/api/v1/properties/{propertyId}/pos")
    public PosTerminalResponse create(@PathVariable Long propertyId, @Valid @RequestBody PosTerminalRequest request) {
        return posTerminalService.create(propertyId, request);
    }

    @PreAuthorize("hasAuthority('POS_MGMT:EDIT')")
    @PutMapping("/api/v1/pos/{id}")
    public PosTerminalResponse update(@PathVariable Long id, @Valid @RequestBody PosTerminalRequest request) {
        return posTerminalService.update(id, request);
    }

    @PreAuthorize("hasAuthority('POS_MGMT:DELETE')")
    @DeleteMapping("/api/v1/pos/{id}")
    public void delete(@PathVariable Long id) {
        posTerminalService.delete(id);
    }
}
