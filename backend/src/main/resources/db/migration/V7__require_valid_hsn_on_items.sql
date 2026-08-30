UPDATE items SET hsn_code = '2202' WHERE hsn_code IS NULL;

ALTER TABLE items ALTER COLUMN hsn_code SET NOT NULL;
ALTER TABLE items ADD CONSTRAINT chk_items_hsn_code_format CHECK (hsn_code ~ '^([0-9]{4}|[0-9]{6}|[0-9]{8})$');
