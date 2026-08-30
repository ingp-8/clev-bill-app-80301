package com.clevstack.clevbill.dto;

import java.time.Instant;

public record ClientResponse(
        Long id,
        String clientName,
        String address,
        String email,
        String mobileNo,
        String logoPath,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
}
