import react from '@vitejs/plugin-react'
import basicSsl from '@vitejs/plugin-basic-ssl'
import { defineConfig } from 'vite'

// Rutas del backend que se reenvían desde el servidor de Vite.
const BACKEND = 'http://localhost:8080'
const BACKEND_PATHS = ['/api', '/auth', '/users']

/**
 * Modos:
 * - `npm run dev`          → solo en este computador (http://localhost:5173).
 * - `npm run dev:celular`  → también desde el celular en la misma red Wi-Fi,
 *   con HTTPS (el navegador del celular solo permite la cámara con HTTPS).
 *   En este modo el celular habla SOLO con Vite y Vite reenvía al backend,
 *   porque en el celular "localhost" sería el propio teléfono.
 *
 * https://vite.dev/config/
 */
export default defineConfig(({ mode }) => {

  const celular = mode === 'celular'

  const proxy = Object.fromEntries(
    BACKEND_PATHS.map((path) => [
      path,
      {
        target: BACKEND,
        changeOrigin: true,
        // El backend solo acepta orígenes conocidos (CORS). Como la
        // petición llega desde el propio servidor de Vite, se quita el
        // origen del navegador (p. ej. https://192.168.x.x:5173).
        configure: (proxyServer) => {
          proxyServer.on('proxyReq', (proxyReq) => {
            proxyReq.removeHeader('origin')
          })
        },
      },
    ])
  )

  return {
    plugins: [react(), ...(celular ? [basicSsl()] : [])],

    server: {
      // Escuchar en la red local solo en modo celular.
      host: celular ? true : 'localhost',
      port: 5173,
      proxy,
    },

    // En modo celular las peticiones van a la misma dirección de la
    // página (rutas relativas) y Vite las reenvía al backend.
    define: celular
      ? { 'import.meta.env.VITE_API_URL': JSON.stringify('') }
      : {},
  }
})
