#!/usr/bin/env bash
# =========================================================
# Traer los datos de otra instalación (por ejemplo, tu PC) al servidor.
# docs/13-produccion.md §3, paso 8.
#
#   ./import-data.sh /ruta/mantas_2026-11-24_210000.dump
#
# REEMPLAZA todos los datos del servidor por los de la copia:
#   1. Comprueba la huella .sha256 de la copia (obligatoria).
#   2. Hace una copia de lo que hay ahora en el servidor.
#   3. Detiene el backend, vacía la base y restaura la copia.
#   4. Arranca el backend (aplica migraciones pendientes) y verifica.
# Pide escribir REEMPLAZAR; en un procedimiento automatizado: CONFIRM=REEMPLAZAR.
# =========================================================
set -euo pipefail
cd "$(dirname "$0")"

FILE="${1:?Uso: ./import-data.sh archivo.dump}"
[[ -f "$FILE" ]] || { echo "No existe el archivo $FILE"; exit 1; }
[[ -f .env ]] || { echo "Falta deploy/.env"; exit 1; }
set -a; . ./.env; set +a
DB="${DB_NAME:-mantas_guajiras_db}"
APP_USER="${DB_USERNAME:-mantas_app}"
SITE="${SITE_ADDRESS:-localhost}"
# Versión instalada (la deja deploy.sh).
APP_TAG="$(cat .version-actual 2>/dev/null || true)"
[[ -n "$APP_TAG" ]] || { echo "Primero instala con ./deploy.sh."; exit 1; }
export APP_TAG
[[ "$SITE" == "localhost" ]] && export VERIFY_INSECURE=1

# 1. Huella
if [[ ! -f "$FILE.sha256" ]]; then
    echo "Falta $FILE.sha256: sin huella no se puede comprobar que la copia esté sana."
    exit 1
fi
if [[ "$(sha256sum "$FILE" | cut -d' ' -f1 | tr 'a-f' 'A-F')" != "$(tr -d '[:space:]' < "$FILE.sha256")" ]]; then
    echo "La huella SHA-256 no coincide: el archivo está dañado. Usa otra copia."
    exit 1
fi
echo "Huella SHA-256 correcta."

echo
echo "ATENCIÓN: se REEMPLAZARÁN todos los datos de $SITE por los de:"
echo "  $FILE"
if [[ "${CONFIRM:-}" != "REEMPLAZAR" ]]; then
    read -r -p "Escribe REEMPLAZAR para continuar: " answer
    [[ "$answer" == "REEMPLAZAR" ]] || { echo "Cancelado. No se cambió nada."; exit 1; }
fi

echo "== 1/4 Copia de lo que hay ahora en el servidor"
docker compose run --rm --no-deps --entrypoint bash backup /scripts/backup-db.sh

echo "== 2/4 Deteniendo el backend"
docker compose stop backend

echo "== 3/4 Restaurando"
docker compose exec -T db psql -U postgres -v ON_ERROR_STOP=1 \
    -c "DROP DATABASE \"$DB\" WITH (FORCE)" \
    -c "CREATE DATABASE \"$DB\" OWNER \"$APP_USER\""
docker compose exec -T db psql -U postgres -d "$DB" -v ON_ERROR_STOP=1 \
    -c "ALTER SCHEMA public OWNER TO \"$APP_USER\""
# Todo queda a nombre del usuario de la app (no del dueño en tu PC).
docker compose exec -T db pg_restore -U postgres --no-owner --no-privileges \
    --role="$APP_USER" --exit-on-error -d "$DB" < "$FILE"

echo "== 4/4 Arrancando y verificando"
docker compose up -d --no-build --wait --wait-timeout 240 backend
docker compose exec -T db psql -U postgres -d "$DB" -At -F ' | ' < ../scripts/backup/integrity-check.sql
./verify.sh "https://$SITE${HTTPS_PORT:+:$HTTPS_PORT}"
echo "== Datos importados. Revisa en la app inventario, últimas ventas y usuarios."
