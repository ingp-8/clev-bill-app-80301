package com.clevstack.clevbill.dto;

import com.clevstack.clevbill.model.InventoryChangeReason;
import java.math.BigDecimal;
import java.time.Instant;

public record InventoryTransactionResponse(
        Long id,
        Long itemId,
        BigDecimal changeQuantity,
        InventoryChangeReason reason,
        Long referenceId,
        String note,
        String createdByName,
        Instant createdAt) {
}
