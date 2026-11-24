# =========================================================
# Frontend + servidor web (Caddy) de Mantas Guajiras.
# Caddy sirve la app, reenvía /api, /auth, /users y /actuator/{health,info}
# al backend (mismo dominio: sin CORS) y obtiene el certificado HTTPS solo.
# Se usa desde deploy/ (el Caddyfile está en deploy/Caddyfile).
# =========================================================
FROM node:22-alpine AS build
WORKDIR /src
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY . .
ARG APP_COMMIT=
# Sin VITE_API_URL: la app llama a su mismo dominio.
ENV VITE_API_URL= APP_COMMIT=${APP_COMMIT}
RUN npm run build

FROM caddy:2.10-alpine
COPY --from=build /src/dist /srv
