import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],

  build: {
    manifest: true,
    outDir: '../resources/static/assets',
    emptyOutDir: false,

    rollupOptions: {
      input: {
        main: './src/main.jsx',
        'order-main': './src/order-main.jsx',
        'order-complete-main': './src/order-complete-main.jsx'
      },
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
    watch: {                
      usePolling: true
    },
    proxy: {
      '/cart': {
        target: 'http://localhost:9070',
        changeOrigin: true,
        secure: false
      },
      '/order': {
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