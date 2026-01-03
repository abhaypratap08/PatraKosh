import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api'
import { setAuth } from '../auth'

export default function SignupPage() {
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await api.post('/api/auth/signup', { username, email, password, confirmPassword })
      setAuth(res.data.token, res.data.user)
      navigate('/dashboard', { replace: true })
    } catch (err) {
      const data = err?.response?.data
      if (data?.fieldErrors) {
        const msg = Object.entries(data.fieldErrors)
          .map(([k, v]) => `${k}: ${v}`)
          .join(' | ')
        setError(msg)
      } else {
        setError(data?.message || 'Signup failed')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="container" style={{ maxWidth: 520 }}>
      <div className="card">
        <h2 style={{ marginTop: 0 }}>Create account</h2>
        <p className="muted" style={{ marginTop: -6 }}>Start storing files securely</p>

        <form className="row" onSubmit={submit}>
          <div>
            <label className="muted">Username</label>
            <input className="input" value={username} onChange={(e) => setUsername(e.target.value)} />
          </div>
          <div>
            <label className="muted">Email</label>
            <input className="input" value={email} onChange={(e) => setEmail(e.target.value)} />
          </div>
          <div>
            <label className="muted">Password</label>
            <input className="input" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
          </div>
          <div>
            <label className="muted">Confirm password</label>
            <input className="input" type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} />
          </div>

          {error ? <div className="error">{error}</div> : null}

          <button className="btn primary" disabled={loading}>
            {loading ? 'Creating…' : 'Sign up'}
          </button>

          <div className="muted" style={{ fontSize: 13 }}>
            Already have an account? <Link to="/login">Login</Link>
          </div>
        </form>
      </div>
    </div>
  )
}
