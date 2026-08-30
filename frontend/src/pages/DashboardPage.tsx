import { useAuth } from '../auth/AuthContext'
import { useProperty } from '../property/PropertyContext'

export function DashboardPage() {
  const { user } = useAuth()
  const { activeProperty, me } = useProperty()

  return (
    <div className="page page-narrow">
      <div className="page-header">
        <h1>{activeProperty?.propertyName ?? 'Clevbill'}</h1>
        <p className="sub">
          Signed in as {user?.fullName} ({me?.superAdmin ? 'Super Admin' : user?.roles.join(', ')})
        </p>
      </div>
      <p style={{ color: 'var(--text)' }}>Use the navigation above to get started.</p>
    </div>
  )
}
