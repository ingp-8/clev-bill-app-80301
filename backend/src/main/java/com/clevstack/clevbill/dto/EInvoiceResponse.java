package com.clevstack.clevbill.dto;

import com.clevstack.clevbill.model.EInvoiceStatus;
import java.time.Instant;

public record EInvoiceResponse(
        Long id,
        Long saleId,
        EInvoiceStatus status,
        String irn,
        String signedQrCode,
        int attempts,
        String lastError,
        Instant lastAttemptAt) {
}
