package com.clevstack.clevbill.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TaxRateResponse(
        Long id,
        Long propertyId,
        Long clientId,
        String name,
        BigDecimal cgstRate,
        BigDecimal sgstRate,
        BigDecimal igstRate,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
}
