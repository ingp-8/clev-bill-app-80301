package com.clevstack.clevbill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClientRequest(
        @NotBlank String clientName,
        String address,
        String email,
        String mobileNo,
        String logoPath,
        @NotNull Boolean active) {
}
