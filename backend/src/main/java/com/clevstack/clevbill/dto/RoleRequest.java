package com.clevstack.clevbill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RoleRequest(
        @NotBlank String roleName, String description, @NotNull Boolean active, @NotNull List<Long> permissionIds) {
}
