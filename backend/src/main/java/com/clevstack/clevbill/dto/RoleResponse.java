package com.clevstack.clevbill.dto;

import java.time.Instant;
import java.util.List;

public record RoleResponse(
        Long id,
        String roleName,
        String description,
        boolean isSystem,
        boolean active,
        List<PermissionResponse> permissions,
        Instant createdAt,
        Instant updatedAt) {
}
