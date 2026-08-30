package com.clevstack.clevbill.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CheckoutRequest(
        @NotNull Long propertyId,
        @NotNull Long posId,
        Long customerId,
        @NotEmpty @Valid List<CheckoutItemRequest> items,
        @NotEmpty @Valid List<PaymentRequest> payments) {
}
