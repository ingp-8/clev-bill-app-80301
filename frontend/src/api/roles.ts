import { apiClient } from './client'

export type PermissionAction = 'VIEW' | 'CREATE' | 'EDIT' | 'DELETE'

export interface Permission {
  id: number
  moduleCode: string
  action: PermissionAction
  description: string | null
}

export interface Role {
  id: number
  roleName: string
  description: string | null
  isSystem: boolean
  active: boolean
  permissions: Permission[]
  createdAt: string
  updatedAt: string
}

export interface RoleRequest {
  roleName: string
  description: string | null
  active: boolean
  permissionIds: number[]
}

export const rolesApi = {
  list: async (): Promise<Role[]> => (await apiClient.get<Role[]>('/v1/roles')).data,
  get: async (id: number): Promise<Role> => (await apiClient.get<Role>(`/v1/roles/${id}`)).data,
  create: async (request: RoleRequest): Promise<Role> => (await apiClient.post<Role>('/v1/roles', request)).data,
  update: async (id: number, request: RoleRequest): Promise<Role> =>
    (await apiClient.put<Role>(`/v1/roles/${id}`, request)).data,
  remove: async (id: number): Promise<void> => {
    await apiClient.delete(`/v1/roles/${id}`)
  },
}

export const permissionsApi = {
  list: async (): Promise<Permission[]> => (await apiClient.get<Permission[]>('/v1/permissions')).data,
}
