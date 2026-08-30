-- Bill numbers reset per property (see V15's note on invoice_counters) —
-- global uniqueness on bill_number was still enforced from before that
-- change landed.
ALTER TABLE sales DROP CONSTRAINT sales_bill_number_key;
ALTER TABLE sales ADD CONSTRAINT uq_sales_property_bill_number UNIQUE (property_id, bill_number);
