import { apiClient } from './client'

export interface SalesSummary {
  saleCount: number
  totalSubtotal: number
  totalTax: number
  totalRevenue: number
  averageSaleValue: number
}

export interface ItemSales {
  itemId: number
  sku: string
  name: string
  quantitySold: number
  revenue: number
}

export interface DailySales {
  date: string
  saleCount: number
  revenue: number
}

export interface ReportFilters {
  from?: string
  to?: string
  cashierId?: number
  categoryId?: number
  itemId?: number
}

function toParams(filters: ReportFilters) {
  const params: Record<string, string> = {}
  if (filters.from) params.from = filters.from
  if (filters.to) params.to = filters.to
  if (filters.cashierId) params.cashierId = String(filters.cashierId)
  if (filters.categoryId) params.categoryId = String(filters.categoryId)
  if (filters.itemId) params.itemId = String(filters.itemId)
  return params
}

export const reportsApi = {
  summary: async (filters: ReportFilters): Promise<SalesSummary> =>
    (await apiClient.get<SalesSummary>('/v1/reports/sales/summary', { params: toParams(filters) })).data,
  byItem: async (filters: ReportFilters): Promise<ItemSales[]> =>
    (await apiClient.get<ItemSales[]>('/v1/reports/sales/by-item', { params: toParams(filters) })).data,
  byDay: async (filters: ReportFilters): Promise<DailySales[]> =>
    (await apiClient.get<DailySales[]>('/v1/reports/sales/by-day', { params: toParams(filters) })).data,
}
