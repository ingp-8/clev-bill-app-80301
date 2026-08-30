import { apiClient } from './client'

export interface Client {
  id: number
  clientName: string
  address: string | null
  email: string | null
  mobileNo: string | null
  logoPath: string | null
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface ClientRequest {
  clientName: string
  address: string | null
  email: string | null
  mobileNo: string | null
  logoPath: string | null
  active: boolean
}

export const clientsApi = {
  list: async (): Promise<Client[]> => (await apiClient.get<Client[]>('/v1/clients')).data,
  get: async (id: number): Promise<Client> => (await apiClient.get<Client>(`/v1/clients/${id}`)).data,
  create: async (request: ClientRequest): Promise<Client> => (await apiClient.post<Client>('/v1/clients', request)).data,
  update: async (id: number, request: ClientRequest): Promise<Client> =>
    (await apiClient.put<Client>(`/v1/clients/${id}`, request)).data,
  remove: async (id: number): Promise<void> => {
    await apiClient.delete(`/v1/clients/${id}`)
  },
}
