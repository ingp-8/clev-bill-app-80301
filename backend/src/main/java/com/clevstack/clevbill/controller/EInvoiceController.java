package com.clevstack.clevbill.controller;

import com.clevstack.clevbill.dto.EInvoiceResponse;
import com.clevstack.clevbill.service.EInvoiceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sales/{saleId}/e-invoice")
public class EInvoiceController {

    private final EInvoiceService eInvoiceService;

    public EInvoiceController(EInvoiceService eInvoiceService) {
        this.eInvoiceService = eInvoiceService;
    }

    @PreAuthorize("hasAuthority('BILLING:VIEW')")
    @GetMapping
    public EInvoiceResponse get(@PathVariable Long saleId) {
        return eInvoiceService.getForSale(saleId);
    }
}
