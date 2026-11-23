# Copias de seguridad y restauración

| Campo                    | Valor                          |
| ------------------------ | ------------------------------ |
| **Proyecto**             | Mantas Guajiras                |
| **Documento**            | Copias de seguridad            |
| **Código**               | DOC-09                         |
| **Versión**              | v1.0.0-rc.1                    |
| **Estado**               | Versión candidata              |
| **Responsable**          | Equipo de Desarrollo           |
| **Última actualización** | 23/11/2026                     |

---

## Resumen

| Qué                            | Cuándo                         | Script                                   |
| ------------------------------ | ------------------------------ | ---------------------------------------- |
| Copia completa de la base      | Todos los días (automático)    | `scripts/backup/backup-db.ps1`           |
| Prueba de que la copia sirve   | Cada domingo (automático)      | `scripts/backup/restore-test.ps1`        |
| Recuperar datos tras un daño   | Solo en una emergencia         | `scripts/backup/restore-db.ps1`          |
| Programar las dos primeras     | Una sola vez                   | `scripts/backup/register-scheduled-tasks.ps1` |

Una copia que nunca se ha restaurado **no es una copia confiable**. Por eso, además de copiar, cada semana se restaura la última copia en una base temporal y se verifica.

Los scripts `.sh` de la misma carpeta hacen lo mismo en Linux, para cuando el sistema se instale en un servidor.

---

## 1. Configuración (una sola vez)

### 1.1. Contraseña de PostgreSQL sin escribirla en los scripts

PostgreSQL lee las contraseñas del archivo `pgpass.conf`. Así ningún script contiene la contraseña.

1. Presiona `Windows + R`, escribe `%APPDATA%` y presiona Enter.
2. Crea la carpeta `postgresql` si no existe.
3. Dentro, crea el archivo `pgpass.conf` (ojo: que no quede como `pgpass.conf.txt`) con una línea:

   ```text
   localhost:5432:*:postgres:TU_CONTRASEÑA_DE_POSTGRES
   ```

   Formato: `servidor:puerto:base:usuario:contraseña`. El `*` vale para todas las bases (la de trabajo y la temporal de pruebas).

### 1.2. Archivo de configuración

1. En la carpeta `scripts\backup` copia `backup.config.example.ps1` y renombra la copia como `backup.config.ps1`.
2. Ábrela en VS Code y revisa:
   - `PgBin`: la carpeta `bin` de tu PostgreSQL. Compruébala en el Explorador; por ejemplo `C:\Program Files\PostgreSQL\17\bin` (el número depende de tu versión).
   - `Database`: el nombre de tu base (por defecto `mantas_guajiras_db`).
   - `BackupDir`: dónde se guardan las copias. Por defecto `C:\Users\TU_USUARIO\MantasGuajiras-backups`, **fuera del proyecto**, para que nunca terminen en un ZIP ni en git.
   - `CopyTo`: **muy recomendado.** Una carpeta de OneDrive o Google Drive (o una USB). Si el disco del computador se daña o te lo roban, las copias de `BackupDir` se pierden con él.

`backup.config.ps1` está en `.gitignore` y no se sube.

### 1.3. Primera copia y primera prueba (manual)

