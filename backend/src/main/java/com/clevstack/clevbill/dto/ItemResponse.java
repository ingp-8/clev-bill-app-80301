package com.clevstack.clevbill.dto;

import com.clevstack.clevbill.model.ItemUnit;
import java.math.BigDecimal;
import java.time.Instant;

public record ItemResponse(
        Long id,
        Long propertyId,
        Long clientId,
        String sku,
        String barcode,
        String name,
        MasterRefResponse category,
        MasterRefResponse brand,
        TaxRateResponse taxRate,
        HsnCodeResponse hsnCode,
        ItemUnit unit,
        BigDecimal sellingPrice,
        BigDecimal costPrice,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
}
