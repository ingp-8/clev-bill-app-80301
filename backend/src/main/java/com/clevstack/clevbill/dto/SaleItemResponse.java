package com.clevstack.clevbill.dto;

import java.math.BigDecimal;

public record SaleItemResponse(
        Long id,
        MasterRefResponse item,
        String sku,
        String name,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal cgstAmount,
        BigDecimal sgstAmount,
        BigDecimal igstAmount,
        BigDecimal lineSubtotal,
        BigDecimal lineTotal) {
}
