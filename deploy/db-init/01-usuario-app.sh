#!/bin/bash
# Se ejecuta UNA sola vez, al crear la base por primera vez.
# La app se conecta con un usuario propio, dueño solo de su base y sin
# permisos de administrador de PostgreSQL (docs/06 §14.3).
set -euo pipefail
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres \
     -v app_user="$APP_DB_USER" -v app_password="$APP_DB_PASSWORD" -v db="$POSTGRES_DB" <<'SQL'
CREATE USER :"app_user" WITH PASSWORD :'app_password';
ALTER DATABASE :"db" OWNER TO :"app_user";
\connect :"db"
ALTER SCHEMA public OWNER TO :"app_user";
SQL
