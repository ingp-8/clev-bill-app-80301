CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    role_name   VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    -- System roles (seeded below) can't be renamed or deleted via the API —
    -- application logic keys off role_name for the SUPER bypass, so it must
    -- stay stable. Admin-created roles are ordinary editable rows.
    is_system   BOOLEAN NOT NULL DEFAULT FALSE,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at  TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc')
);

CREATE TABLE permissions (
    id          BIGSERIAL PRIMARY KEY,
    module_code VARCHAR(50) NOT NULL,
    action      VARCHAR(20) NOT NULL,
    description VARCHAR(200),
    CONSTRAINT uq_permissions_module_action UNIQUE (module_code, action)
);

CREATE TABLE role_permissions (
    role_id       BIGINT NOT NULL REFERENCES roles(id),
    permission_id BIGINT NOT NULL REFERENCES permissions(id),
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id),
    role_id BIGINT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

-- Which properties a user may operate against — a separate dimension from
-- roles (roles say *what* a user can do; this says *where*). SUPER-role
-- users bypass this check entirely in application logic rather than
-- needing a row per property.
CREATE TABLE user_property_access (
    user_id     BIGINT NOT NULL REFERENCES users(id),
    property_id BIGINT NOT NULL REFERENCES properties(id),
    client_id   BIGINT NOT NULL REFERENCES clients(id),
    PRIMARY KEY (user_id, property_id)
);

CREATE INDEX idx_user_property_access_property_id ON user_property_access(property_id);

INSERT INTO roles (role_name, description, is_system) VALUES
    ('Super Admin', 'Full access to every module, every client and property — nothing scoped', TRUE),
    ('Store Manager', 'Manages masters, transactions, and reports for their assigned properties', TRUE),
    ('Operator', 'Billing screen only — no access to masters, reports, or administration', TRUE);

INSERT INTO permissions (module_code, action)
SELECT module_code, action
FROM (VALUES
    ('CLIENT_MGMT'), ('PROPERTY_MGMT'), ('POS_MGMT'), ('USER_MGMT'), ('ROLE_MGMT'),
    ('MASTERS_CATEGORY'), ('MASTERS_BRAND'), ('MASTERS_TAX_RATE'), ('MASTERS_ITEM'),
    ('MASTERS_PRICE_LIST'), ('MASTERS_CUSTOMER'), ('MASTERS_SUPPLIER'),
    ('BILLING'), ('RETURNS'), ('INVENTORY'), ('REPORTS')
) AS modules(module_code)
CROSS JOIN (VALUES ('VIEW'), ('CREATE'), ('EDIT'), ('DELETE')) AS actions(action);

-- Super Admin: every permission.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p WHERE r.role_name = 'Super Admin';

-- Store Manager: everything except client/property/POS infrastructure and
-- user/role administration.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.role_name = 'Store Manager'
  AND p.module_code NOT IN ('CLIENT_MGMT', 'PROPERTY_MGMT', 'POS_MGMT', 'USER_MGMT', 'ROLE_MGMT');

-- Operator: view and create on Billing and Returns only.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.role_name = 'Operator'
  AND p.module_code IN ('BILLING', 'RETURNS')
  AND p.action IN ('VIEW', 'CREATE');

-- Migrate the old ADMIN/MANAGER/CASHIER enum on users into the new model.
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON
    (u.role = 'ADMIN' AND r.role_name = 'Super Admin') OR
    (u.role = 'MANAGER' AND r.role_name = 'Store Manager') OR
    (u.role = 'CASHIER' AND r.role_name = 'Operator');

INSERT INTO user_property_access (user_id, property_id, client_id)
SELECT u.id, p.id, p.client_id FROM users u CROSS JOIN properties p;

ALTER TABLE users DROP COLUMN role;
