import fs from 'node:fs'
import path from 'node:path'
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

const repoRoot = path.resolve(__dirname, '..')
const devKeystorePath = process.env.PATRAKOSH_SSL_KEY_STORE || path.resolve(repoRoot, '.certs/patrakosh-dev.p12')
const devKeystorePassword = process.env.PATRAKOSH_SSL_KEY_STORE_PASSWORD || 'changeit'
const httpsConfig = fs.existsSync(devKeystorePath)
  ? {
      pfx: fs.readFileSync(devKeystorePath),
      passphrase: devKeystorePassword
    }
  : undefined

export default defineConfig(({ command }) => {
  const publicBase = process.env.PATRAKOSH_PUBLIC_BASE || (command === 'build' ? '/PatraKosh/' : '/')

  return {
    base: publicBase,
    plugins: [react()],
    server: {
      host: '127.0.0.1',
      port: 5173,
      https: httpsConfig,
      proxy: {
        '/api': {
          target: process.env.VITE_API_BASE_URL || (httpsConfig ? 'https://127.0.0.1:8443' : 'http://127.0.0.1:8080'),
          changeOrigin: true,
          secure: false
        }
      }
    }
  }
})
