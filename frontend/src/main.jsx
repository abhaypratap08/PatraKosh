import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, HashRouter } from 'react-router-dom'
import App from './App'
import { IS_STATIC_PREVIEW_HOST } from './app-env'
import { AuthProvider } from './auth-context'
import './styles.css'

const Router = IS_STATIC_PREVIEW_HOST ? HashRouter : BrowserRouter

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <AuthProvider>
      <Router>
        <App />
      </Router>
    </AuthProvider>
  </React.StrictMode>
)
