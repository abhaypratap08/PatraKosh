import React, { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import api from '../api'
import { useAuth } from '../auth-context'

export default function LoginPage() {
  const navigate = useNavigate()
  const { user, apiAvailable, apiState, initializing, previewMode, signIn } = useAuth()
  const [usernameOrEmail, setUsernameOrEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  if (!initializing && user) {
    return <Navigate to="/dashboard" replace />
  }

  const submit = async (e) => {
    e.preventDefault()
    if (!apiAvailable) {
      setError(previewMode ? 'Hosted preview mode does not support live sign-in.' : 'The API is unavailable.')
      return
    }
    setError('')
    setLoading(true)
    try {
      const res = await api.post('/api/auth/login', { usernameOrEmail, password })
      signIn(res.data.user)
      navigate('/dashboard', { replace: true })
    } catch (err) {
      setError(err?.response?.data?.message || 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="container auth-shell">
      <section className="auth-grid">
        <div className="card auth-intro">
          <span className="eyebrow">Session Access</span>
          <h1>Sign in to the secure workspace.</h1>
          <p className="muted">
            Use the local API and frontend together to test the full session flow, file storage, share links, and
            activity logging.
          </p>

          <ul className="plain-list">
            <li>HttpOnly session cookies instead of browser-exposed bearer tokens.</li>
            <li>Per-user file isolation with audit-ready activity history.</li>
            <li>Share links that can expire and be revoked.</li>
          </ul>
        </div>

        <div className="card auth-card">
          <h2>Login</h2>
          <p className="muted">Access your secure files</p>

          {previewMode ? (
            <div className="alert info">
              GitHub Pages is serving a hosted preview only. Open the preview workspace below or run the API locally for
              real sign-in.
            </div>
          ) : null}
          {!previewMode && apiState === 'unreachable' ? (
            <div className="alert error">The API is not reachable. Start the backend, then retry.</div>
          ) : null}

          <form className="row" onSubmit={submit}>
          <div>
            <label className="muted">Username or Email</label>
            <input
              className="input"
              value={usernameOrEmail}
              onChange={(e) => setUsernameOrEmail(e.target.value)}
              disabled={!apiAvailable || loading}
            />
          </div>
          <div>
            <label className="muted">Password</label>
            <input
              className="input"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={!apiAvailable || loading}
            />
          </div>

            {error ? <div className="alert error">{error}</div> : null}

            <button className="btn primary" disabled={loading || !apiAvailable}>
              {loading ? 'Signing in...' : 'Login'}
            </button>

            {previewMode ? (
              <Link className="btn ghost" to="/dashboard">Open hosted preview</Link>
            ) : null}

            <div className="muted auth-footnote">
              Do not have an account? <Link to="/signup">Sign up</Link>
            </div>
          </form>
        </div>
      </section>
    </div>
  )
}
