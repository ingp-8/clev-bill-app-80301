import { useAuth } from '../auth/AuthContext'
import { useProperty } from '../property/PropertyContext'

export function DashboardPage() {
  const { user } = useAuth()
  const { activeProperty, me } = useProperty()

  return (
    <div style={{ maxWidth: 640, margin: '40px auto', textAlign: 'left', padding: '0 16px' }}>
      <h1 style={{ fontSize: 28, margin: 0 }}>{activeProperty?.propertyName ?? 'Clevbill'}</h1>
      <p>
        Signed in as {user?.fullName} ({me?.superAdmin ? 'Super Admin' : user?.roles.join(', ')})
      </p>
      <p style={{ color: 'var(--text)' }}>Use the navigation above to get started.</p>
    </div>
  )
}
