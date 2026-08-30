package com.clevstack.clevbill.dto;

import java.time.Instant;
import java.util.List;

public record UserResponse(
        Long id,
        String username,
        String fullName,
        boolean enabled,
        List<MasterRefResponse> roles,
        List<Long> propertyIds,
        Instant createdAt,
        Instant updatedAt) {
}
