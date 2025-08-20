import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  build: {
    outDir: '../resources/static/',
    emptyOutDir: true,
  },
  server: {
    port: 3000,
    host: true,
    proxy: {
      '/api': 'http://localhost:9070'
    }
  }
})