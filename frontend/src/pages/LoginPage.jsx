import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api'
import { setAuth } from '../auth'

export default function LoginPage() {
  const navigate = useNavigate()
  const [usernameOrEmail, setUsernameOrEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await api.post('/api/auth/login', { usernameOrEmail, password })
      setAuth(res.data.token, res.data.user)
      navigate('/dashboard', { replace: true })
    } catch (err) {
      setError(err?.response?.data?.message || 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="container" style={{ maxWidth: 520 }}>
      <div className="card">
        <h2 style={{ marginTop: 0 }}>Login</h2>
        <p className="muted" style={{ marginTop: -6 }}>Access your secure files</p>

        <form className="row" onSubmit={submit}>
          <div>
            <label className="muted">Username or Email</label>
            <input className="input" value={usernameOrEmail} onChange={(e) => setUsernameOrEmail(e.target.value)} />
          </div>
          <div>
            <label className="muted">Password</label>
            <input className="input" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
          </div>

          {error ? <div className="error">{error}</div> : null}

          <button className="btn primary" disabled={loading}>
            {loading ? 'Signing in…' : 'Login'}
          </button>

          <div className="muted" style={{ fontSize: 13 }}>
            Don’t have an account? <Link to="/signup">Sign up</Link>
          </div>
        </form>
      </div>
    </div>
  )
}
