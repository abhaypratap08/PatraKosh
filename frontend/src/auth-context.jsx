import React, { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { IS_STATIC_PREVIEW_HOST } from './app-env'
import api from './api'
import { AUTH_CLEARED_EVENT, AUTH_UPDATED_EVENT, clearAuth, getAuth, setAuth } from './auth'

const AuthContext = createContext(null)
const PREVIEW_VIEWER = {
  username: 'Hosted Preview',
  email: 'preview@patrakosh.local'
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => getAuth().user)
  const [initializing, setInitializing] = useState(true)
  const [apiState, setApiState] = useState('checking')

  useEffect(() => {
    let cancelled = false

    const bootstrap = async () => {
      try {
        const res = await api.get('/api/auth/me')
        if (cancelled) return
        setAuth(res.data, { notify: false })
        setUser(res.data)
        setApiState('reachable')
      } catch (err) {
        if (cancelled) return
        clearAuth({ notify: false })
        setUser(null)
        setApiState(err?.response?.status === 401 ? 'reachable' : IS_STATIC_PREVIEW_HOST ? 'offline' : 'unreachable')
      } finally {
        if (!cancelled) {
          setInitializing(false)
        }
      }
    }

    bootstrap()

    const handleAuthUpdated = (event) => {
      setUser(event.detail?.user ?? getAuth().user)
      setApiState('reachable')
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

  const previewMode = IS_STATIC_PREVIEW_HOST && apiState === 'offline'
  const viewer = user ?? (previewMode ? PREVIEW_VIEWER : null)

  const value = useMemo(() => ({
    user,
    viewer,
    initializing,
    apiState,
    apiAvailable: apiState === 'reachable',
    previewMode,
    signIn(nextUser) {
      setAuth(nextUser)
      setUser(nextUser)
      setApiState('reachable')
    },
    signOut() {
      clearAuth()
      setUser(null)
    }
  }), [apiState, initializing, previewMode, user, viewer])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
