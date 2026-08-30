package com.clevstack.clevbill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * {@code password} is required on create; on update, leave it blank to
 * keep the existing password unchanged.
 */
public record UserRequest(
        @NotBlank String username,
        @NotBlank String fullName,
        String password,
        @NotNull Boolean enabled,
        @NotNull List<Long> roleIds,
        @NotNull List<Long> propertyIds) {
}
