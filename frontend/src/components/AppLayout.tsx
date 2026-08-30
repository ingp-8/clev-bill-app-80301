import { Outlet } from 'react-router-dom'
import { AppShell } from './AppShell'
import { ProtectedRoute } from './ProtectedRoute'
import { PropertyGate } from './PropertyGate'
import { PropertyProvider } from '../property/PropertyContext'

export function AppLayout() {
  return (
    <ProtectedRoute>
      <PropertyProvider>
        <PropertyGate>
          <AppShell>
            <Outlet />
          </AppShell>
        </PropertyGate>
      </PropertyProvider>
    </ProtectedRoute>
  )
}
