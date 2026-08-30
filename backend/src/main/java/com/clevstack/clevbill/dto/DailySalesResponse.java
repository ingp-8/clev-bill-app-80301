package com.clevstack.clevbill.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySalesResponse(LocalDate date, long saleCount, BigDecimal revenue) {
}
