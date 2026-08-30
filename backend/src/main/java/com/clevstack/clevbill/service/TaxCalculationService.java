package com.clevstack.clevbill.service;

import com.clevstack.clevbill.dto.LineTax;
import com.clevstack.clevbill.model.TaxRate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

/**
 * Per-line GST split. Rounds at the line level (never sum-then-round — see
 * the gst-compliance skill) and chooses CGST+SGST vs IGST by comparing the
 * store's and customer's GSTIN state codes (first two digits). Either GSTIN
 * missing defaults to intra-state, the common case for a walk-in retail sale.
 */
@Service
public class TaxCalculationService {

    public LineTax calculate(BigDecimal lineSubtotal, TaxRate taxRate, String storeGstin, String customerGstin) {
        if (isInterstate(storeGstin, customerGstin)) {
            BigDecimal igstAmount = round(lineSubtotal.multiply(taxRate.getIgstRate()).divide(BigDecimal.valueOf(100)));
            return new LineTax(BigDecimal.ZERO, BigDecimal.ZERO, igstAmount);
        }

        BigDecimal cgstAmount = round(lineSubtotal.multiply(taxRate.getCgstRate()).divide(BigDecimal.valueOf(100)));
        BigDecimal sgstAmount = round(lineSubtotal.multiply(taxRate.getSgstRate()).divide(BigDecimal.valueOf(100)));
        return new LineTax(cgstAmount, sgstAmount, BigDecimal.ZERO);
    }

    private boolean isInterstate(String storeGstin, String customerGstin) {
        if (storeGstin == null || storeGstin.length() < 2 || customerGstin == null || customerGstin.length() < 2) {
            return false;
        }
        return !storeGstin.substring(0, 2).equals(customerGstin.substring(0, 2));
    }

    private BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
