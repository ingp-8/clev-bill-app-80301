package com.clevstack.clevbill.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record HsnCodeRequest(
        @NotNull @Pattern(regexp = "^([0-9]{4}|[0-9]{6}|[0-9]{8})$", message = "must be a valid 4, 6, or 8-digit HSN code")
                String code,
        String description,
        @NotNull Boolean active) {
}
