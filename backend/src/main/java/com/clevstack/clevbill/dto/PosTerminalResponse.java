package com.clevstack.clevbill.dto;

import java.time.Instant;

public record PosTerminalResponse(
        Long id,
        Long propertyId,
        String propertyName,
        String posName,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
}
