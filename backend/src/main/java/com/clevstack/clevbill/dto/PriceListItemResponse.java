package com.clevstack.clevbill.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceListItemResponse(
        Long id,
        Long priceListId,
        MasterRefResponse item,
        BigDecimal price,
        Instant createdAt,
        Instant updatedAt) {
}
