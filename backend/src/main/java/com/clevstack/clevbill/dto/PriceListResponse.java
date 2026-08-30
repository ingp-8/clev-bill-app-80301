package com.clevstack.clevbill.dto;

import java.time.Instant;

public record PriceListResponse(
        Long id,
        Long propertyId,
        Long clientId,
        String name,
        boolean isDefault,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
}
