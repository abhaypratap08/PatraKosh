import axios from 'axios'
import { appPath, currentAppPath } from './app-env'
import { clearAuth } from './auth'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  withCredentials: true
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status
    const requestUrl = error?.config?.url || ''
    const isAuthEndpoint = requestUrl.includes('/api/auth/')

    if (status === 401 && !isAuthEndpoint) {
      clearAuth()
      if (currentAppPath() !== '/login') {
        window.location.replace(appPath('/login'))
      }
    }

    return Promise.reject(error)
  }
)

export default api
