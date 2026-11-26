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
| Copia completa de la base      | Cada 2 horas de 6:00 a 21:00 y al iniciar sesión (automático) | `scripts/backup/backup-db.ps1` |
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
   - `CopyTo`: **muy recomendado.** Una carpeta de OneDrive o Google Drive (o una USB). Si el disco del computador se daña o te lo roban, las copias de `BackupDir` se pierden con él. La misma política de retención (`RetentionDays` y `MinKeep`) se aplica también a esta carpeta, así que no se llena con el tiempo.

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

- **MantasGuajiras - Copias del día**: una copia cada 2 horas desde las 6:00 (6, 8, 10… 20), una copia final a las 21:00 **y otra cada vez que inicias sesión en Windows** (por ejemplo, cuando vuelve la luz y prendes el computador).
- **MantasGuajiras - Prueba semanal**: los domingos a las 21:30.

Si el computador está apagado a una hora programada, la copia se hace en cuanto se encienda.

Para otro horario, por ejemplo cada hora de 7 a. m. a 8 p. m. (los valores por defecto son `-EveryHours 2 -StartTime 06:00 -EndTime 21:00`):

```powershell
powershell -ExecutionPolicy Bypass -File scripts\backup\register-scheduled-tasks.ps1 -EveryHours 1 -StartTime 07:00 -EndTime 20:00
```

Volver a ejecutarlo reemplaza la programación anterior. Cada copia tarda pocos segundos y no interrumpe a quien esté vendiendo.

---

### 1.5. Cortes de luz: qué se pierde y qué no

**PostgreSQL no pierde las operaciones ya guardadas cuando se va la luz.** Antes de responder "venta registrada", escribe la operación en su diario en el disco (WAL). Al volver a arrancar, la reconstruye desde ahí.

| Situación cuando se va la luz                     | Qué pasa                                                                 |
| ------------------------------------------------- | ------------------------------------------------------------------------ |
| Venta que ya mostró "registrada"                  | **Se conserva.**                                                         |
| Venta que se estaba guardando en ese instante     | No queda nada a medias: o se guardó completa o no se guardó. Revisa el historial y regístrala de nuevo si no aparece. |
| Formulario a medio llenar en la pantalla          | Se pierde lo escrito (no se había enviado).                             |
| El disco se daña por el apagón (poco frecuente)   | Aquí es donde se usan las copias: se pierde lo hecho después de la última copia. Con copias cada 2 horas, como máximo 2 horas. |

Para que esta protección funcione, PostgreSQL debe tener activadas tres opciones que vienen activadas por defecto. Compruébalo una vez en pgAdmin (Query Tool) o en `psql`:

```sql
SHOW fsync;              -- debe decir: on
SHOW synchronous_commit; -- debe decir: on
SHOW full_page_writes;   -- debe decir: on
```

Si alguna dice `off`, cámbiala a `on` en `postgresql.conf` y reinicia el servicio de PostgreSQL.

**Recomendación de hardware:** una UPS (regulador con batería, conocida como "no break") para el computador y el módem. Con 10 a 15 minutos de batería se puede terminar la venta y apagar el equipo normalmente. Es la protección más efectiva contra los apagones.

---

## 2. ¿Cómo sé que está funcionando?

- **Historial:** `backup.log` dentro de la carpeta de copias. Cada copia y cada prueba dejan una línea con fecha.
- **Alerta:** si una copia o una prueba falla, aparece en el Escritorio el archivo **`ALERTA - Copia de seguridad MantasGuajiras.txt`** con el motivo. Bórralo cuando lo soluciones.
- **OneDrive:** si `CopyTo` es una carpeta de OneDrive, cada copia comprueba que OneDrive esté abierto (si está cerrado, las copias no suben a la nube). Si lo encuentra cerrado, lo abre y lo anota en `backup.log`; si no puede abrirlo, deja la ALERTA. La copia de esa hora sigue siendo válida.
- **Revisión mensual (2 minutos):** abre la carpeta de copias y confirma que hay archivos `mantas_AAAA-MM-DD_....dump` recientes, y que también están en `CopyTo`. Si `CopyTo` es OneDrive, entra a **onedrive.com** y confirma que esos mismos archivos están en la carpeta: es la única forma segura de saber que subieron. El estado de sincronización que muestra Windows no sirve para esto: en la prueba del 26/11/2026 marcaba "pendiente" archivos que ya estaban en la nube.

