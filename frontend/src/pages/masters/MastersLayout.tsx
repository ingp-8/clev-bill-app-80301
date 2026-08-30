import { NavLink, Outlet } from 'react-router-dom'

const TABS = [
  { to: '/masters/categories', label: 'Categories' },
  { to: '/masters/brands', label: 'Brands' },
  { to: '/masters/tax-rates', label: 'Tax Rates' },
  { to: '/masters/items', label: 'Items' },
  { to: '/masters/price-lists', label: 'Price Lists' },
  { to: '/masters/customers', label: 'Customers' },
  { to: '/masters/suppliers', label: 'Suppliers' },
]

export function MastersLayout() {
  return (
    <div style={{ maxWidth: 1000, margin: '0 auto', padding: '24px 16px', textAlign: 'left' }}>
      <nav style={{ display: 'flex', gap: 12, marginBottom: 24, borderBottom: '1px solid var(--border)', paddingBottom: 12 }}>
        {TABS.map((tab) => (
          <NavLink
            key={tab.to}
            to={tab.to}
            style={({ isActive }) => ({
              fontWeight: isActive ? 700 : 400,
              textDecoration: 'none',
              color: 'var(--text-h)',
            })}
          >
            {tab.label}
          </NavLink>
        ))}
      </nav>
      <Outlet />
    </div>
  )
}
