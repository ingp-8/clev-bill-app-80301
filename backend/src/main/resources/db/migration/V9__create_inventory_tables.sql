CREATE TABLE inventories (
    item_id    BIGINT PRIMARY KEY REFERENCES items(id),
    quantity   DECIMAL(12,3) NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc')
);

CREATE TABLE inventory_transactions (
    id              BIGSERIAL PRIMARY KEY,
    item_id         BIGINT NOT NULL REFERENCES items(id),
    change_quantity DECIMAL(12,3) NOT NULL,
    reason          VARCHAR(20) NOT NULL,
    reference_id    BIGINT,
    note            VARCHAR(300),
    created_by      BIGINT REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc')
);

CREATE INDEX idx_inventory_transactions_item_id ON inventory_transactions(item_id);
