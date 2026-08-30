package com.clevstack.clevbill.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A GST-registered place of business under a {@link Client}. Owns the
 * fields that used to live on the single Business Settings row —
 * invoice numbering, GSTIN, default tax rates, e-invoice eligibility —
 * because each property is its own GST registration.
 */
@Entity
@Table(name = "properties")
@Getter
@Setter
@NoArgsConstructor
public class Property extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "property_name", nullable = false, length = 150)
    private String propertyName;

    @Column(length = 300)
    private String address;

    @Column(length = 15)
    private String gstin;

    @Column(name = "invoice_series_prefix", nullable = false, length = 10)
    private String invoiceSeriesPrefix;

    @Column(name = "default_cgst_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal defaultCgstRate;

    @Column(name = "default_sgst_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal defaultSgstRate;

    @Column(name = "default_igst_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal defaultIgstRate;

    @Column(name = "e_invoice_enabled", nullable = false)
    private boolean eInvoiceEnabled = false;

    @Column(nullable = false)
    private boolean active = true;
}
