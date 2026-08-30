package com.clevstack.clevbill.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SaleResponse(
        Long id,
        String billNumber,
        Long propertyId,
        Long posId,
        String posName,
        MasterRefResponse customer,
        MasterRefResponse cashier,
        List<SaleItemResponse> items,
        List<PaymentResponse> payments,
        BigDecimal subtotalAmount,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        Instant createdAt) {
}
