package com.clevstack.clevbill.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Which properties a Customer or Supplier serves. {@code partyId} is a
 * plain FK to whichever table {@code partyType} names (customers or
 * suppliers) — Postgres can't put a real foreign key on a column that
 * targets one of two different tables depending on a discriminator, so
 * referential integrity for partyId is enforced in
 * {@code PartyPropertyAccessService}, not the database.
 */
@Entity
@Table(name = "party_property_access")
@Getter
@Setter
@NoArgsConstructor
public class PartyPropertyAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "party_type", nullable = false, length = 10)
    private PartyType partyType;

    @Column(name = "party_id", nullable = false)
    private Long partyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
