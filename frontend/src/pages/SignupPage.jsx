import React, { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import api from '../api'
import { useAuth } from '../auth-context'

export default function SignupPage() {
  const navigate = useNavigate()
  const { user, apiAvailable, apiState, initializing, previewMode, signIn } = useAuth()
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  if (!initializing && user) {
    return <Navigate to="/dashboard" replace />
  }

  const submit = async (e) => {
    e.preventDefault()
    if (!apiAvailable) {
      setError(previewMode ? 'Hosted preview mode does not support account creation.' : 'The API is unavailable.')
      return
    }
    setError('')
    setLoading(true)
    try {
      const res = await api.post('/api/auth/signup', { username, email, password, confirmPassword })
      signIn(res.data.user)
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
    <div className="container auth-shell">
      <section className="auth-grid">
        <div className="card auth-intro">
          <span className="eyebrow">Account Setup</span>
          <h1>Create a PatraKosh workspace.</h1>
          <p className="muted">
            The local stack persists users, sessions, files, and activity state. The public GitHub Pages deployment is
            only a frontend showcase and intentionally does not register live accounts.
          </p>

          <ul className="plain-list">
            <li>PBKDF2 password hashing for stored credentials.</li>
            <li>Revocable cookie-based sessions.</li>
            <li>Local persistence for files, metadata, and history.</li>
          </ul>
        </div>

        <div className="card auth-card">
          <h2>Create account</h2>
          <p className="muted">Start storing files securely</p>

          {previewMode ? (
            <div className="alert info">
              The hosted preview does not create server accounts. Use the preview workspace or run the app locally.
            </div>
          ) : null}
          {!previewMode && apiState === 'unreachable' ? (
            <div className="alert error">The API is not reachable. Start the backend before creating an account.</div>
          ) : null}

          <form className="row" onSubmit={submit}>
          <div>
            <label className="muted">Username</label>
            <input className="input" value={username} onChange={(e) => setUsername(e.target.value)} disabled={!apiAvailable || loading} />
          </div>
          <div>
            <label className="muted">Email</label>
            <input className="input" value={email} onChange={(e) => setEmail(e.target.value)} disabled={!apiAvailable || loading} />
          </div>
          <div>
            <label className="muted">Password</label>
            <input className="input" type="password" value={password} onChange={(e) => setPassword(e.target.value)} disabled={!apiAvailable || loading} />
          </div>
          <div>
            <label className="muted">Confirm password</label>
            <input
              className="input"
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              disabled={!apiAvailable || loading}
            />
          </div>

            {error ? <div className="alert error">{error}</div> : null}

            <button className="btn primary" disabled={loading || !apiAvailable}>
              {loading ? 'Creating...' : 'Sign up'}
            </button>

            {previewMode ? (
              <Link className="btn ghost" to="/dashboard">Open hosted preview</Link>
            ) : null}

            <div className="muted auth-footnote">
              Already have an account? <Link to="/login">Login</Link>
            </div>
          </form>
        </div>
      </section>
    </div>
  )
}
