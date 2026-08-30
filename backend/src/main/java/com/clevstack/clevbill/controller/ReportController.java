package com.clevstack.clevbill.controller;

import com.clevstack.clevbill.dto.DailySalesResponse;
import com.clevstack.clevbill.dto.ItemSalesResponse;
import com.clevstack.clevbill.dto.SalesSummaryResponse;
import com.clevstack.clevbill.service.ReportService;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/properties/{propertyId}/reports/sales")
@PreAuthorize("hasAuthority('REPORTS:VIEW')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    public SalesSummaryResponse summary(
            @PathVariable Long propertyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) Long cashierId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long itemId,
            Principal principal) {
        return reportService.getSummary(propertyId, from, to, cashierId, categoryId, itemId, principal.getName());
    }

    @GetMapping("/by-item")
    public List<ItemSalesResponse> byItem(
            @PathVariable Long propertyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) Long cashierId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long itemId,
            Principal principal) {
        return reportService.getByItem(propertyId, from, to, cashierId, categoryId, itemId, principal.getName());
    }

    @GetMapping("/by-day")
    public List<DailySalesResponse> byDay(
            @PathVariable Long propertyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) Long cashierId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long itemId,
            Principal principal) {
        return reportService.getByDay(propertyId, from, to, cashierId, categoryId, itemId, principal.getName());
    }
}
