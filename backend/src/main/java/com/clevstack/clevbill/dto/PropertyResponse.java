package com.clevstack.clevbill.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PropertyResponse(
        Long id,
        Long clientId,
        String clientName,
        String propertyName,
        String address,
        String gstin,
        String invoiceSeriesPrefix,
        BigDecimal defaultCgstRate,
        BigDecimal defaultSgstRate,
        BigDecimal defaultIgstRate,
        boolean eInvoiceEnabled,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
}
