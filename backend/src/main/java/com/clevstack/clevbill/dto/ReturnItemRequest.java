package com.clevstack.clevbill.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ReturnItemRequest(@NotNull Long saleItemId, @NotNull @DecimalMin("0.001") BigDecimal quantity) {
}
