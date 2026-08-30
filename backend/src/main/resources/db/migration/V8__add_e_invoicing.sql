ALTER TABLE business_settings ADD COLUMN e_invoice_enabled BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE e_invoices (
    id              BIGSERIAL PRIMARY KEY,
    sale_id         BIGINT NOT NULL UNIQUE REFERENCES sales(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    irn             VARCHAR(64),
    signed_qr_code  TEXT,
    attempts        INT NOT NULL DEFAULT 0,
    last_error      VARCHAR(500),
    last_attempt_at TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at      TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc')
);

CREATE INDEX idx_e_invoices_status ON e_invoices(status);
