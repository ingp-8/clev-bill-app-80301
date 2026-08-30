import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { useSystemConfig } from '../hooks/useSystemConfig'

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const { data: config } = useSystemConfig()

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await login({ username, password })
      navigate('/', { replace: true })
    } catch {
      setError('Invalid username or password')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div style={{ maxWidth: 320, margin: '80px auto', textAlign: 'left' }}>
      <h1 style={{ fontSize: 32, textAlign: 'center' }}>{config?.storeName ?? 'clevbill'}</h1>
      <form onSubmit={handleSubmit}>
        <label style={{ display: 'block', marginBottom: 12 }}>
          Username
          <input
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoFocus
            required
            style={{ display: 'block', width: '100%', padding: 8, marginTop: 4 }}
          />
        </label>
        <label style={{ display: 'block', marginBottom: 12 }}>
          Password
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            style={{ display: 'block', width: '100%', padding: 8, marginTop: 4 }}
          />
        </label>
        {error && (
          <p role="alert" style={{ color: 'crimson', marginBottom: 12 }}>
            {error}
          </p>
        )}
        <button type="submit" disabled={submitting} style={{ width: '100%', padding: 10 }}>
          {submitting ? 'Signing in...' : 'Sign in'}
        </button>
      </form>
    </div>
  )
}
