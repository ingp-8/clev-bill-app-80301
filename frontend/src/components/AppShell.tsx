import type { ReactNode } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { useProperty } from '../property/PropertyContext'

interface NavItem {
  to: string
  label: string
  moduleCode: string
}

const NAV_ITEMS: NavItem[] = [
  { to: '/checkout', label: 'Checkout', moduleCode: 'BILLING' },
  { to: '/masters/categories', label: 'Masters', moduleCode: 'MASTERS_CATEGORY' },
  { to: '/inventory', label: 'Inventory', moduleCode: 'INVENTORY' },
  { to: '/reports', label: 'Reports', moduleCode: 'REPORTS' },
  { to: '/admin/properties', label: 'Properties', moduleCode: 'PROPERTY_MGMT' },
  { to: '/admin/clients', label: 'Clients', moduleCode: 'CLIENT_MGMT' },
  { to: '/admin/users', label: 'Users', moduleCode: 'USER_MGMT' },
  { to: '/admin/roles', label: 'Roles', moduleCode: 'ROLE_MGMT' },
]

export function AppShell({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth()
  const { properties, activeProperty, setActivePropertyId, hasPermission } = useProperty()
  const navigate = useNavigate()

  const visibleItems = NAV_ITEMS.filter((item) => hasPermission(item.moduleCode, 'VIEW'))

  function handleLogout() {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <header
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          padding: '10px 20px',
          borderBottom: '1px solid var(--border)',
          gap: 16,
          flexWrap: 'wrap',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
          <strong style={{ fontSize: 18 }}>Clevbill</strong>
          {properties.length > 1 ? (
            <select
              value={activeProperty?.id ?? ''}
              onChange={(e) => setActivePropertyId(Number(e.target.value))}
              style={{ padding: 6 }}
            >
              {properties.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.propertyName}
                </option>
              ))}
            </select>
          ) : (
            activeProperty && <span style={{ color: 'var(--text)' }}>{activeProperty.propertyName}</span>
          )}
        </div>

        <nav style={{ display: 'flex', gap: 14, flexWrap: 'wrap' }}>
          {visibleItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              style={({ isActive }) => ({
                textDecoration: 'none',
                color: 'var(--text-h)',
                fontWeight: isActive ? 700 : 400,
              })}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <span style={{ fontSize: 13, color: 'var(--text)' }}>
            {user?.fullName} ({user?.roles.join(', ')})
          </span>
          <button type="button" onClick={handleLogout}>
            Sign out
          </button>
        </div>
      </header>

      <main style={{ flex: 1 }}>{children}</main>
    </div>
  )
}
