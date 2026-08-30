import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import { ProtectedRoute } from './components/ProtectedRoute'
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
import { SettingsPage } from './pages/settings/SettingsPage'

const queryClient = new QueryClient()

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route
              path="/"
              element={
                <ProtectedRoute>
                  <DashboardPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/checkout"
              element={
                <ProtectedRoute>
                  <CheckoutPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/reports"
              element={
                <ProtectedRoute>
                  <ReportsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/inventory"
              element={
                <ProtectedRoute>
                  <InventoryPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/inventory/:itemId"
              element={
                <ProtectedRoute>
                  <InventoryHistoryPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/settings"
              element={
                <ProtectedRoute>
                  <SettingsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/masters"
              element={
                <ProtectedRoute>
                  <MastersLayout />
                </ProtectedRoute>
              }
            >
              <Route index element={<Navigate to="categories" replace />} />
              <Route path="categories" element={<CategoriesPage />} />
              <Route path="brands" element={<BrandsPage />} />
              <Route path="tax-rates" element={<TaxRatesPage />} />
              <Route path="items" element={<ItemsPage />} />
              <Route path="price-lists" element={<PriceListsPage />} />
              <Route path="price-lists/:priceListId" element={<PriceListDetailPage />} />
              <Route path="customers" element={<CustomersPage />} />
              <Route path="suppliers" element={<SuppliersPage />} />
            </Route>
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  )
}

export default App
