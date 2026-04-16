import React, { useState } from 'react'
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import api from '../api'
import { useAuth } from '../auth-context'

export default function AppLayout() {
  const navigate = useNavigate()
  const { viewer, previewMode, signOut } = useAuth()
  const [loggingOut, setLoggingOut] = useState(false)

  const logout = async () => {
    if (previewMode) {
      navigate('/', { replace: true })
      return
    }

    setLoggingOut(true)
    try {
      await api.post('/api/auth/logout')
    } catch {
    } finally {
      signOut()
      navigate('/login', { replace: true })
      setLoggingOut(false)
    }
  }

  return (
    <div className="app-shell container">
      <header className="shell-topbar">
        <div>
          <Link to="/" className="brand-lockup">PatraKosh</Link>
          <p className="shell-subtitle">
            {previewMode
              ? 'Hosted preview workspace on GitHub Pages'
              : `Signed in as ${viewer?.username || viewer?.email || 'User'}`}
          </p>
        </div>

        <div className="topbar-actions">
          {previewMode ? <span className="pill">Preview mode</span> : null}
          <button className="btn ghost" onClick={logout} disabled={loggingOut}>
            {previewMode ? 'Back to home' : loggingOut ? 'Logging out...' : 'Logout'}
          </button>
        </div>
      </header>

      <nav className="nav-strip">
        <NavLink to="/dashboard" className={({ isActive }) => (isActive ? 'active' : '')}>Dashboard</NavLink>
        <NavLink to="/team" className={({ isActive }) => (isActive ? 'active' : '')}>Team</NavLink>
        <NavLink to="/timeline" className={({ isActive }) => (isActive ? 'active' : '')}>Timeline</NavLink>
        <NavLink to="/leaderboard" className={({ isActive }) => (isActive ? 'active' : '')}>Leaderboard</NavLink>
        <NavLink to="/faq" className={({ isActive }) => (isActive ? 'active' : '')}>FAQ</NavLink>
        <NavLink to="/review" className={({ isActive }) => (isActive ? 'active' : '')}>Review</NavLink>
      </nav>

      {previewMode ? (
        <section className="banner">
          <strong>Hosted preview:</strong> the static site is now stable under GitHub Pages, but uploads, real downloads,
          and secure share links still require the local API.
        </section>
      ) : null}

      <Outlet />
    </div>
  )
}
