import { apiClient } from './client'
import type { Property } from './properties'

export interface Me {
  id: number
  username: string
  fullName: string
  roles: string[]
  permissions: string[]
  superAdmin: boolean
}

export const meApi = {
  profile: async (): Promise<Me> => (await apiClient.get<Me>('/v1/me')).data,
  properties: async (): Promise<Property[]> => (await apiClient.get<Property[]>('/v1/me/properties')).data,
}
