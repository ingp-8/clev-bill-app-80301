import type { ReactNode } from 'react'
import { useProperty } from '../property/PropertyContext'
import { EmptyState, LoadingState } from './EmptyState'

/**
 * Blocks rendering until the active property is resolved. Also the single
 * place that explains "no property access" — every property-scoped screen
 * sits behind this instead of re-deriving the same three states itself.
 */
export function PropertyGate({ children }: { children: ReactNode }) {
  const { isLoading, properties, activeProperty } = useProperty()

  if (isLoading) {
    return <LoadingState />
  }

  if (properties.length === 0) {
    return (
      <EmptyState
        title="No property access"
        description="Your account isn't assigned to any property yet. Contact an administrator."
      />
    )
  }

  if (!activeProperty) {
    return <LoadingState />
  }

  return <>{children}</>
}
