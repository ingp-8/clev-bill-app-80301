package com.clevstack.clevbill.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ReturnRequest(String reason, @NotEmpty @Valid List<ReturnItemRequest> items) {
}
