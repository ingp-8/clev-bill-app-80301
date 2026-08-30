import { apiClient } from './client'

export interface SystemConfig {
  storeName: string
  gstin: string | null
  address: string | null
  invoiceSeriesPrefix: string
  defaultCgstRate: number
  defaultSgstRate: number
  defaultIgstRate: number
  eInvoiceEnabled: boolean
}

export type SystemConfigRequest = SystemConfig

export async function fetchSystemConfig(): Promise<SystemConfig> {
  const { data } = await apiClient.get<SystemConfig>('/system/config')
  return data
}

export async function updateSystemConfig(request: SystemConfigRequest): Promise<SystemConfig> {
  const { data } = await apiClient.put<SystemConfig>('/system/config', request)
  return data
}
