package com.clevstack.clevbill.dto;

import java.math.BigDecimal;

public record ItemSalesResponse(Long itemId, String sku, String name, BigDecimal quantitySold, BigDecimal revenue) {
}
