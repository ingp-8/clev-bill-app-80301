import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { clearToken, getRefreshToken, getToken, setRefreshToken, setToken } from '../auth/tokenStorage'

export const apiClient = axios.create({
  baseURL: '/api',
})

apiClient.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

type RetryableConfig = InternalAxiosRequestConfig & { _retry?: boolean }

let refreshPromise: Promise<string | null> | null = null

async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = getRefreshToken()
  if (!refreshToken) return null
  try {
    const { data } = await axios.post<{ token: string; refreshToken: string }>('/api/v1/auth/refresh', {
      refreshToken,
    })
    setToken(data.token)
    setRefreshToken(data.refreshToken)
    return data.token
  } catch {
    return null
  }
}

function redirectToLogin() {
  clearToken()
  if (window.location.pathname !== '/login') {
    window.location.assign('/login')
  }
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as RetryableConfig | undefined
    const isAuthEndpoint = originalRequest?.url?.includes('/auth/')

    if (error.response?.status === 401 && originalRequest && !originalRequest._retry && !isAuthEndpoint) {
      originalRequest._retry = true
      refreshPromise ??= refreshAccessToken().finally(() => {
        refreshPromise = null
      })
      const newToken = await refreshPromise

      if (newToken) {
        originalRequest.headers.set('Authorization', `Bearer ${newToken}`)
        return apiClient(originalRequest)
      }
    }

    if (error.response?.status === 401) {
      redirectToLogin()
    }
    return Promise.reject(error)
  },
)
