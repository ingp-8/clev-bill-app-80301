package com.clevstack.clevbill.dto;

import com.clevstack.clevbill.model.ItemUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record ItemRequest(
        @NotBlank String sku,
        String barcode,
        @NotBlank String name,
        Long categoryId,
        Long brandId,
        @NotNull Long taxRateId,
        @NotBlank @Pattern(regexp = "^([0-9]{4}|[0-9]{6}|[0-9]{8})$", message = "must be a valid 4, 6, or 8-digit HSN code")
                String hsnCode,
        @NotNull ItemUnit unit,
        @NotNull @DecimalMin("0.00") BigDecimal sellingPrice,
        BigDecimal costPrice,
        @NotNull Boolean active) {
}
