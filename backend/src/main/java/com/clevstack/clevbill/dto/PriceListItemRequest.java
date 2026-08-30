package com.clevstack.clevbill.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PriceListItemRequest(@NotNull Long itemId, @NotNull @DecimalMin("0.00") BigDecimal price) {
}
