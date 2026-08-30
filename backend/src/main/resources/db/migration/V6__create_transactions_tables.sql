CREATE TABLE invoice_counters (
    financial_year TEXT PRIMARY KEY,
    next_number    BIGINT NOT NULL DEFAULT 1
);

CREATE TABLE sales (
    id               BIGSERIAL PRIMARY KEY,
    bill_number      VARCHAR(30) NOT NULL UNIQUE,
    customer_id      BIGINT REFERENCES customers(id),
    cashier_id       BIGINT NOT NULL REFERENCES users(id),
    subtotal_amount  DECIMAL(12,2) NOT NULL,
    tax_amount       DECIMAL(12,2) NOT NULL,
    total_amount     DECIMAL(12,2) NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at       TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc')
);

CREATE INDEX idx_sales_customer_id ON sales(customer_id);
CREATE INDEX idx_sales_created_at ON sales(created_at);

CREATE TABLE sale_items (
    id            BIGSERIAL PRIMARY KEY,
    sale_id       BIGINT NOT NULL REFERENCES sales(id),
    item_id       BIGINT NOT NULL REFERENCES items(id),
    sku_snapshot  VARCHAR(50) NOT NULL,
    name_snapshot VARCHAR(200) NOT NULL,
    quantity      DECIMAL(12,3) NOT NULL,
    unit_price    DECIMAL(12,2) NOT NULL,
    cgst_rate     DECIMAL(5,2) NOT NULL,
    sgst_rate     DECIMAL(5,2) NOT NULL,
    igst_rate     DECIMAL(5,2) NOT NULL,
    cgst_amount   DECIMAL(12,2) NOT NULL,
    sgst_amount   DECIMAL(12,2) NOT NULL,
    igst_amount   DECIMAL(12,2) NOT NULL,
    line_subtotal DECIMAL(12,2) NOT NULL,
    line_total    DECIMAL(12,2) NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at    TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc')
);

CREATE INDEX idx_sale_items_sale_id ON sale_items(sale_id);
CREATE INDEX idx_sale_items_item_id ON sale_items(item_id);

CREATE TABLE payments (
    id         BIGSERIAL PRIMARY KEY,
    sale_id    BIGINT NOT NULL REFERENCES sales(id),
    method     VARCHAR(20) NOT NULL,
    amount     DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc')
);

CREATE INDEX idx_payments_sale_id ON payments(sale_id);

CREATE TABLE returns (
    id           BIGSERIAL PRIMARY KEY,
    sale_id      BIGINT NOT NULL REFERENCES sales(id),
    reason       VARCHAR(300),
    total_amount DECIMAL(12,2) NOT NULL,
    created_by   BIGINT NOT NULL REFERENCES users(id),
    created_at   TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at   TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc')
);

CREATE INDEX idx_returns_sale_id ON returns(sale_id);

CREATE TABLE return_items (
    id            BIGSERIAL PRIMARY KEY,
    return_id     BIGINT NOT NULL REFERENCES returns(id),
    sale_item_id  BIGINT NOT NULL REFERENCES sale_items(id),
    quantity      DECIMAL(12,3) NOT NULL,
    amount        DECIMAL(12,2) NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at    TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc')
);

CREATE INDEX idx_return_items_return_id ON return_items(return_id);
CREATE INDEX idx_return_items_sale_item_id ON return_items(sale_item_id);
