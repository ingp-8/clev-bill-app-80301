CREATE TABLE items (
    id            BIGSERIAL PRIMARY KEY,
    sku           VARCHAR(50) NOT NULL UNIQUE,
    barcode       VARCHAR(50) UNIQUE,
    name          VARCHAR(200) NOT NULL,
    category_id   BIGINT REFERENCES categories(id),
    brand_id      BIGINT REFERENCES brands(id),
    tax_rate_id   BIGINT NOT NULL REFERENCES tax_rates(id),
    hsn_code      VARCHAR(10),
    unit          VARCHAR(10) NOT NULL,
    selling_price DECIMAL(12,2) NOT NULL,
    cost_price    DECIMAL(12,2),
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at    TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc')
);

CREATE INDEX idx_items_category_id ON items(category_id);
CREATE INDEX idx_items_brand_id ON items(brand_id);
