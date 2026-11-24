#!/usr/bin/env bash
# =========================================================
# Copias automáticas en el servidor (servicio "backup" de docker-compose).
#  - Una copia al arrancar (por ejemplo, justo después de actualizar).
#  - Cada 2 horas de 6:00 a 20:00 y a las 21:00 (BACKUP_HOURS).
#  - Los domingos a las 21:00, además, prueba de restauración.
# Usa scripts/backup/backup-db.sh y restore-test.sh (los mismos de siempre).
# Si algo falla, queda "ALERTA" en `docker compose logs backup` y en
# backups/backup.log.
# =========================================================
set -uo pipefail
# Copias legibles solo por su dueño (root en el servidor).
umask 077

HOURS=" ${BACKUP_HOURS:-6 8 10 12 14 16 18 20 21} "
last=""

copy() {
    /scripts/backup-db.sh || echo "ALERTA: la copia de seguridad FALLÓ $(date '+%F %T')" >&2
}

until pg_isready -q; do sleep 5; done
copy

while true; do
    hour=$(date +%-H)
    key=$(date +%F-%H)
    if [[ "$HOURS" == *" $hour "* && "$key" != "$last" ]]; then
        last="$key"
        copy
        if [[ "$(date +%u)" == "7" && "$hour" == "21" ]]; then
            /scripts/restore-test.sh || echo "ALERTA: la prueba de restauración FALLÓ $(date '+%F %T')" >&2
        fi
    fi
    sleep 60
done
