package com.clevstack.clevbill.dto;

import java.math.BigDecimal;

public record ReturnItemResponse(Long id, Long saleItemId, String itemName, BigDecimal quantity, BigDecimal amount) {
}
