package com.clevstack.clevbill.controller;

import com.clevstack.clevbill.dto.HsnCodeRequest;
import com.clevstack.clevbill.dto.HsnCodeResponse;
import com.clevstack.clevbill.service.HsnCodeService;
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
public class HsnCodeController {

    private final HsnCodeService hsnCodeService;

    public HsnCodeController(HsnCodeService hsnCodeService) {
        this.hsnCodeService = hsnCodeService;
    }

    @PreAuthorize("hasAuthority('MASTERS_HSN:VIEW')")
    @GetMapping("/api/v1/properties/{propertyId}/hsn-codes")
    public List<HsnCodeResponse> listByProperty(@PathVariable Long propertyId) {
        return hsnCodeService.listByProperty(propertyId);
    }

    @PreAuthorize("hasAuthority('MASTERS_HSN:VIEW')")
    @GetMapping("/api/v1/hsn-codes/{id}")
    public HsnCodeResponse get(@PathVariable Long id) {
        return hsnCodeService.get(id);
    }

    @PreAuthorize("hasAuthority('MASTERS_HSN:CREATE')")
    @PostMapping("/api/v1/properties/{propertyId}/hsn-codes")
    public HsnCodeResponse create(@PathVariable Long propertyId, @Valid @RequestBody HsnCodeRequest request) {
        return hsnCodeService.create(propertyId, request);
    }

    @PreAuthorize("hasAuthority('MASTERS_HSN:EDIT')")
    @PutMapping("/api/v1/hsn-codes/{id}")
    public HsnCodeResponse update(@PathVariable Long id, @Valid @RequestBody HsnCodeRequest request) {
        return hsnCodeService.update(id, request);
    }

    @PreAuthorize("hasAuthority('MASTERS_HSN:DELETE')")
    @DeleteMapping("/api/v1/hsn-codes/{id}")
    public void delete(@PathVariable Long id) {
        hsnCodeService.delete(id);
    }
}
