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
    <div className="page">
      <nav className="pill-tabs">
        {TABS.map((tab) => (
          <NavLink key={tab.to} to={tab.to} className={({ isActive }) => (isActive ? 'active' : undefined)}>
            {tab.label}
          </NavLink>
        ))}
      </nav>
      <Outlet />
    </div>
  )
}
