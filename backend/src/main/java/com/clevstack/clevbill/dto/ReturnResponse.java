package com.clevstack.clevbill.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ReturnResponse(
        Long id,
        Long saleId,
        String reason,
        BigDecimal totalAmount,
        List<ReturnItemResponse> items,
        Instant createdAt) {
}
