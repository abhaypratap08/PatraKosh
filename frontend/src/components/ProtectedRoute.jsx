import React from 'react'
import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../auth-context'

export default function ProtectedRoute() {
  const { viewer, initializing } = useAuth()

  if (initializing) {
    return (
      <div className="container narrow-shell">
        <div className="card status-card">Checking session...</div>
      </div>
    )
  }

  if (!viewer) {
    return <Navigate to="/login" replace />
  }
  return <Outlet />
}
