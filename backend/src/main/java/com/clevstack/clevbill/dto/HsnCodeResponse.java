package com.clevstack.clevbill.dto;

import java.time.Instant;

public record HsnCodeResponse(
        Long id,
        Long propertyId,
        Long clientId,
        String code,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
}