En la terminal de VS Code (PowerShell), desde la carpeta del proyecto:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\backup\backup-db.ps1
```

```powershell
powershell -ExecutionPolicy Bypass -File scripts\backup\restore-test.ps1
```

La prueba debe terminar con:

```text
RESULTADO: la copia se restauró y pasó todas las verificaciones.
```

### 1.4. Programar las copias automáticas

```powershell
powershell -ExecutionPolicy Bypass -File scripts\backup\register-scheduled-tasks.ps1
```

Crea dos tareas en el **Programador de tareas** de Windows:

- **MantasGuajiras - Copia diaria**: todos los días a las 21:00.
- **MantasGuajiras - Prueba semanal**: los domingos a las 21:30.

Si el computador está apagado a esa hora, la tarea corre en cuanto se encienda. Para otra hora: `... register-scheduled-tasks.ps1 -BackupTime 19:00 -TestTime 19:30`.

---

## 2. ¿Cómo sé que está funcionando?

- **Historial:** `backup.log` dentro de la carpeta de copias. Cada copia y cada prueba dejan una línea con fecha.
- **Alerta:** si una copia o una prueba falla, aparece en el Escritorio el archivo **`ALERTA - Copia de seguridad MantasGuajiras.txt`** con el motivo. Bórralo cuando lo soluciones.
- **Revisión mensual (2 minutos):** abre la carpeta de copias y confirma que hay archivos `mantas_AAAA-MM-DD_....dump` recientes, y que también están en `CopyTo`.

### Qué verifica la prueba de restauración

1. La huella SHA-256 del archivo (detecta archivos dañados o modificados).
2. Que `pg_restore` pueda restaurarlo completo en una base temporal.
3. Integridad de los datos restaurados (`scripts/backup/integrity-check.sql`):
   - el inventario de cada producto es igual a la suma de sus movimientos;
   - ningún inventario es negativo;
   - toda venta tiene productos;
   - no hay migraciones fallidas;
   - existe al menos un administrador activo.
4. Compara la cantidad de registros con la base actual. Que la base actual tenga **más** es normal (ventas hechas después de la copia). Que la copia tenga **más** que la base actual indica un problema: algo se borró.

La base temporal se elimina al terminar. **Tu base de trabajo nunca se modifica.**

---

## 3. Emergencia: recuperar los datos

1. **No sigas registrando operaciones** hasta terminar.
2. Detén el backend.
3. Elige la copia (la más reciente que pasó la prueba; revisa `backup.log`).
4. Restaura en una base **nueva**:

   ```powershell
   powershell -ExecutionPolicy Bypass -File scripts\backup\restore-db.ps1 -BackupFile "C:\Users\TU_USUARIO\MantasGuajiras-backups\mantas_2026-11-23_210000.dump"
   ```

   Por seguridad, el script **nunca sobrescribe** una base existente: restaura en `mantas_guajiras_db_restaurada` y te pide escribir el nombre para confirmar.
5. En `backend/src/main/resources/application-dev.yml` cambia el final de `spring.datasource.url` por `/mantas_guajiras_db_restaurada`.
6. Arranca el backend y revisa inventario, últimas ventas y usuarios.
7. Cuando confirmes que todo está bien, puedes conservar la base dañada un tiempo por si hace falta revisarla, y borrarla después desde pgAdmin.

Las operaciones hechas **después** de la copia usada se pierden y hay que registrarlas de nuevo. Por eso la copia es diaria.

---

## 4. Antes de cada actualización del sistema

Antes de traer una nueva versión del código (`git pull`) que incluya migraciones nuevas (`V20__...sql`, etc.), haz una copia manual con `backup-db.ps1`. Si la migración sale mal, se puede volver al estado anterior.

---

## 5. Linux / servidor (futuro)

```bash
# Contraseña en ~/.pgpass  (chmod 600 ~/.pgpass)
DB_NAME=mantas_guajiras_db BACKUP_DIR=/var/backups/mantasguajiras scripts/backup/backup-db.sh
DB_NAME=mantas_guajiras_db BACKUP_DIR=/var/backups/mantasguajiras scripts/backup/restore-test.sh
```

Programación con `crontab -e`:

```text
0 21 * * *  DB_NAME=mantas_guajiras_db BACKUP_DIR=/var/backups/mantasguajiras /ruta/scripts/backup/backup-db.sh
30 21 * * 0 DB_NAME=mantas_guajiras_db BACKUP_DIR=/var/backups/mantasguajiras /ruta/scripts/backup/restore-test.sh
```

Además del disco del servidor, las copias deben llevarse a otro lugar (almacenamiento del proveedor, otro servidor o un servicio en la nube).
