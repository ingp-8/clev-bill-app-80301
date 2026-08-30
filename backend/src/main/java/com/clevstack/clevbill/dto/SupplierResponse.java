package com.clevstack.clevbill.dto;

import java.time.Instant;
import java.util.List;

public record SupplierResponse(
        Long id,
        Long clientId,
        String name,
        String phone,
        String email,
        String gstin,
        String address,
        boolean active,
        List<Long> propertyIds,
        Instant createdAt,
        Instant updatedAt) {
}
