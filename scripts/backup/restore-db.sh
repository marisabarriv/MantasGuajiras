#!/usr/bin/env bash
# =========================================================
# RECUPERACIÓN (Linux / servidor). Equivale a restore-db.ps1.
# Restaura una copia en una base NUEVA; nunca sobrescribe una existente.
#
# Uso:
#   scripts/backup/restore-db.sh archivo.dump [base_destino]
# Por defecto la base destino es mantas_guajiras_db_restaurada.
# Pide escribir el nombre de la base para confirmar; en un procedimiento
# automatizado se puede pasar CONFIRM_DB=<nombre de la base destino>.
# La huella .sha256 es obligatoria (ALLOW_MISSING_HASH=1 solo para copias
# antiguas que se sabe que están bien).
# Conexión: PGHOST, PGPORT, PGUSER (contraseña en ~/.pgpass).
# =========================================================
set -euo pipefail

FILE="${1:-}"
TARGET="${2:-mantas_guajiras_db_restaurada}"
BACKUP_DIR="${BACKUP_DIR:-$HOME/mantasguajiras-backups}"
export PGHOST="${PGHOST:-localhost}" PGPORT="${PGPORT:-5432}" PGUSER="${PGUSER:-postgres}"

mkdir -p "$BACKUP_DIR"
log() { echo "$(date '+%Y-%m-%d %H:%M:%S')  $*" | tee -a "$BACKUP_DIR/backup.log"; }
die() { log "ERROR: $*"; exit 1; }

[[ -n "$FILE" && -f "$FILE" ]] || die "indica un archivo .dump existente."

if [[ -f "$FILE.sha256" ]]; then
    [[ "$(sha256sum "$FILE" | cut -d' ' -f1 | tr 'a-f' 'A-F')" == "$(tr -d '[:space:]' < "$FILE.sha256")" ]] \
        || die "la huella SHA-256 no coincide: el archivo está dañado. Usa otra copia."
elif [[ "${ALLOW_MISSING_HASH:-0}" != "1" ]]; then
    die "falta $FILE.sha256; sin huella no se puede comprobar la copia (ALLOW_MISSING_HASH=1 para forzar)."
fi

if [[ -n "$(psql --no-password -d postgres -Atc "SELECT 1 FROM pg_database WHERE datname = '$TARGET'")" ]]; then
    die "la base '$TARGET' ya existe. Elige otro nombre; este script nunca sobrescribe datos."
fi

echo "Se va a restaurar: $FILE"
echo "En la base NUEVA:  $TARGET"
ANSWER="${CONFIRM_DB:-}"
if [[ -z "$ANSWER" ]]; then
    read -r -p "Escribe el nombre de la base para confirmar: " ANSWER
fi
[[ "$ANSWER" == "$TARGET" ]] || { echo "Cancelado."; exit 1; }

createdb --no-password "$TARGET"
pg_restore --no-password --dbname "$TARGET" --no-owner --exit-on-error "$FILE"
log "RECUPERACIÓN: $FILE restaurada en '$TARGET'."
echo "Listo. Apunta el backend a la base '$TARGET' (DB_URL o application-dev.yml) y reinícialo."
