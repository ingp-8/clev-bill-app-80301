package com.clevstack.clevbill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PropertyRequest(
        @NotBlank String propertyName,
        String address,
        String gstin,
        @NotBlank String invoiceSeriesPrefix,
        @NotNull BigDecimal defaultCgstRate,
        @NotNull BigDecimal defaultSgstRate,
        @NotNull BigDecimal defaultIgstRate,
        @NotNull Boolean eInvoiceEnabled,
        @NotNull Boolean active) {
}
