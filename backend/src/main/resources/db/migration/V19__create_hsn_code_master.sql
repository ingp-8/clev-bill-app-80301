CREATE TABLE hsn_codes (
    id          BIGSERIAL PRIMARY KEY,
    property_id BIGINT NOT NULL REFERENCES properties(id),
    client_id   BIGINT NOT NULL REFERENCES clients(id),
    code        VARCHAR(10) NOT NULL,
    description VARCHAR(200),
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at  TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    CONSTRAINT uq_hsn_codes_property_code UNIQUE (property_id, code),
    CONSTRAINT chk_hsn_codes_format CHECK (code ~ '^([0-9]{4}|[0-9]{6}|[0-9]{8})$')
);

CREATE INDEX idx_hsn_codes_property_id ON hsn_codes(property_id);

-- Backfill one hsn_codes row per distinct (property, code) already used on an item.
INSERT INTO hsn_codes (property_id, client_id, code, active, created_at, updated_at)
SELECT DISTINCT i.property_id, i.client_id, i.hsn_code, TRUE, now() AT TIME ZONE 'utc', now() AT TIME ZONE 'utc'
FROM items i;

ALTER TABLE items ADD COLUMN hsn_code_id BIGINT REFERENCES hsn_codes(id);

UPDATE items i
SET hsn_code_id = h.id
FROM hsn_codes h
WHERE h.property_id = i.property_id AND h.code = i.hsn_code;

ALTER TABLE items ALTER COLUMN hsn_code_id SET NOT NULL;
ALTER TABLE items DROP CONSTRAINT chk_items_hsn_code_format;
ALTER TABLE items DROP COLUMN hsn_code;
