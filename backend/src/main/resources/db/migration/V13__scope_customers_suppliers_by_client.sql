ALTER TABLE customers ADD COLUMN client_id BIGINT REFERENCES clients(id);
UPDATE customers SET client_id = (SELECT id FROM clients LIMIT 1);
ALTER TABLE customers ALTER COLUMN client_id SET NOT NULL;
CREATE INDEX idx_customers_client_id ON customers(client_id);

ALTER TABLE suppliers ADD COLUMN client_id BIGINT REFERENCES clients(id);
UPDATE suppliers SET client_id = (SELECT id FROM clients LIMIT 1);
ALTER TABLE suppliers ALTER COLUMN client_id SET NOT NULL;
CREATE INDEX idx_suppliers_client_id ON suppliers(client_id);

-- A customer or supplier can serve more than one property under the same
-- client. party_type + party_id together identify the customer/supplier
-- row; Postgres can't put a real FK on a column that points at one of two
-- different tables depending on a discriminator, so referential integrity
-- for party_id is enforced in the service layer instead (see PartyPropertyAccessService).
CREATE TABLE party_property_access (
    id          BIGSERIAL PRIMARY KEY,
    party_type  VARCHAR(10) NOT NULL,
    party_id    BIGINT NOT NULL,
    property_id BIGINT NOT NULL REFERENCES properties(id),
    client_id   BIGINT NOT NULL REFERENCES clients(id),
    created_at  TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    CONSTRAINT uq_party_property UNIQUE (party_type, party_id, property_id)
);

CREATE INDEX idx_party_property_access_party ON party_property_access(party_type, party_id);
CREATE INDEX idx_party_property_access_property_id ON party_property_access(property_id);

-- Existing customers/suppliers (there's only ever test data at this point)
-- get access to every property under their client, so nothing goes dark.
INSERT INTO party_property_access (party_type, party_id, property_id, client_id)
SELECT 'CUSTOMER', c.id, p.id, p.client_id
FROM customers c JOIN properties p ON p.client_id = c.client_id;

INSERT INTO party_property_access (party_type, party_id, property_id, client_id)
SELECT 'SUPPLIER', s.id, p.id, p.client_id
FROM suppliers s JOIN properties p ON p.client_id = s.client_id;
