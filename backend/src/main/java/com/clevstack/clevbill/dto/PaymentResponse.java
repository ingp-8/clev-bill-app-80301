package com.clevstack.clevbill.dto;

import com.clevstack.clevbill.model.PaymentMethod;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(Long id, PaymentMethod method, BigDecimal amount, Instant createdAt) {
}
