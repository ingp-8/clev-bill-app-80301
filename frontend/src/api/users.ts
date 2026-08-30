import { apiClient } from './client'
import type { MasterRef } from './masters'

export interface AppUser {
  id: number
  username: string
  fullName: string
  enabled: boolean
  roles: MasterRef[]
  propertyIds: number[]
  createdAt: string
  updatedAt: string
}

export interface UserRequest {
  username: string
  fullName: string
  password: string | null
  enabled: boolean
  roleIds: number[]
  propertyIds: number[]
}

export const usersApi = {
  list: async (): Promise<AppUser[]> => (await apiClient.get<AppUser[]>('/v1/users')).data,
  get: async (id: number): Promise<AppUser> => (await apiClient.get<AppUser>(`/v1/users/${id}`)).data,
  create: async (request: UserRequest): Promise<AppUser> => (await apiClient.post<AppUser>('/v1/users', request)).data,
  update: async (id: number, request: UserRequest): Promise<AppUser> =>
    (await apiClient.put<AppUser>(`/v1/users/${id}`, request)).data,
  remove: async (id: number): Promise<void> => {
    await apiClient.delete(`/v1/users/${id}`)
  },
}