### Qué verifica la prueba de restauración

1. La huella SHA-256 del archivo (detecta archivos dañados o modificados). **Es obligatoria:** si falta el archivo `.sha256` o no coincide, la prueba falla. Solo para una copia antigua sin huella que sabes que es buena se puede forzar con `-AllowMissingHash` (en Linux, `ALLOW_MISSING_HASH=1`).
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

   El script comprueba primero la huella SHA-256: si falta o no coincide, se detiene (usa otra copia o, solo si sabes que es buena, agrega `-AllowMissingHash`). Por seguridad **nunca sobrescribe** una base existente: restaura en `mantas_guajiras_db_restaurada` y te pide escribir el nombre para confirmar.
5. En `backend/src/main/resources/application-dev.yml` cambia el final de `spring.datasource.url` por `/mantas_guajiras_db_restaurada`.
6. Arranca el backend y revisa inventario, últimas ventas y usuarios.
7. Cuando confirmes que todo está bien, puedes conservar la base dañada un tiempo por si hace falta revisarla, y borrarla después desde pgAdmin.

Las operaciones hechas **después** de la copia usada se pierden y hay que registrarlas de nuevo. Por eso las copias se hacen varias veces al día.

---

## 4. Antes de cada actualización del sistema

Antes de traer una nueva versión del código (`git pull`) que incluya migraciones nuevas (`V20__...sql`, etc.), haz una copia manual con `backup-db.ps1`. Si la migración sale mal, se puede volver al estado anterior.

### 4.1. Si la actualización sale mal (volver atrás)

1. Detén el backend.
2. Vuelve al código anterior: `git checkout <commit o rama anterior>`.
3. Restaura la copia hecha **antes** de actualizar con `restore-db.ps1` (en una base nueva) y apunta el backend a ella.
4. Arranca el backend anterior.

No se debe "deshacer" una migración a mano: la base restaurada ya está en la versión anterior.

### 4.2. Actualización a la versión 1.0.0-rc.1 (V19 → V21)

Probada el 24/11/2026 con la versión exacta anterior (commit `a6f5ada`, base en V19) y datos de un negocio: productos (incluidos dos con el **mismo nombre**), vendedor, ventas normales y con precio especial, compra, producción, personalizada con abonos y un ajuste **sin motivo**:

* V20 y V21 se aplican solas al arrancar (0,016 s); el arranque completo tarda ~6 s.
* Conteos, totales, inventario por producto e integridad: **idénticos** antes y después.
* Usuarios anteriores inician sesión; las ventas antiguas conservan su precio especial; el historial muestra los datos antiguos; las personalizadas antiguas reciben pagos; las ventas nuevas no se duplican al reintentar.
* **Nombres repetidos de antes:** el sistema los deja como están, pero al **editar** uno de ellos pide cambiarle el nombre. Para encontrarlos en pgAdmin:

  ```sql
  SELECT lower(name) AS nombre, count(*), string_agg(internal_code, ', ')
  FROM product GROUP BY lower(name) HAVING count(*) > 1;
  ```

* Los ajustes antiguos sin motivo se siguen viendo; los nuevos exigen motivo.

---

## 5. Linux / servidor

Con el kit de producción (`deploy/`), las copias ya vienen programadas en el servicio `backup`: al arrancar, cada 2 horas de 6:00 a 21:00 y prueba de restauración los domingos. Ver `docs/13-produccion.md` §6. Lo siguiente es para un servidor sin Docker.

```bash
# Contraseña en ~/.pgpass  (chmod 600 ~/.pgpass)
DB_NAME=mantas_guajiras_db BACKUP_DIR=/var/backups/mantasguajiras scripts/backup/backup-db.sh
DB_NAME=mantas_guajiras_db BACKUP_DIR=/var/backups/mantasguajiras scripts/backup/restore-test.sh
# Emergencia: restaura en una base NUEVA (pide escribir el nombre para confirmar)
scripts/backup/restore-db.sh /var/backups/mantasguajiras/mantas_AAAA-MM-DD_HHMMSS.dump mantas_guajiras_db_restaurada
```

