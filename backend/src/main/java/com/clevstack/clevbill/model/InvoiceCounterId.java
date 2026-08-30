package com.clevstack.clevbill.model;

import java.io.Serializable;
import java.util.Objects;

public class InvoiceCounterId implements Serializable {

    private Long property;
    private String financialYear;

    public InvoiceCounterId() {
    }

    public InvoiceCounterId(Long property, String financialYear) {
        this.property = property;
        this.financialYear = financialYear;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InvoiceCounterId that)) {
            return false;
        }
        return Objects.equals(property, that.property) && Objects.equals(financialYear, that.financialYear);
    }

    @Override
    public int hashCode() {
        return Objects.hash(property, financialYear);
    }
}
