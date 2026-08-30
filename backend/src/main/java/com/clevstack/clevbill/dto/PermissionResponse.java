package com.clevstack.clevbill.dto;

import com.clevstack.clevbill.model.PermissionAction;

public record PermissionResponse(Long id, String moduleCode, PermissionAction action, String description) {
}
