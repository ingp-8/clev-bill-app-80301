import { apiClient } from './client'

export interface PosTerminal {
  id: number
  propertyId: number
  propertyName: string
  posName: string
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface PosTerminalRequest {
  posName: string
  active: boolean
}

export const posTerminalsApi = {
  listByProperty: async (propertyId: number): Promise<PosTerminal[]> =>
    (await apiClient.get<PosTerminal[]>(`/v1/properties/${propertyId}/pos`)).data,
  create: async (propertyId: number, request: PosTerminalRequest): Promise<PosTerminal> =>
    (await apiClient.post<PosTerminal>(`/v1/properties/${propertyId}/pos`, request)).data,
  update: async (id: number, request: PosTerminalRequest): Promise<PosTerminal> =>
    (await apiClient.put<PosTerminal>(`/v1/pos/${id}`, request)).data,
  remove: async (id: number): Promise<void> => {
    await apiClient.delete(`/v1/pos/${id}`)
  },
}
