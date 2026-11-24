import react from '@vitejs/plugin-react'
import basicSsl from '@vitejs/plugin-basic-ssl'
import { defineConfig, loadEnv } from 'vite'
import process from 'node:process'
import { readFileSync } from 'node:fs'

// Versión visible en "Mi perfil": la de package.json y, si se compila
// con APP_COMMIT (lo hace el kit de despliegue), el commit exacto.
const pkg = JSON.parse(readFileSync(new URL('./package.json', import.meta.url), 'utf8'))
const appVersion = process.env.APP_COMMIT
  ? `${pkg.version} (${process.env.APP_COMMIT.slice(0, 7)})`
  : pkg.version

// Rutas del backend que se reenvían desde el servidor de Vite.
const BACKEND = 'http://localhost:8080'
const BACKEND_PATHS = ['/api', '/auth', '/users']

/**
 * Política de seguridad de contenido (CSP) solo en la versión compilada.
 *
 * Limita lo que el navegador acepta ejecutar y a dónde puede enviar datos:
 * aunque alguien lograra inyectar código en la página, no podría cargar
 * scripts de otros sitios ni enviar el token o las ventas guardadas a un
 * servidor ajeno (connect-src solo permite este sitio y el backend).
 *
 * En desarrollo (npm run dev) no se aplica: Vite necesita scripts en línea
 * para la recarga en caliente.
 */
function originOf(url) {
  try {
    return url ? new URL(url).origin : ''
  } catch {
    return ''
  }
}

function contentSecurityPolicy(apiUrl) {
  const apiOrigin = originOf(apiUrl)

  const policy = [
    "default-src 'self'",
    "script-src 'self'",
    // React aplica estilos en línea en algunos componentes (avatar).
    "style-src 'self' 'unsafe-inline'",
    // Fotos de perfil (blob:) y vista de la cámara.
    "img-src 'self' data: blob:",
    "media-src 'self' blob:",
    `connect-src 'self'${apiOrigin ? ` ${apiOrigin}` : ''}`,
    "worker-src 'self'",
    "manifest-src 'self'",
    "object-src 'none'",
    "base-uri 'self'",
    "form-action 'self'",
  ].join('; ')

  return {
    name: 'mantas-csp',
    apply: 'build',
    transformIndexHtml(html) {
      return html.replace(
        '<meta charset="UTF-8" />',
        `<meta charset="UTF-8" />\n    <meta http-equiv="Content-Security-Policy" content="${policy}" />`
      )
    },
  }
}

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
  const env = loadEnv(mode, process.cwd(), '')

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
    plugins: [
      react(),
      ...(celular ? [basicSsl()] : []),
      contentSecurityPolicy(process.env.VITE_API_URL || env.VITE_API_URL),
    ],

    server: {
      // Escuchar en la red local solo en modo celular.
      host: celular ? true : 'localhost',
      port: 5173,
      proxy,
    },

    // En modo celular las peticiones van a la misma dirección de la
    // página (rutas relativas) y Vite las reenvía al backend.
    define: {
      __APP_VERSION__: JSON.stringify(appVersion),
      ...(celular ? { 'import.meta.env.VITE_API_URL': JSON.stringify('') } : {}),
    },
  }
})
