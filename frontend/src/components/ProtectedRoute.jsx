import React from 'react'
import { Navigate, Outlet } from 'react-router-dom'
import { isAuthed } from '../auth'

export default function ProtectedRoute() {
  if (!isAuthed()) {
    return <Navigate to="/login" replace />
  }
  return <Outlet />
}
