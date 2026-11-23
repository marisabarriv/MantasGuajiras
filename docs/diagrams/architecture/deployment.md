# Diagrama de despliegue

## Actual (local, versión candidata)

```text
┌──────────────────────── Computador del negocio ────────────────────────┐
│                                                                         │
│  Navegador / app instalada (PWA)                                        │
│        │  http://localhost:5173                                         │
│        ▼                                                                │
│  Frontend React (Vite) ── /api, /auth, /users ──►  Backend Spring Boot  │
│                                                    http://localhost:8080│
│                                                          │ JDBC         │
│                                                          ▼              │
│                                                    PostgreSQL :5432     │
│                                                    mantas_guajiras_db   │
│                                                          │ pg_dump      │
│                                                          ▼  (diario)    │
│                                             %USERPROFILE%\MantasGuajiras-backups
└──────────────────────────────────────────────────────────┼──────────────┘
                                                           ▼
                                              Segunda copia (OneDrive / USB)
```

Celular en la misma red Wi-Fi: `npm run dev:celular` sirve el frontend por HTTPS en la red local y Vite reenvía las peticiones al backend (modo proxy).

## Producción (recomendado)

```text
Navegador ──HTTPS──► Proxy inverso (Nginx/Caddy) ──┬──► frontend/dist (archivos estáticos)
                     app.dominio.com               └──► /api, /auth, /users → Backend :8080 (perfil prod)
                                                                               │
                                                                               ▼
                                                             PostgreSQL (sin acceso desde Internet)
                                                                               │ backup-db.sh (cron)
                                                                               ▼
                                                              Copias locales + copia externa
```

Con un mismo dominio para frontend y backend, CORS no interviene. Detalles en `docs/06-despliegue.md` (sección 14) y `docs/09-copias-de-seguridad.md`.
