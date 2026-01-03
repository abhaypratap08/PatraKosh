import React from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { clearAuth, getAuth } from '../auth'

export default function AppLayout() {
  const navigate = useNavigate()
  const { user } = getAuth()

  const logout = () => {
    clearAuth()
    navigate('/login', { replace: true })
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

        <button className="btn" onClick={logout}>Logout</button>
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
