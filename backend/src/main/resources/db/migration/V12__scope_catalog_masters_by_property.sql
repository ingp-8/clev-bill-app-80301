-- Categories
ALTER TABLE categories ADD COLUMN property_id BIGINT REFERENCES properties(id);
ALTER TABLE categories ADD COLUMN client_id BIGINT REFERENCES clients(id);
UPDATE categories SET property_id = (SELECT id FROM properties LIMIT 1), client_id = (SELECT client_id FROM properties LIMIT 1);
ALTER TABLE categories ALTER COLUMN property_id SET NOT NULL;
ALTER TABLE categories ALTER COLUMN client_id SET NOT NULL;
ALTER TABLE categories DROP CONSTRAINT categories_name_key;
ALTER TABLE categories ADD CONSTRAINT uq_categories_property_name UNIQUE (property_id, name);
CREATE INDEX idx_categories_property_id ON categories(property_id);

-- Brands
ALTER TABLE brands ADD COLUMN property_id BIGINT REFERENCES properties(id);
ALTER TABLE brands ADD COLUMN client_id BIGINT REFERENCES clients(id);
UPDATE brands SET property_id = (SELECT id FROM properties LIMIT 1), client_id = (SELECT client_id FROM properties LIMIT 1);
ALTER TABLE brands ALTER COLUMN property_id SET NOT NULL;
ALTER TABLE brands ALTER COLUMN client_id SET NOT NULL;
ALTER TABLE brands DROP CONSTRAINT brands_name_key;
ALTER TABLE brands ADD CONSTRAINT uq_brands_property_name UNIQUE (property_id, name);
CREATE INDEX idx_brands_property_id ON brands(property_id);

-- Tax rates
ALTER TABLE tax_rates ADD COLUMN property_id BIGINT REFERENCES properties(id);
ALTER TABLE tax_rates ADD COLUMN client_id BIGINT REFERENCES clients(id);
UPDATE tax_rates SET property_id = (SELECT id FROM properties LIMIT 1), client_id = (SELECT client_id FROM properties LIMIT 1);
ALTER TABLE tax_rates ALTER COLUMN property_id SET NOT NULL;
ALTER TABLE tax_rates ALTER COLUMN client_id SET NOT NULL;
ALTER TABLE tax_rates DROP CONSTRAINT tax_rates_name_key;
ALTER TABLE tax_rates ADD CONSTRAINT uq_tax_rates_property_name UNIQUE (property_id, name);
CREATE INDEX idx_tax_rates_property_id ON tax_rates(property_id);

-- Items
ALTER TABLE items ADD COLUMN property_id BIGINT REFERENCES properties(id);
ALTER TABLE items ADD COLUMN client_id BIGINT REFERENCES clients(id);
UPDATE items SET property_id = (SELECT id FROM properties LIMIT 1), client_id = (SELECT client_id FROM properties LIMIT 1);
ALTER TABLE items ALTER COLUMN property_id SET NOT NULL;
ALTER TABLE items ALTER COLUMN client_id SET NOT NULL;
ALTER TABLE items DROP CONSTRAINT items_sku_key;
ALTER TABLE items DROP CONSTRAINT items_barcode_key;
ALTER TABLE items ADD CONSTRAINT uq_items_property_sku UNIQUE (property_id, sku);
ALTER TABLE items ADD CONSTRAINT uq_items_property_barcode UNIQUE (property_id, barcode);
CREATE INDEX idx_items_property_id ON items(property_id);

-- Price lists
ALTER TABLE price_lists ADD COLUMN property_id BIGINT REFERENCES properties(id);
ALTER TABLE price_lists ADD COLUMN client_id BIGINT REFERENCES clients(id);
UPDATE price_lists SET property_id = (SELECT id FROM properties LIMIT 1), client_id = (SELECT client_id FROM properties LIMIT 1);
ALTER TABLE price_lists ALTER COLUMN property_id SET NOT NULL;
ALTER TABLE price_lists ALTER COLUMN client_id SET NOT NULL;
ALTER TABLE price_lists DROP CONSTRAINT price_lists_name_key;
ALTER TABLE price_lists ADD CONSTRAINT uq_price_lists_property_name UNIQUE (property_id, name);
CREATE INDEX idx_price_lists_property_id ON price_lists(property_id);
