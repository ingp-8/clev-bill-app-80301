package com.clevstack.clevbill.dto;

import java.time.Instant;
import java.util.List;

public record LoginResponse(
        String token, String refreshToken, String username, String fullName, List<String> roles, Instant expiresAt) {
}
