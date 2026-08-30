package com.clevstack.clevbill.dto;

import java.math.BigDecimal;

public record LineTax(BigDecimal cgstAmount, BigDecimal sgstAmount, BigDecimal igstAmount) {
}
