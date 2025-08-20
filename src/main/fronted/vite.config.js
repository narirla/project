import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],

  build: {
    manifest: true,
    outDir: '../resources/static/assets',
    emptyOutDir: false,

    rollupOptions: {
      input: './src/main.jsx',
      output: {
        entryFileNames: '[name]-[hash].js',
        chunkFileNames: '[name]-[hash].js',
        assetFileNames: '[name]-[hash].[ext]'
      }
    }
  },

  server: {
    port: 3000,
    host: true,
    proxy: {
      '/cart': {
        target: 'http://localhost:9070',
        changeOrigin: true,
        secure: false
      },
      '/api': {
        target: 'http://localhost:9070',
        changeOrigin: true,
        secure: false
      }
    }
  }
})