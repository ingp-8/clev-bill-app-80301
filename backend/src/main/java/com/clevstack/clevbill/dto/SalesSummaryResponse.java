package com.clevstack.clevbill.dto;

import java.math.BigDecimal;

public record SalesSummaryResponse(
        long saleCount,
        BigDecimal totalSubtotal,
        BigDecimal totalTax,
        BigDecimal totalRevenue,
        BigDecimal averageSaleValue) {
}
