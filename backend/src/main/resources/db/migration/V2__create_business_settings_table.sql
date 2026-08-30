CREATE TABLE business_settings (
    id                    BIGINT PRIMARY KEY DEFAULT 1,
    store_name            VARCHAR(150) NOT NULL,
    gstin                 VARCHAR(15),
    address               VARCHAR(300),
    invoice_series_prefix VARCHAR(10) NOT NULL,
    default_cgst_rate     DECIMAL(5,2) NOT NULL DEFAULT 0,
    default_sgst_rate     DECIMAL(5,2) NOT NULL DEFAULT 0,
    default_igst_rate     DECIMAL(5,2) NOT NULL DEFAULT 0,
    updated_at            TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    CONSTRAINT business_settings_single_row CHECK (id = 1)
);

INSERT INTO business_settings (id, store_name, gstin, address, invoice_series_prefix)
VALUES (1, 'Clevbill Store', NULL, NULL, 'CB');
