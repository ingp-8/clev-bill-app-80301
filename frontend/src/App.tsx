import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import { AppLayout } from './components/AppLayout'
import { PermissionGate } from './components/PermissionGate'
import { CheckoutPage } from './pages/checkout/CheckoutPage'
import { DashboardPage } from './pages/DashboardPage'
import { InventoryHistoryPage } from './pages/inventory/InventoryHistoryPage'
import { InventoryPage } from './pages/inventory/InventoryPage'
import { LoginPage } from './pages/LoginPage'
import { ReportsPage } from './pages/reports/ReportsPage'
import { BrandsPage } from './pages/masters/BrandsPage'
import { CategoriesPage } from './pages/masters/CategoriesPage'
import { CustomersPage } from './pages/masters/CustomersPage'
import { ItemsPage } from './pages/masters/ItemsPage'
import { MastersLayout } from './pages/masters/MastersLayout'
import { PriceListDetailPage } from './pages/masters/PriceListDetailPage'
import { PriceListsPage } from './pages/masters/PriceListsPage'
import { SuppliersPage } from './pages/masters/SuppliersPage'
import { TaxRatesPage } from './pages/masters/TaxRatesPage'
import { ClientsPage } from './pages/admin/ClientsPage'
import { PropertiesPage } from './pages/admin/PropertiesPage'
import { PosTerminalsPage } from './pages/admin/PosTerminalsPage'
import { RolesPage } from './pages/admin/RolesPage'
import { UsersPage } from './pages/admin/UsersPage'

const queryClient = new QueryClient()

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<LoginPage />} />

            <Route element={<AppLayout />}>
              <Route path="/" element={<DashboardPage />} />

              <Route
                path="/checkout"
                element={
                  <PermissionGate moduleCode="BILLING">
                    <CheckoutPage />
                  </PermissionGate>
                }
              />
              <Route
                path="/reports"
                element={
                  <PermissionGate moduleCode="REPORTS">
                    <ReportsPage />
                  </PermissionGate>
                }
              />
              <Route
                path="/inventory"
                element={
                  <PermissionGate moduleCode="INVENTORY">
                    <InventoryPage />
                  </PermissionGate>
                }
              />
              <Route
                path="/inventory/:itemId"
                element={
                  <PermissionGate moduleCode="INVENTORY">
                    <InventoryHistoryPage />
                  </PermissionGate>
                }
              />

              <Route path="/masters" element={<MastersLayout />}>
                <Route index element={<Navigate to="categories" replace />} />
                <Route
                  path="categories"
                  element={
                    <PermissionGate moduleCode="MASTERS_CATEGORY">
                      <CategoriesPage />
                    </PermissionGate>
                  }
                />
                <Route
                  path="brands"
                  element={
                    <PermissionGate moduleCode="MASTERS_BRAND">
                      <BrandsPage />
                    </PermissionGate>
                  }
                />
                <Route
                  path="tax-rates"
                  element={
                    <PermissionGate moduleCode="MASTERS_TAX_RATE">
                      <TaxRatesPage />
                    </PermissionGate>
                  }
                />
                <Route
                  path="items"
                  element={
                    <PermissionGate moduleCode="MASTERS_ITEM">
                      <ItemsPage />
                    </PermissionGate>
                  }
                />
                <Route
                  path="price-lists"
                  element={
                    <PermissionGate moduleCode="MASTERS_PRICE_LIST">
                      <PriceListsPage />
                    </PermissionGate>
                  }
                />
                <Route
                  path="price-lists/:priceListId"
                  element={
                    <PermissionGate moduleCode="MASTERS_PRICE_LIST">
                      <PriceListDetailPage />
                    </PermissionGate>
                  }
                />
                <Route
                  path="customers"
                  element={
                    <PermissionGate moduleCode="MASTERS_CUSTOMER">
                      <CustomersPage />
                    </PermissionGate>
                  }
                />
                <Route
                  path="suppliers"
                  element={
                    <PermissionGate moduleCode="MASTERS_SUPPLIER">
                      <SuppliersPage />
                    </PermissionGate>
                  }
                />
              </Route>

              <Route
                path="/admin/clients"
                element={
                  <PermissionGate moduleCode="CLIENT_MGMT">
                    <ClientsPage />
                  </PermissionGate>
                }
              />
              <Route
                path="/admin/properties"
                element={
                  <PermissionGate moduleCode="PROPERTY_MGMT">
                    <PropertiesPage />
                  </PermissionGate>
                }
              />
              <Route
                path="/admin/properties/:propertyId/pos"
                element={
                  <PermissionGate moduleCode="POS_MGMT">
                    <PosTerminalsPage />
                  </PermissionGate>
                }
              />
              <Route
                path="/admin/users"
                element={
                  <PermissionGate moduleCode="USER_MGMT">
                    <UsersPage />
                  </PermissionGate>
                }
              />
              <Route
                path="/admin/roles"
                element={
                  <PermissionGate moduleCode="ROLE_MGMT">
                    <RolesPage />
                  </PermissionGate>
                }
              />
            </Route>

            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  )
}

export default App
