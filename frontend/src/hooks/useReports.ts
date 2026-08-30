import { useQuery } from '@tanstack/react-query'
import { reportsApi, type ReportFilters } from '../api/reports'

export function useSalesSummary(propertyId: number, filters: ReportFilters) {
  return useQuery({
    queryKey: ['reports', propertyId, 'summary', filters],
    queryFn: () => reportsApi.summary(propertyId, filters),
    enabled: Number.isFinite(propertyId),
  })
}

export function useSalesByItem(propertyId: number, filters: ReportFilters) {
  return useQuery({
    queryKey: ['reports', propertyId, 'by-item', filters],
    queryFn: () => reportsApi.byItem(propertyId, filters),
    enabled: Number.isFinite(propertyId),
  })
}

export function useSalesByDay(propertyId: number, filters: ReportFilters) {
  return useQuery({
    queryKey: ['reports', propertyId, 'by-day', filters],
    queryFn: () => reportsApi.byDay(propertyId, filters),
    enabled: Number.isFinite(propertyId),
  })
}
