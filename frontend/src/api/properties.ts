import { apiClient } from './client'

export interface Property {
  id: number
  clientId: number
  clientName: string
  propertyName: string
  address: string | null
  gstin: string | null
  invoiceSeriesPrefix: string
  defaultCgstRate: number
  defaultSgstRate: number
  defaultIgstRate: number
  eInvoiceEnabled: boolean
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface PropertyRequest {
  propertyName: string
  address: string | null
  gstin: string | null
  invoiceSeriesPrefix: string
  defaultCgstRate: number
  defaultSgstRate: number
  defaultIgstRate: number
  eInvoiceEnabled: boolean
  active: boolean
}

export const propertiesApi = {
  listByClient: async (clientId: number): Promise<Property[]> =>
    (await apiClient.get<Property[]>(`/v1/clients/${clientId}/properties`)).data,
  get: async (id: number): Promise<Property> => (await apiClient.get<Property>(`/v1/properties/${id}`)).data,
  create: async (clientId: number, request: PropertyRequest): Promise<Property> =>
    (await apiClient.post<Property>(`/v1/clients/${clientId}/properties`, request)).data,
  update: async (id: number, request: PropertyRequest): Promise<Property> =>
    (await apiClient.put<Property>(`/v1/properties/${id}`, request)).data,
  remove: async (id: number): Promise<void> => {
    await apiClient.delete(`/v1/properties/${id}`)
  },
}
