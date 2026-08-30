import type { ReactNode } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { useProperty } from '../property/PropertyContext'
import { NavIcon } from './icons'
import './AppShell.css'

interface NavItem {
  to: string
  label: string
  moduleCode: string
}

const NAV_ITEMS: NavItem[] = [
  { to: '/checkout', label: 'Checkout', moduleCode: 'BILLING' },
  { to: '/masters', label: 'Masters', moduleCode: 'MASTERS_CATEGORY' },
  { to: '/inventory', label: 'Inventory', moduleCode: 'INVENTORY' },
  { to: '/reports', label: 'Reports', moduleCode: 'REPORTS' },
  { to: '/admin/properties', label: 'Properties', moduleCode: 'PROPERTY_MGMT' },
  { to: '/admin/pos', label: 'POS', moduleCode: 'POS_MGMT' },
  { to: '/admin/clients', label: 'Clients', moduleCode: 'CLIENT_MGMT' },
  { to: '/admin/users', label: 'Users', moduleCode: 'USER_MGMT' },
  { to: '/admin/roles', label: 'Roles', moduleCode: 'ROLE_MGMT' },
]

function initials(fullName: string | undefined): string {
  if (!fullName) return '?'
  return fullName
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join('')
}

export function AppShell({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth()
  const { properties, activeProperty, setActivePropertyId, hasPermission } = useProperty()
  const navigate = useNavigate()

  const visibleItems = NAV_ITEMS.filter((item) => hasPermission(item.moduleCode, 'VIEW'))

  async function handleLogout() {
    await logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="shell">
      <header className="shell-header">
        <div className="shell-brandrow">
          <span className="shell-brand">Clevbill</span>
          {properties.length > 1 ? (
            <select
              className="shell-propswitch"
              value={activeProperty?.id ?? ''}
              onChange={(e) => setActivePropertyId(Number(e.target.value))}
            >
              {properties.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.propertyName}
                </option>
              ))}
            </select>
          ) : (
            activeProperty && <span className="shell-propswitch">{activeProperty.propertyName}</span>
          )}
        </div>

        <nav className="shell-nav">
          {visibleItems.map((item) => (
            <NavLink key={item.to} to={item.to} className={({ isActive }) => (isActive ? 'active' : undefined)}>
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="shell-user">
          <span className="shell-avatar">{initials(user?.fullName)}</span>
          <span className="shell-username">{user?.fullName}</span>
          <button type="button" className="shell-signout" onClick={handleLogout}>
            Sign out
          </button>
        </div>
      </header>

      <main className="shell-main">{children}</main>

      <nav className="shell-bottomtabs">
        {visibleItems.map((item) => (
          <NavLink key={item.to} to={item.to} className={({ isActive }) => (isActive ? 'active' : undefined)}>
            <NavIcon moduleCode={item.moduleCode} />
            <span className="lbl">{item.label}</span>
          </NavLink>
        ))}
      </nav>
    </div>
  )
}
