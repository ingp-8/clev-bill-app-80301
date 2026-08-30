package com.clevstack.clevbill.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record InventoryAdjustmentRequest(@NotNull Long itemId, @NotNull BigDecimal quantityDelta, String note) {
}
