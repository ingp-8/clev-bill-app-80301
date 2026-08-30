package com.clevstack.clevbill.controller;

import com.clevstack.clevbill.dto.ReturnRequest;
import com.clevstack.clevbill.dto.ReturnResponse;
import com.clevstack.clevbill.service.ReturnService;
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
@RequestMapping("/api/v1/sales/{saleId}/returns")
public class ReturnController {

    private final ReturnService returnService;

    public ReturnController(ReturnService returnService) {
        this.returnService = returnService;
    }

    @PreAuthorize("hasAuthority('RETURNS:CREATE')")
    @PostMapping
    public ReturnResponse create(
            @PathVariable Long saleId, @Valid @RequestBody ReturnRequest request, Principal principal) {
        return returnService.create(saleId, request, principal.getName());
    }

    @PreAuthorize("hasAuthority('RETURNS:VIEW')")
    @GetMapping
    public List<ReturnResponse> list(@PathVariable Long saleId) {
        return returnService.list(saleId);
    }
}
