package com.clevstack.clevbill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PriceListRequest(@NotBlank String name, @NotNull Boolean isDefault, @NotNull Boolean active) {
}
