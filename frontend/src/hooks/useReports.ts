import { useQuery } from '@tanstack/react-query'
import { reportsApi, type ReportFilters } from '../api/reports'

export function useSalesSummary(filters: ReportFilters) {
  return useQuery({ queryKey: ['reports', 'summary', filters], queryFn: () => reportsApi.summary(filters) })
}

export function useSalesByItem(filters: ReportFilters) {
  return useQuery({ queryKey: ['reports', 'by-item', filters], queryFn: () => reportsApi.byItem(filters) })
}

export function useSalesByDay(filters: ReportFilters) {
  return useQuery({ queryKey: ['reports', 'by-day', filters], queryFn: () => reportsApi.byDay(filters) })
}
