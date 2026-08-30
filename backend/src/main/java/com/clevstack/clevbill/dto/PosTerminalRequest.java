package com.clevstack.clevbill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PosTerminalRequest(@NotBlank String posName, @NotNull Boolean active) {
}
