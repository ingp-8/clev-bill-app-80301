import type { ReactNode } from 'react'
import { useProperty } from '../property/PropertyContext'

/**
 * Blocks rendering until the active property is resolved. Also the single
 * place that explains "no property access" — every property-scoped screen
 * sits behind this instead of re-deriving the same three states itself.
 */
export function PropertyGate({ children }: { children: ReactNode }) {
  const { isLoading, properties, activeProperty } = useProperty()

  if (isLoading) {
    return <p style={{ padding: 24 }}>Loading...</p>
  }

  if (properties.length === 0) {
    return (
      <div style={{ maxWidth: 480, margin: '80px auto', textAlign: 'center' }}>
        <h2>No property access</h2>
        <p>Your account isn't assigned to any property yet. Contact an administrator.</p>
      </div>
    )
  }

  if (!activeProperty) {
    return <p style={{ padding: 24 }}>Loading...</p>
  }

  return <>{children}</>
}
