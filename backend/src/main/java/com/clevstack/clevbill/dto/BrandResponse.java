package com.clevstack.clevbill.dto;

import java.time.Instant;

public record BrandResponse(
        Long id, Long propertyId, Long clientId, String name, boolean active, Instant createdAt, Instant updatedAt) {
}