Programación con `crontab -e`:

```text
0 6-20/2,21 * * *  DB_NAME=mantas_guajiras_db BACKUP_DIR=/var/backups/mantasguajiras /ruta/scripts/backup/backup-db.sh
30 21 * * 0 DB_NAME=mantas_guajiras_db BACKUP_DIR=/var/backups/mantasguajiras /ruta/scripts/backup/restore-test.sh
```

Además del disco del servidor, las copias deben llevarse a otro lugar (almacenamiento del proveedor, otro servidor o un servicio en la nube).


---

## 6. Objetivos de recuperación (RPO y RTO)

| Objetivo | Valor propuesto | Evidencia |
| -------- | --------------- | --------- |
| **RPO** — información que se puede perder como máximo | **2 horas** en horario de operación (6:00–21:00). Fuera de horario no hay operación. | Copias cada 2 h + copia al encender el equipo. Las operaciones confirmadas no se pierden por un corte de luz (sección 1.5). |
| **RTO** — tiempo máximo sin servicio | **1 hora** | Tiempo **técnico** medido: **7,5 s** (simulacro del 24/11). La hora cubre lo humano: darse cuenta, elegir la copia, cambiar la configuración y revisar. |

Estos valores deben aprobarse por la responsable del negocio y repetirse en el equipo real (el PC del negocio o el servidor definitivo), donde los tiempos pueden ser mayores.

## 7. Registro de simulacros

### 24/11/2026 — Pérdida total de la base (entorno de pruebas, Linux)

* **Datos:** volumen realista de ~3 años de operación: 33.003 ventas, 33.633 movimientos de inventario, compras, producción y personalizadas con pagos (30 MB en PostgreSQL).
* **Procedimiento:** copia con `backup-db.sh` → se **elimina la base** (`dropdb`) y el sistema deja de responder → `restore-db.sh` en una base nueva → backend apuntando a la base restaurada.

| Paso | Tiempo |
| ---- | ------ |
| Copia de seguridad (archivo de 4,6 MB) | 0,7 s |
| Prueba de restauración semanal | 0,9 s |
| Restaurar la base completa | 0,6 s |
| Arrancar el sistema sobre la base restaurada | 6,9 s |
| **Recuperación técnica total** | **7,5 s** |

* **Verificación:** conteos, totales e inventario por producto **idénticos** a los de antes del desastre; `integrity-check.sql` sin problemas; el vendedor inicia sesión y registra una venta nueva.
* **Resultado:** ✅ superado.
* **Pendiente:** repetir el simulacro en Windows con los scripts `.ps1` en el PC del negocio (hecho; ver abajo).

### 24/11/2026 — Restauración en el PC del negocio (Windows 11, PostgreSQL 18.4)

* **Datos:** copia de la base de trabajo en V20 (5 productos, 2 ventas, 18 movimientos, 3 personalizadas).
* **Procedimiento:** copia con `backup-db.ps1` → `restore-db.ps1` en una base nueva (`mantas_guajiras_simulacro`) → versión 1.0.0-rc.1 arrancada sobre ella en otro puerto, con `SPRING_DATASOURCE_URL`. La base de trabajo no se borró ni se modificó.

| Paso | Tiempo |
| ---- | ------ |
| Restaurar la base completa (incluye verificar la huella) | 1,5 s |
| Arrancar el sistema sobre la base restaurada | 10,0 s |
| **Recuperación técnica total** | **11,5 s** |

* **Verificación:** V21 se aplicó sin errores; `/actuator/health` en `UP`; conteos, total de ventas e inventario idénticos a los de la base de trabajo; `integrity-check.sql` sin problemas.
* **No se comprobó:** iniciar sesión y registrar una venta sobre la base restaurada. El primer arranque falló porque el puerto 8090 estaba ocupado por otro programa; se repitió en otro puerto.
* **Resultado:** ✅ superado.
