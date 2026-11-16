import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // Any frontend call to /api/... is forwarded to the Spring Boot backend.
      // Same-origin for the browser = no CORS problems in development.
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      // live chat over WebSocket (ws: true = proxy the upgrade request too)
      '/ws': {
        target: 'ws://localhost:8081',
        ws: true,
      },
    },
  },
})
