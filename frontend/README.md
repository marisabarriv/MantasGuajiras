# Mantas Guajiras — Frontend

Aplicación web (React + Vite) del sistema Mantas Guajiras. Documentación completa: [`docs/08-frontend.md`](../docs/08-frontend.md).

## Configuración

Copia `.env.example` como `.env`:

```text
VITE_API_URL=http://localhost:8080
```

## Comandos

| Comando               | Para qué                                                                  |
| --------------------- | ------------------------------------------------------------------------- |
| `npm ci`              | Instala las dependencias exactas de `package-lock.json`.                   |
| `npm run dev`         | Desarrollo en este computador: http://localhost:5173                      |
| `npm run dev:celular` | Desarrollo accesible desde el celular en la misma red Wi-Fi (HTTPS + proxy al backend; necesario para la cámara). |
| `npm run lint`        | Revisión con ESLint.                                                       |
| `npm run build`       | Compilación de producción en `dist/`.                                      |
| `npm run preview`     | Sirve localmente la compilación de `dist/`.                                |

Si `npm run build` falla con un error de un módulo nativo (por ejemplo, Rolldown), casi siempre es porque `node_modules` se copió desde otro sistema operativo: bórralo y ejecuta `npm ci` de nuevo.
