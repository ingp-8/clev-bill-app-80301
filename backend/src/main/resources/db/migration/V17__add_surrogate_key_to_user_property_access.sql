-- role_permissions and user_roles stay plain composite-key join tables
-- (transparent JPA @ManyToMany, no extra columns). user_property_access
-- carries an extra client_id column, so it's mapped as its own entity —
-- give it a normal surrogate key instead of a composite one.
ALTER TABLE user_property_access DROP CONSTRAINT user_property_access_pkey;
ALTER TABLE user_property_access ADD COLUMN id BIGSERIAL PRIMARY KEY;
ALTER TABLE user_property_access ADD CONSTRAINT uq_user_property_access UNIQUE (user_id, property_id);
