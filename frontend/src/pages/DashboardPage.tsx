import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { useSystemConfig } from '../hooks/useSystemConfig'

export function DashboardPage() {
  const { user, logout } = useAuth()
  const { data: config } = useSystemConfig()

  return (
    <div style={{ maxWidth: 640, margin: '40px auto', textAlign: 'left' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h1 style={{ fontSize: 28, margin: 0 }}>{config?.storeName ?? 'clevbill'}</h1>
        <button onClick={logout}>Sign out</button>
      </div>
      <p>
        Signed in as {user?.fullName} ({user?.role})
      </p>
      <p>
        <Link to="/checkout">Checkout &rarr;</Link>
      </p>
      <p>
        <Link to="/masters">Manage masters &rarr;</Link>
      </p>
      <p>
        <Link to="/inventory">Inventory &rarr;</Link>
      </p>
      <p>
        <Link to="/reports">Reports &rarr;</Link>
      </p>
      <p>
        <Link to="/settings">Business settings &rarr;</Link>
      </p>
    </div>
  )
}
