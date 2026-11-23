#!/usr/bin/env bash
# =========================================================
# Copia de seguridad (Linux / servidor). Equivale a backup-db.ps1.
# Variables (con valores por defecto):
#   PGHOST=localhost PGPORT=5432 PGUSER=postgres  (contraseña en ~/.pgpass)
#   DB_NAME=mantas_guajiras_db
#   BACKUP_DIR=$HOME/mantasguajiras-backups
#   RETENTION_DAYS=30 MIN_KEEP=7 COPY_TO=   (segunda copia, opcional)
# Programar con cron, por ejemplo cada día a las 21:00:
#   0 21 * * * /ruta/scripts/backup/backup-db.sh
# =========================================================
set -euo pipefail

DB_NAME="${DB_NAME:-mantas_guajiras_db}"
BACKUP_DIR="${BACKUP_DIR:-$HOME/mantasguajiras-backups}"
RETENTION_DAYS="${RETENTION_DAYS:-30}"
MIN_KEEP="${MIN_KEEP:-7}"
COPY_TO="${COPY_TO:-}"
export PGHOST="${PGHOST:-localhost}" PGPORT="${PGPORT:-5432}" PGUSER="${PGUSER:-postgres}"

mkdir -p "$BACKUP_DIR"
LOG="$BACKUP_DIR/backup.log"
log() { echo "$(date '+%Y-%m-%d %H:%M:%S')  $*" | tee -a "$LOG"; }
trap 'log "ERROR: la copia de seguridad falló (línea $LINENO)."' ERR

FILE="$BACKUP_DIR/mantas_$(date '+%Y-%m-%d_%H%M%S').dump"
log "Iniciando copia de '$DB_NAME' -> $FILE"

pg_dump --no-password --format=custom --compress=9 --file "$FILE" "$DB_NAME"
pg_restore --list "$FILE" > /dev/null
sha256sum "$FILE" | cut -d' ' -f1 | tr 'a-f' 'A-F' > "$FILE.sha256"
log "Copia correcta ($(du -h "$FILE" | cut -f1))."

if [[ -n "$COPY_TO" ]]; then
    mkdir -p "$COPY_TO"
    cp "$FILE" "$FILE.sha256" "$COPY_TO/"
    log "Segunda copia guardada en $COPY_TO."
fi

# Conserva siempre las MIN_KEEP más recientes; de las demás borra las viejas.
ls -1t "$BACKUP_DIR"/mantas_*.dump | tail -n +"$((MIN_KEEP + 1))" | while read -r old; do
    if [[ -n "$(find "$old" -mtime +"$RETENTION_DAYS")" ]]; then
        rm -f "$old" "$old.sha256"
        log "Copia antigua eliminada: $(basename "$old")"
    fi
done
