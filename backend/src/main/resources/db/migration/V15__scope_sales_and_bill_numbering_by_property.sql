ALTER TABLE sales ADD COLUMN property_id BIGINT REFERENCES properties(id);
ALTER TABLE sales ADD COLUMN client_id BIGINT REFERENCES clients(id);
ALTER TABLE sales ADD COLUMN pos_id BIGINT REFERENCES pos_terminals(id);

UPDATE sales SET
    property_id = (SELECT id FROM properties LIMIT 1),
    client_id = (SELECT client_id FROM properties LIMIT 1),
    pos_id = (SELECT id FROM pos_terminals LIMIT 1);

ALTER TABLE sales ALTER COLUMN property_id SET NOT NULL;
ALTER TABLE sales ALTER COLUMN client_id SET NOT NULL;
ALTER TABLE sales ALTER COLUMN pos_id SET NOT NULL;

CREATE INDEX idx_sales_property_id ON sales(property_id);
CREATE INDEX idx_sales_pos_id ON sales(pos_id);

-- Bill number sequence resets per property now, not globally — a bill
-- numbered CB-2026-27-00001 at Property A and another at Property B are
-- both legitimate first-of-year bills, not a collision.
ALTER TABLE invoice_counters ADD COLUMN property_id BIGINT REFERENCES properties(id);
UPDATE invoice_counters SET property_id = (SELECT id FROM properties LIMIT 1);
ALTER TABLE invoice_counters ALTER COLUMN property_id SET NOT NULL;
ALTER TABLE invoice_counters DROP CONSTRAINT invoice_counters_pkey;
ALTER TABLE invoice_counters ADD PRIMARY KEY (property_id, financial_year);
