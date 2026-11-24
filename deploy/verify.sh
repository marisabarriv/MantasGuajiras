#!/usr/bin/env bash
# =========================================================
# Verificación después de instalar o actualizar (docs/13-produccion.md §5).
# Uso:   ./verify.sh https://ventas.ejemplo.com [commit-esperado]
# Con certificado local (SITE_ADDRESS=localhost): VERIFY_INSECURE=1
# Termina en "VERIFICACIÓN: TODO CORRECTO" o dice qué falló (código 1).
# No crea ni cambia datos.
# =========================================================
set -uo pipefail

BASE="${1:?Uso: ./verify.sh https://tu-dominio [commit]}"
BASE="${BASE%/}"
EXPECTED_COMMIT="${2:-}"
CURL=(curl -s --max-time 15)
[[ "${VERIFY_INSECURE:-0}" == "1" ]] && CURL+=(-k)

failures=0
check() {   # check "descripción" condición...
    local name="$1"; shift
    if "$@"; then echo "  ✔ $name"; else echo "  ✘ $name"; failures=$((failures + 1)); fi
}
status_of() { "${CURL[@]}" -o /dev/null -w '%{http_code}' "$@"; }
header_of() { "${CURL[@]}" -o /dev/null -D - "$1" | tr -d '\r' | grep -i "^$2:" | head -1; }

echo "Verificando $BASE"

health="$("${CURL[@]}" "$BASE/actuator/health")"
check "estado del sistema UP (base de datos y disco)" grep -q '"status":"UP"' <<<"$health"
check "base de datos UP" grep -q '"db":{"status":"UP"}' <<<"$health"
check "disco UP" grep -q '"diskSpace":{"status":"UP"}' <<<"$health"

info="$("${CURL[@]}" "$BASE/actuator/info")"
version="$(sed -n 's/.*"version":"\([^"]*\)".*/\1/p' <<<"$info")"
commit="$(sed -n 's/.*"commit":"\([^"]*\)".*/\1/p' <<<"$info")"
check "versión publicada: ${version:-?} (commit ${commit:-?})" test -n "$version"
if [[ -n "$EXPECTED_COMMIT" ]]; then
    check "es la versión recién instalada ($EXPECTED_COMMIT)" test "$commit" == "$EXPECTED_COMMIT"
fi

home="$("${CURL[@]}" "$BASE/")"
check "la app carga" grep -q '<div id="root">' <<<"$home"
check "la app tiene política de seguridad (CSP)" grep -q 'Content-Security-Policy' <<<"$home"
check "rutas de la app (p. ej. /sales) abren la app" test "$(status_of "$BASE/sales")" == "200"
check "el service worker (modo sin conexión) está publicado" test "$(status_of "$BASE/sw.js")" == "200"
check "el service worker se revisa en cada visita" grep -qi 'no-cache' <<<"$(header_of "$BASE/sw.js" cache-control)"

check "la API responde a través del servidor web (401 sin sesión)" test "$(status_of "$BASE/api/products")" == "401"
check "el inicio de sesión llega al backend" test "$(status_of -X POST -H 'Content-Type: application/json' \
    -d '{"identifier":"verificacion-despliegue","password":"no-es-real-1"}' "$BASE/auth/login")" == "401"
check "Actuator no publica nada más (/actuator/env → 404)" test "$(status_of "$BASE/actuator/env")" == "404"
no_openapi() { ! grep -q '"openapi"' <<<"$("${CURL[@]}" "$BASE/v3/api-docs")"; }
check "documentación interactiva de la API (Swagger) no publicada" no_openapi

check "HTTPS obligatorio (HSTS)" test -n "$(header_of "$BASE/" strict-transport-security)"
check "cabecera X-Content-Type-Options" test -n "$(header_of "$BASE/" x-content-type-options)"
check "cabecera X-Frame-Options" test -n "$(header_of "$BASE/" x-frame-options)"

if [[ "$BASE" == https://* && "${VERIFY_HTTP_REDIRECT:-1}" == "1" ]]; then
    host="${BASE#https://}"; host="${host%%/*}"; host="${host%%:*}"
    location="$(curl -s --max-time 15 -o /dev/null -D - "http://${host}${VERIFY_HTTP_PORT:+:$VERIFY_HTTP_PORT}/" | tr -d '\r' | grep -i '^location:')"
    check "http:// redirige a https://" grep -qi 'location: https://' <<<"$location"
fi

echo
if [[ $failures -eq 0 ]]; then
    echo "VERIFICACIÓN: TODO CORRECTO"
else
    echo "VERIFICACIÓN: $failures COMPROBACIONES FALLARON"
    exit 1
fi
