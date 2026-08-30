CREATE TABLE clients (
    id          BIGSERIAL PRIMARY KEY,
    client_name VARCHAR(150) NOT NULL,
    address     VARCHAR(300),
    email       VARCHAR(150),
    mobile_no   VARCHAR(15),
    logo_path   VARCHAR(300),
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at  TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc')
);

CREATE TABLE properties (
    id                    BIGSERIAL PRIMARY KEY,
    client_id             BIGINT NOT NULL REFERENCES clients(id),
    property_name         VARCHAR(150) NOT NULL,
    address               VARCHAR(300),
    gstin                 VARCHAR(15),
    invoice_series_prefix VARCHAR(10) NOT NULL DEFAULT 'INV',
    default_cgst_rate     DECIMAL(5,2) NOT NULL DEFAULT 0,
    default_sgst_rate     DECIMAL(5,2) NOT NULL DEFAULT 0,
    default_igst_rate     DECIMAL(5,2) NOT NULL DEFAULT 0,
    e_invoice_enabled     BOOLEAN NOT NULL DEFAULT FALSE,
    active                BOOLEAN NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at            TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc')
);

CREATE INDEX idx_properties_client_id ON properties(client_id);

CREATE TABLE pos_terminals (
    id          BIGSERIAL PRIMARY KEY,
    property_id BIGINT NOT NULL REFERENCES properties(id),
    client_id   BIGINT NOT NULL REFERENCES clients(id),
    pos_name    VARCHAR(100) NOT NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at  TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc')
);

CREATE INDEX idx_pos_terminals_property_id ON pos_terminals(property_id);
CREATE INDEX idx_pos_terminals_client_id ON pos_terminals(client_id);

-- Seed one Client/Property/POS from the existing (soon to be dropped, see
-- V16) Business Settings row, so every table we're about to scope by
-- property in the following migrations has somewhere to attach.
-- business_settings always has exactly one row (id=1, CHECK-enforced) by
-- the time this runs, on a fresh install or an existing one alike.
INSERT INTO clients (client_name, address)
SELECT store_name, address FROM business_settings WHERE id = 1;

INSERT INTO properties (
    client_id, property_name, address, gstin, invoice_series_prefix,
    default_cgst_rate, default_sgst_rate, default_igst_rate, e_invoice_enabled
)
SELECT c.id, bs.store_name, bs.address, bs.gstin, bs.invoice_series_prefix,
       bs.default_cgst_rate, bs.default_sgst_rate, bs.default_igst_rate, bs.e_invoice_enabled
FROM clients c CROSS JOIN business_settings bs
WHERE bs.id = 1;

INSERT INTO pos_terminals (property_id, client_id, pos_name)
SELECT p.id, p.client_id, 'Main Counter' FROM properties p;
