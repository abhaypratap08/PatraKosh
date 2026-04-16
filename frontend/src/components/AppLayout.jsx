import React, { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import api from '../api'
import { useAuth } from '../auth-context'

export default function AppLayout() {
  const navigate = useNavigate()
  const { user, signOut } = useAuth()
  const [loggingOut, setLoggingOut] = useState(false)

  const logout = async () => {
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
    <div className="container">
      <div className="topbar">
        <div>
          <div style={{ fontSize: 18, fontWeight: 700 }}>PatraKosh</div>
          <div className="muted" style={{ fontSize: 12 }}>
            Signed in as {user?.username || user?.email || 'User'}
          </div>
        </div>

        <button className="btn" onClick={logout} disabled={loggingOut}>
          {loggingOut ? 'Logging out…' : 'Logout'}
        </button>
      </div>

      <div className="nav" style={{ marginBottom: 18 }}>
        <NavLink to="/dashboard" className={({ isActive }) => (isActive ? 'active' : '')}>Dashboard</NavLink>
        <NavLink to="/team" className={({ isActive }) => (isActive ? 'active' : '')}>Team</NavLink>
        <NavLink to="/timeline" className={({ isActive }) => (isActive ? 'active' : '')}>Timeline</NavLink>
        <NavLink to="/leaderboard" className={({ isActive }) => (isActive ? 'active' : '')}>Leaderboard</NavLink>
        <NavLink to="/faq" className={({ isActive }) => (isActive ? 'active' : '')}>FAQ</NavLink>
        <NavLink to="/review" className={({ isActive }) => (isActive ? 'active' : '')}>Review</NavLink>
      </div>

      <Outlet />
    </div>
  )
}
