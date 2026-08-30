package com.clevstack.clevbill.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The bill-numbering sequence resets per property, not globally — see
 * BillNumberService. Key is (property, financialYear).
 */
@Entity
@Table(name = "invoice_counters")
@IdClass(InvoiceCounterId.class)
@Getter
@Setter
@NoArgsConstructor
public class InvoiceCounter {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property;

    @Id
    @Column(name = "financial_year")
    private String financialYear;

    @Column(name = "next_number", nullable = false)
    private Long nextNumber;
}
