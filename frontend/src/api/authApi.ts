import { apiClient } from './client'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  refreshToken: string
  username: string
  fullName: string
  roles: string[]
  expiresAt: string
}

export async function login(request: LoginRequest): Promise<LoginResponse> {
  const { data } = await apiClient.post<LoginResponse>('/v1/auth/login', request)
  return data
}

export async function refresh(refreshToken: string): Promise<LoginResponse> {
  const { data } = await apiClient.post<LoginResponse>('/v1/auth/refresh', { refreshToken })
  return data
}

export async function logout(refreshToken: string): Promise<void> {
  await apiClient.post('/v1/auth/logout', { refreshToken })
}
