import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useAuth } from '../auth/AuthContext'
import { meApi, type Me } from '../api/me'
import type { Property } from '../api/properties'

const ACTIVE_PROPERTY_KEY = 'clevbill.activePropertyId'

interface PropertyContextValue {
  me: Me | undefined
  properties: Property[]
  activeProperty: Property | null
  setActivePropertyId: (id: number) => void
  isLoading: boolean
  hasPermission: (moduleCode: string, action: string) => boolean
}

const PropertyContext = createContext<PropertyContextValue | undefined>(undefined)

export function PropertyProvider({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth()
  const queryClient = useQueryClient()

  const { data: me, isLoading: meLoading } = useQuery({
    queryKey: ['me'],
    queryFn: meApi.profile,
    enabled: isAuthenticated,
  })

  const { data: properties = [], isLoading: propertiesLoading } = useQuery({
    queryKey: ['me', 'properties'],
    queryFn: meApi.properties,
    enabled: isAuthenticated,
  })

  const [activePropertyId, setActivePropertyIdState] = useState<number | null>(() => {
    const stored = localStorage.getItem(ACTIVE_PROPERTY_KEY)
    return stored ? Number(stored) : null
  })

  useEffect(() => {
    if (properties.length === 0) return
    const stillValid = properties.some((p) => p.id === activePropertyId)
    if (!stillValid) {
      setActivePropertyIdState(properties[0].id)
    }
  }, [properties, activePropertyId])

  function setActivePropertyId(id: number) {
    localStorage.setItem(ACTIVE_PROPERTY_KEY, String(id))
    setActivePropertyIdState(id)
    queryClient.invalidateQueries()
  }

  const activeProperty = useMemo(
    () => properties.find((p) => p.id === activePropertyId) ?? null,
    [properties, activePropertyId],
  )

  function hasPermission(moduleCode: string, action: string): boolean {
    if (!me) return false
    if (me.superAdmin) return true
    return me.permissions.includes(`${moduleCode}:${action}`)
  }

  const value: PropertyContextValue = {
    me,
    properties,
    activeProperty,
    setActivePropertyId,
    isLoading: isAuthenticated && (meLoading || propertiesLoading),
    hasPermission,
  }

  return <PropertyContext.Provider value={value}>{children}</PropertyContext.Provider>
}

export function useProperty(): PropertyContextValue {
  const context = useContext(PropertyContext)
  if (!context) {
    throw new Error('useProperty must be used within a PropertyProvider')
  }
  return context
}
