import { createContext, useContext, useMemo, useState, type ReactNode } from 'react'
import { login as loginRequest, type LoginRequest } from '../api/authApi'
import { clearToken, getToken, setToken } from './tokenStorage'

interface AuthUser {
  username: string
  fullName: string
  roles: string[]
}

interface AuthContextValue {
  user: AuthUser | null
  isAuthenticated: boolean
  login: (request: LoginRequest) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

const USER_KEY = 'clevbill.user'

function loadStoredUser(): AuthUser | null {
  const raw = localStorage.getItem(USER_KEY)
  return raw ? (JSON.parse(raw) as AuthUser) : null
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(() => (getToken() ? loadStoredUser() : null))

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: user !== null,
      login: async (request) => {
        const response = await loginRequest(request)
        setToken(response.token)
        const authUser: AuthUser = {
          username: response.username,
          fullName: response.fullName,
          roles: response.roles,
        }
        localStorage.setItem(USER_KEY, JSON.stringify(authUser))
        setUser(authUser)
      },
      logout: () => {
        clearToken()
        localStorage.removeItem(USER_KEY)
        localStorage.removeItem('clevbill.activePropertyId')
        setUser(null)
      },
    }),
    [user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
