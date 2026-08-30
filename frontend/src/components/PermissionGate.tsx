import type { ReactNode } from 'react'
import { useProperty } from '../property/PropertyContext'

/**
 * Hides a screen the current user has no VIEW permission for. This is a
 * UX convenience only — the backend enforces the same check server-side
 * on every request regardless of what this component renders.
 */
export function PermissionGate({
  moduleCode,
  action = 'VIEW',
  children,
}: {
  moduleCode: string
  action?: string
  children: ReactNode
}) {
  const { hasPermission } = useProperty()

  if (!hasPermission(moduleCode, action)) {
    return (
      <div style={{ maxWidth: 480, margin: '80px auto', textAlign: 'center' }}>
        <h2>Access denied</h2>
        <p>You don't have permission to view this page.</p>
      </div>
    )
  }

  return <>{children}</>
}
