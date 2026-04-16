import React from 'react'
import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../auth-context'

export default function ProtectedRoute() {
  const { user, initializing } = useAuth()

  if (initializing) {
    return (
      <div className="container" style={{ maxWidth: 520 }}>
        <div className="card">Checking session…</div>
      </div>
    )
  }

  if (!user) {
    return <Navigate to="/login" replace />
  }
  return <Outlet />
}
