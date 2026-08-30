package com.clevstack.clevbill.service;

import java.math.BigDecimal;

public record IrpRequest(String billNumber, BigDecimal totalAmount) {
}
