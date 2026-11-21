import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    // Under utveckling: skicka /api-anrop vidare till Spring Boot, så att CORS inte behövs.
    proxy: {
      '/api': 'http://localhost:8081',
      '/graphql': 'http://localhost:8081',
    },
  },
})
