package com.clevstack.clevbill.dto;

import java.util.List;

public record MeResponse(
        Long id, String username, String fullName, List<String> roles, List<String> permissions, boolean superAdmin) {
}
