# =========================================================
# Configuración de las copias de seguridad (Windows).
# 1. Copia este archivo como backup.config.ps1 (misma carpeta).
# 2. Ajusta los valores.
# backup.config.ps1 NO se sube a git.
#
# La contraseña de PostgreSQL NO va aquí: va en el archivo
#   %APPDATA%\postgresql\pgpass.conf
# con una línea así (ver docs/09-copias-de-seguridad.md):
#   localhost:5432:*:postgres:TU_CONTRASEÑA
# =========================================================

$BackupConfig = @{

    # Carpeta "bin" de PostgreSQL (donde están pg_dump.exe y pg_restore.exe).
    PgBin         = 'C:\Program Files\PostgreSQL\17\bin'

    DbHost        = 'localhost'
    DbPort        = 5432
    DbUser        = 'postgres'
    Database      = 'mantas_guajiras_db'

    # Dónde se guardan las copias. FUERA de la carpeta del proyecto,
    # para que nunca terminen en un ZIP ni en git.
    BackupDir     = "$env:USERPROFILE\MantasGuajiras-backups"

    # Copias más antiguas que esto se borran...
    RetentionDays = 30
    # ...pero siempre se conservan al menos estas (las más recientes).
    # Con copias cada 2 horas, 50 copias cubren más de 3 días.
    MinKeep       = 50

    # Segunda copia fuera de este disco (MUY recomendado):
    # una carpeta de OneDrive/Google Drive o una memoria USB.
    # Déjalo en '' para desactivarlo.
    CopyTo        = ''
    # Ejemplo: CopyTo = "$env:USERPROFILE\OneDrive\MantasGuajiras-backups"

    # Base TEMPORAL que usa la prueba de restauración (se borra al final).
    RestoreTestDb = 'mantas_guajiras_restore_test'
}
