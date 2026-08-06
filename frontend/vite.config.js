import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],

  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },

  // ── Development Server ──────────────────────────────────────────────────
  server: {
    port: 3000,
    // Proxy API requests to Spring Boot during development.
    // This avoids CORS issues — the browser only talks to localhost:3000.
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      '/actuator': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },

  // ── Production Build ─────────────────────────────────────────────────────
  // Output directly into Spring Boot's static resources directory.
  // Spring Boot bundles these files into the JAR at build time.
  // WebConfig.java serves them at GET /** with SPA fallback to index.html.
  build: {
    outDir: '../backend/src/main/resources/static',
    emptyOutDir: true,
    sourcemap: false,
    rollupOptions: {
      output: {
        // Split vendor libraries into separate chunks for better caching
        manualChunks: {
          vendor: ['react', 'react-dom', 'react-router-dom'],
          mui: ['@mui/material', '@mui/icons-material', '@emotion/react', '@emotion/styled'],
          charts: ['recharts'],
        },
      },
    },
  },
})
