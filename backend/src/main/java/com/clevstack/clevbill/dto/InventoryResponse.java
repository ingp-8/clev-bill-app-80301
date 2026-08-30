package com.clevstack.clevbill.dto;

import java.math.BigDecimal;

public record InventoryResponse(Long itemId, String sku, String itemName, BigDecimal quantity) {
}
