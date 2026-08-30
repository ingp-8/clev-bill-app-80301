import { apiClient } from './client'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  username: string
  fullName: string
  roles: string[]
  expiresAt: string
}

export async function login(request: LoginRequest): Promise<LoginResponse> {
  const { data } = await apiClient.post<LoginResponse>('/v1/auth/login', request)
  return data
}
