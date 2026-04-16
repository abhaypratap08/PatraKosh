import React, { createContext, useContext, useEffect, useMemo, useState } from 'react'
import api from './api'
import { AUTH_CLEARED_EVENT, AUTH_UPDATED_EVENT, clearAuth, getAuth, setAuth } from './auth'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => getAuth().user)
  const [initializing, setInitializing] = useState(true)

  useEffect(() => {
    let cancelled = false

    const bootstrap = async () => {
      try {
        const res = await api.get('/api/auth/me')
        if (cancelled) return
        setAuth(res.data, { notify: false })
        setUser(res.data)
      } catch (err) {
        if (cancelled) return
        clearAuth({ notify: false })
        setUser(null)
      } finally {
        if (!cancelled) {
          setInitializing(false)
        }
      }
    }

    bootstrap()

    const handleAuthUpdated = (event) => {
      setUser(event.detail?.user ?? getAuth().user)
    }

    const handleAuthCleared = () => {
      setUser(null)
    }

    window.addEventListener(AUTH_UPDATED_EVENT, handleAuthUpdated)
    window.addEventListener(AUTH_CLEARED_EVENT, handleAuthCleared)

    return () => {
      cancelled = true
      window.removeEventListener(AUTH_UPDATED_EVENT, handleAuthUpdated)
      window.removeEventListener(AUTH_CLEARED_EVENT, handleAuthCleared)
    }
  }, [])

  const value = useMemo(() => ({
    user,
    initializing,
    signIn(nextUser) {
      setAuth(nextUser)
      setUser(nextUser)
    },
    signOut() {
      clearAuth()
      setUser(null)
    }
  }), [initializing, user])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
