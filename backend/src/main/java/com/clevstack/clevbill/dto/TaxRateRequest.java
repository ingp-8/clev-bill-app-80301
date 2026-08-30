package com.clevstack.clevbill.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TaxRateRequest(
        @NotBlank String name,
        @NotNull @DecimalMin("0.00") BigDecimal cgstRate,
        @NotNull @DecimalMin("0.00") BigDecimal sgstRate,
        @NotNull @DecimalMin("0.00") BigDecimal igstRate,
        @NotNull Boolean active) {
}
