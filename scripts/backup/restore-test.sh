#!/usr/bin/env bash
# =========================================================
# Prueba de restauración (Linux / servidor). Equivale a restore-test.ps1.
# Restaura la última copia (o la indicada como argumento) en una base
# TEMPORAL, verifica integridad, compara conteos y la borra.
# Uso: scripts/backup/restore-test.sh [archivo.dump]
# =========================================================
set -uo pipefail

DB_NAME="${DB_NAME:-mantas_guajiras_db}"
BACKUP_DIR="${BACKUP_DIR:-$HOME/mantasguajiras-backups}"
TEST_DB="${RESTORE_TEST_DB:-mantas_guajiras_restore_test}"
export PGHOST="${PGHOST:-localhost}" PGPORT="${PGPORT:-5432}" PGUSER="${PGUSER:-postgres}"
HERE="$(cd "$(dirname "$0")" && pwd)"

mkdir -p "$BACKUP_DIR"
LOG="$BACKUP_DIR/backup.log"
log() { echo "$(date '+%Y-%m-%d %H:%M:%S')  $*" | tee -a "$LOG"; }

[[ "$TEST_DB" == "$DB_NAME" ]] && { log "ERROR: RESTORE_TEST_DB no puede ser la base de trabajo."; exit 1; }

FILE="${1:-$(ls -1t "$BACKUP_DIR"/mantas_*.dump 2>/dev/null | head -1)}"
[[ -z "$FILE" || ! -f "$FILE" ]] && { log "ERROR: no hay copia para probar en $BACKUP_DIR."; exit 1; }

ok=1
fail() { log "  FALLA  $*"; ok=0; }
trap 'dropdb --no-password --if-exists "$TEST_DB" 2>/dev/null' EXIT

log "PRUEBA DE RESTAURACIÓN de $FILE"

if [[ -f "$FILE.sha256" ]]; then
    [[ "$(sha256sum "$FILE" | cut -d' ' -f1 | tr 'a-f' 'A-F')" == "$(tr -d '[:space:]' < "$FILE.sha256")" ]] \
        && log "Huella SHA-256 correcta." || fail "la huella SHA-256 no coincide (archivo dañado)"
fi

dropdb --no-password --if-exists "$TEST_DB" 2>/dev/null
createdb --no-password "$TEST_DB" || { log "ERROR: createdb falló."; exit 1; }
if ! pg_restore --no-password --dbname "$TEST_DB" --no-owner --exit-on-error "$FILE"; then
    log "ERROR: pg_restore falló."; exit 1
fi
log "Copia restaurada en la base temporal '$TEST_DB'."

while IFS='|' read -r check problems; do
    [[ -z "$check" ]] && continue
    [[ "$problems" == "0" ]] && log "  OK     $check" || fail "$check: $problems problema(s)"
done < <(psql --no-password -d "$TEST_DB" -At -F '|' -f "$HERE/integrity-check.sql")

COUNTS="SELECT 'product', COUNT(*) FROM product UNION ALL SELECT 'inventory_movement', COUNT(*) FROM inventory_movement
 UNION ALL SELECT 'sale', COUNT(*) FROM sale UNION ALL SELECT 'purchase', COUNT(*) FROM purchase
 UNION ALL SELECT 'production', COUNT(*) FROM production UNION ALL SELECT 'orders', COUNT(*) FROM orders
 UNION ALL SELECT 'app_user', COUNT(*) FROM app_user"
declare -A live
while IFS='|' read -r t n; do live[$t]=$n; done < <(psql --no-password -d "$DB_NAME" -At -F '|' -c "$COUNTS")
log "  $(printf '%-20s %10s %10s' Tabla Copia Actual)"
while IFS='|' read -r t n; do
    note=""
    (( n > ${live[$t]:-0} )) && { note="  <- la copia tiene MÁS registros: revisar"; ok=0; }
    (( n < ${live[$t]:-0} )) && note="  (operaciones posteriores a la copia)"
    log "  $(printf '%-20s %10s %10s' "$t" "$n" "${live[$t]:-?}")$note"
done < <(psql --no-password -d "$TEST_DB" -At -F '|' -c "$COUNTS")

if (( ok )); then log "RESULTADO: la copia se restauró y pasó todas las verificaciones."; exit 0; fi
log "RESULTADO: LA PRUEBA DE RESTAURACIÓN FALLÓ."; exit 1
