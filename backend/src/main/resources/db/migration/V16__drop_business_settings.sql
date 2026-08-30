-- Superseded by Property (gstin, invoice_series_prefix, default tax rates,
-- e_invoice_enabled) and Client (store name/address) — see V11, which
-- already migrated this table's one row into the new masters.
DROP TABLE business_settings;
