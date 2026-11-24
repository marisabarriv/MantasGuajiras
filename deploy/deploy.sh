#!/usr/bin/env bash
# =========================================================
# Instalar o actualizar Mantas Guajiras (docs/13-produccion.md §4).
#   cd deploy && ./deploy.sh
# Pasos:
#   1. Copia de seguridad de la base (si ya existe).
#   2. Compila la versión actual del repositorio (git pull antes).
#   3. Arranca y espera a que el backend esté sano.
#   4. Verifica (verify.sh).
#   5. Si la verificación falla, vuelve a la versión anterior.
# DEPLOY_SKIP_BUILD=1: instalar imágenes ya compiladas con esa etiqueta.
# =========================================================
set -euo pipefail
cd "$(dirname "$0")"

[[ -f .env ]] || { echo "Falta deploy/.env: copia .env.example a .env y complétalo."; exit 1; }
set -a; . ./.env; set +a

APP_COMMIT="$(git rev-parse --short HEAD 2>/dev/null || echo desconocido)"
export APP_COMMIT APP_TAG="$APP_COMMIT"
SITE="${SITE_ADDRESS:-localhost}"
URL="https://${SITE}"
[[ -n "${HTTPS_PORT:-}" && "${HTTPS_PORT}" != "443" ]] && URL="${URL}:${HTTPS_PORT}"
[[ "$SITE" == "localhost" ]] && export VERIFY_INSECURE=1
[[ -n "${HTTP_PORT:-}" ]] && export VERIFY_HTTP_PORT="$HTTP_PORT"
PREVIOUS="$(cat .version-actual 2>/dev/null || true)"

echo "== Mantas Guajiras: instalando la versión $APP_COMMIT (anterior: ${PREVIOUS:-ninguna})"

# 1. Copia previa: es la vuelta atrás si la versión nueva cambia la base.
if docker compose ps --status running --services 2>/dev/null | grep -qx db; then
    echo "== 1/4 Copia de seguridad previa"
    docker compose run --rm --no-deps --entrypoint bash backup /scripts/backup-db.sh
else
    echo "== 1/4 Primera instalación: no hay base que copiar"
fi

if [[ "${DEPLOY_SKIP_BUILD:-0}" == "1" ]]; then
    echo "== 2/4 Usando imágenes ya compiladas ($APP_TAG)"
else
    echo "== 2/4 Compilando"
    docker compose build
fi

echo "== 3/4 Arrancando"
if ! docker compose up -d --no-build --wait --wait-timeout 240; then
    echo "El backend no quedó sano. Últimas líneas:"
    docker compose logs --tail 40 backend || true
fi

echo "== 4/4 Verificando"
if ./verify.sh "$URL" "$APP_COMMIT"; then
    echo "$APP_TAG" > .version-actual
    echo "== Listo: versión $APP_COMMIT instalada y verificada."
    exit 0
fi

echo "== La versión nueva NO pasó la verificación."
if [[ -n "$PREVIOUS" ]] && docker image inspect "mantasguajiras-backend:$PREVIOUS" >/dev/null 2>&1; then
    echo "== Volviendo a la versión anterior ($PREVIOUS)"
    APP_TAG="$PREVIOUS" APP_COMMIT="$PREVIOUS" docker compose up -d --no-build --wait --wait-timeout 240 backend web || true
    if ./verify.sh "$URL" "$PREVIOUS"; then
        echo "== Se restableció la versión anterior. Revisa: docker compose logs backend"
    else
        echo "== La versión anterior tampoco pasa la verificación."
        echo "   Si la versión nueva cambió la base de datos, restaura la copia del paso 1:"
        echo "   docs/09-copias-de-seguridad.md §4.1 (vuelta atrás)."
    fi
else
    echo "   No hay versión anterior instalada. Revisa: docker compose logs backend"
fi
exit 1
