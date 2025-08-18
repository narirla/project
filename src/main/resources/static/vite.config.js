
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,  // 통상적인 포트
    proxy: {
      '/api': 'http://localhost:9070'  // 백엔드 연동
    }
  }
})
