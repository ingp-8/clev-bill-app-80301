-- created_by/updated_by on every auditable table, tracking who wrote a row.
-- Nullable throughout: rows inserted outside an authenticated request (Flyway
-- seed data, the bootstrap admin user) simply carry no auditor.

ALTER TABLE users ADD COLUMN created_by BIGINT REFERENCES users(id);
ALTER TABLE users ADD COLUMN updated_by BIGINT REFERENCES users(id);

ALTER TABLE clients ADD COLUMN created_by BIGINT REFERENCES users(id);
ALTER TABLE clients ADD COLUMN updated_by BIGINT REFERENCES users(id);

ALTER TABLE properties ADD COLUMN created_by BIGINT REFERENCES users(id);
ALTER TABLE properties ADD COLUMN updated_by BIGINT REFERENCES users(id);

ALTER TABLE pos_terminals ADD COLUMN created_by BIGINT REFERENCES users(id);
ALTER TABLE pos_terminals ADD COLUMN updated_by BIGINT REFERENCES users(id);

ALTER TABLE roles ADD COLUMN created_by BIGINT REFERENCES users(id);
ALTER TABLE roles ADD COLUMN updated_by BIGINT REFERENCES users(id);

ALTER TABLE categories ADD COLUMN created_by BIGINT REFERENCES users(id);
ALTER TABLE categories ADD COLUMN updated_by BIGINT REFERENCES users(id);

ALTER TABLE brands ADD COLUMN created_by BIGINT REFERENCES users(id);
ALTER TABLE brands ADD COLUMN updated_by BIGINT REFERENCES users(id);

ALTER TABLE tax_rates ADD COLUMN created_by BIGINT REFERENCES users(id);
ALTER TABLE tax_rates ADD COLUMN updated_by BIGINT REFERENCES users(id);

ALTER TABLE hsn_codes ADD COLUMN created_by BIGINT REFERENCES users(id);
ALTER TABLE hsn_codes ADD COLUMN updated_by BIGINT REFERENCES users(id);

ALTER TABLE items ADD COLUMN created_by BIGINT REFERENCES users(id);
ALTER TABLE items ADD COLUMN updated_by BIGINT REFERENCES users(id);

ALTER TABLE price_lists ADD COLUMN created_by BIGINT REFERENCES users(id);
ALTER TABLE price_lists ADD COLUMN updated_by BIGINT REFERENCES users(id);

ALTER TABLE price_list_items ADD COLUMN created_by BIGINT REFERENCES users(id);
ALTER TABLE price_list_items ADD COLUMN updated_by BIGINT REFERENCES users(id);

ALTER TABLE customers ADD COLUMN created_by BIGINT REFERENCES users(id);
ALTER TABLE customers ADD COLUMN updated_by BIGINT REFERENCES users(id);

ALTER TABLE suppliers ADD COLUMN created_by BIGINT REFERENCES users(id);
ALTER TABLE suppliers ADD COLUMN updated_by BIGINT REFERENCES users(id);

ALTER TABLE sales ADD COLUMN created_by BIGINT REFERENCES users(id);
ALTER TABLE sales ADD COLUMN updated_by BIGINT REFERENCES users(id);

ALTER TABLE sale_items ADD COLUMN created_by BIGINT REFERENCES users(id);
ALTER TABLE sale_items ADD COLUMN updated_by BIGINT REFERENCES users(id);

ALTER TABLE payments ADD COLUMN created_by BIGINT REFERENCES users(id);
ALTER TABLE payments ADD COLUMN updated_by BIGINT REFERENCES users(id);

-- "returns" already had a NOT NULL created_by (who processed the return, a
-- business relationship set by ReturnService, not a generic row auditor) —
-- rename it out of the way before adding the generic created_by/updated_by
-- pair every other table gets.
ALTER TABLE returns RENAME COLUMN created_by TO processed_by;
ALTER TABLE returns ADD COLUMN created_by BIGINT REFERENCES users(id);
ALTER TABLE returns ADD COLUMN updated_by BIGINT REFERENCES users(id);

ALTER TABLE return_items ADD COLUMN created_by BIGINT REFERENCES users(id);
ALTER TABLE return_items ADD COLUMN updated_by BIGINT REFERENCES users(id);

ALTER TABLE e_invoices ADD COLUMN created_by BIGINT REFERENCES users(id);
ALTER TABLE e_invoices ADD COLUMN updated_by BIGINT REFERENCES users(id);
