INSERT INTO permissions (module_code, action)
SELECT 'MASTERS_HSN', action FROM (VALUES ('VIEW'), ('CREATE'), ('EDIT'), ('DELETE')) AS actions(action);

-- Super Admin and Store Manager get the same masters access as every other MASTERS_* module.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.role_name IN ('Super Admin', 'Store Manager') AND p.module_code = 'MASTERS_HSN';
