package com.clevstack.clevbill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SupplierRequest(
        @NotBlank String name,
        String phone,
        String email,
        String gstin,
        String address,
        @NotNull Boolean active,
        @NotNull List<Long> propertyIds) {
}
