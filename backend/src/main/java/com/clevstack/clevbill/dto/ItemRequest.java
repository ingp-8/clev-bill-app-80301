package com.clevstack.clevbill.dto;

import com.clevstack.clevbill.model.ItemUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ItemRequest(
        @NotBlank String sku,
        String barcode,
        @NotBlank String name,
        Long categoryId,
        Long brandId,
        @NotNull Long taxRateId,
        @NotNull Long hsnCodeId,
        @NotNull ItemUnit unit,
        @NotNull @DecimalMin("0.00") BigDecimal sellingPrice,
        BigDecimal costPrice,
        @NotNull Boolean active) {
}
