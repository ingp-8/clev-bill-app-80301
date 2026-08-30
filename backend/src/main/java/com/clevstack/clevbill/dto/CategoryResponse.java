package com.clevstack.clevbill.dto;

import java.time.Instant;

public record CategoryResponse(
        Long id, Long propertyId, Long clientId, String name, boolean active, Instant createdAt, Instant updatedAt) {
}
