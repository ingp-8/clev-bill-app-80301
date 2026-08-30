package com.clevstack.clevbill.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.clevstack.clevbill.dto.LineTax;
import com.clevstack.clevbill.model.TaxRate;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Pure unit tests — no Spring context needed. Covers the CGST+SGST vs IGST
 * split rule and the per-line rounding contract from the gst-compliance
 * skill (round at the line level, HALF_UP to 2dp, never sum-then-round).
 */
class TaxCalculationServiceTest {

    private final TaxCalculationService service = new TaxCalculationService();

    private TaxRate taxRate(String cgst, String sgst, String igst) {
        TaxRate taxRate = new TaxRate();
        taxRate.setCgstRate(new BigDecimal(cgst));
        taxRate.setSgstRate(new BigDecimal(sgst));
        taxRate.setIgstRate(new BigDecimal(igst));
        return taxRate;
    }

    @Test
    void bothGstinsMissingDefaultsToIntraState() {
        LineTax tax = service.calculate(new BigDecimal("100.00"), taxRate("9", "9", "18"), null, null);

        assertThat(tax.cgstAmount()).isEqualByComparingTo("9.00");
        assertThat(tax.sgstAmount()).isEqualByComparingTo("9.00");
        assertThat(tax.igstAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void sameStateCodeIsIntraState() {
        LineTax tax = service.calculate(
                new BigDecimal("100.00"), taxRate("9", "9", "18"), "27AAAAA0000A1Z5", "27BBBBB1111B2Z6");

        assertThat(tax.cgstAmount()).isEqualByComparingTo("9.00");
        assertThat(tax.sgstAmount()).isEqualByComparingTo("9.00");
        assertThat(tax.igstAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void differentStateCodeIsInterState() {
        LineTax tax = service.calculate(
                new BigDecimal("100.00"), taxRate("9", "9", "18"), "27AAAAA0000A1Z5", "09BBBBB1111B2Z6");

        assertThat(tax.cgstAmount()).isEqualByComparingTo("0.00");
        assertThat(tax.sgstAmount()).isEqualByComparingTo("0.00");
        assertThat(tax.igstAmount()).isEqualByComparingTo("18.00");
    }

    @Test
    void customerGstinMissingDefaultsToIntraStateEvenWithStoreGstinPresent() {
        LineTax tax = service.calculate(new BigDecimal("100.00"), taxRate("9", "9", "18"), "27AAAAA0000A1Z5", null);

        assertThat(tax.cgstAmount()).isEqualByComparingTo("9.00");
        assertThat(tax.igstAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void roundsHalfUpToTwoDecimalPlacesPerLine() {
        // 33.33 * 9% = 2.9997 -> rounds up to 3.00, not truncates to 2.99
        LineTax tax = service.calculate(new BigDecimal("33.33"), taxRate("9", "9", "18"), null, null);

        assertThat(tax.cgstAmount()).isEqualByComparingTo("3.00");
        assertThat(tax.sgstAmount()).isEqualByComparingTo("3.00");
    }

    @Test
    void zeroRatedLineProducesZeroTax() {
        LineTax tax = service.calculate(new BigDecimal("100.00"), taxRate("0", "0", "0"), null, null);

        assertThat(tax.cgstAmount()).isEqualByComparingTo("0.00");
        assertThat(tax.sgstAmount()).isEqualByComparingTo("0.00");
        assertThat(tax.igstAmount()).isEqualByComparingTo("0.00");
    }
}
