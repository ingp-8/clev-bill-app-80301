package com.clevstack.clevbill.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Derived/cached stock level per item — the {@code inventory_transactions}
 * ledger is the source of truth (see docs/ARCHITECTURE.md section 5). This
 * row is only ever adjusted by {@code InventoryService.applyChange}, never
 * written directly.
 */
@Entity
@Table(name = "inventories")
@Getter
@Setter
@NoArgsConstructor
public class Inventory {

    @Id
    @Column(name = "item_id")
    private Long itemId;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
