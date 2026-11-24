# Rendimiento y monitoreo

Fase 6 de la certificación (`docs/10-certificacion.md`, secciones 13 y 15). Medido el 24/11/2026 con la versión 1.0.0-rc.1.

---

## 1. Resumen

| Qué | Antes | Después |
| --- | --- | --- |
| Historial de ventas (3 años de datos) | 1,7–2,6 s y **26 MB** por consulta, todas las ventas | **0,09 s y 1,1 MB** (últimos 30 días) |
| Historial de movimientos | 1,0–1,4 s y **37 MB** | **0,07 s y 1,4 MB** |
| Consultas a la base por historial | 1 + varias por cada venta (N+1) | **3 en total** (personalizadas: 13, en bloques de 100) |
| Prueba de carga, 15 usuarios | **123 errores 500**; el servidor se quedó sin memoria | **0 errores**; p95 ≤ 76 ms |
| Prueba de carga, 40 usuarios | — | **0 errores de servidor**; p95 ≤ 270 ms |
| Página de Ventas, 3 meses en pantalla | 17,7 s (miles de filas de una vez) | **0,4 s** (de a 100, con "Ver más") |
| Estado del sistema | Sin forma de consultarlo | `GET /actuator/health` (base de datos y disco) |
| Log | Solo en la consola | Archivo con rotación + una línea por operación |

## 2. Prueba de carga

### 2.1 Datos

Base de prueba con **3 años de operación** (`scripts/perf/`):

- 40 mantas y 12 telas, 5 vendedores.
- 33.000 ventas (66.000 líneas de detalle).
- 1.000 personalizadas con 1.750 pagos.
- Reposición semanal, unos 74.000 movimientos. La base ocupa 47 MB.
- `integrity-check.sql` en cero antes y después de cada prueba: el inventario coincide con los movimientos y nunca queda negativo.

### 2.2 Escenario

Usuarios simultáneos (`load.py`):

- **Vendedores (80 %)**: en ciclo, consultan productos, inventario y categorías, registran una venta y "piensan" entre 0,5 y 1,5 s. Si el producto se agota, lo reponen con un ajuste, como haría la tienda.
- **Administradores (20 %)**: consultan el historial de ventas, las personalizadas, los movimientos (todos y los de un producto) y esperan entre 1 y 3 s.

La tienda tiene pocos vendedores. Se probó con **15 y 40 usuarios a la vez**, muy por encima del uso esperado (REN-11: hasta 10 personas conectadas).

### 2.3 Resultados (tiempos en ms)

**Antes, 15 usuarios, 60 s:**

| Operación | p50 | p95 | máx | Errores |
| --- | ---: | ---: | ---: | ---: |
| Historial de ventas | 1.947 | 2.647 | 4.441 | 2 |
| Historial de movimientos | 1.345 | 5.170 | 5.603 | 4 |
| Registrar venta | 11 | 84 | 1.129 | 22 |
| Productos | 6 | 124 | 3.576 | 40 |
| **Total** | | | | **123 respuestas 500** |

**Después, 40 usuarios, 90 s** (12.400 operaciones, 2.771 ventas):

| Operación | p50 | p95 | máx | Errores de servidor |
| --- | ---: | ---: | ---: | ---: |
| Productos | 2 | 18 | 364 | 0 |
| Inventario | 3 | 14 | 380 | 0 |
| Registrar venta | 5 | 25 | 772 | 0 |
| Ajuste de inventario | 5 | 37 | 50 | 0 |
| Historial de ventas (30 días) | 147 | 268 | 1.526 | 0 |
| Historial de movimientos (30 días) | 103 | 183 | 605 | 0 |
| Movimientos de un producto | 6 | 23 | 159 | 0 |
| Personalizadas | 20 | 58 | 448 | 0 |
| Iniciar sesión | 835 | 1.092 | 1.109 | 0 |

- Las 24 ventas rechazadas fueron **rechazos correctos** ("No hay suficiente inventario… Disponible: 0"), no fallas.
- Los historiales pesan más que en el resumen porque la propia prueba ya había registrado miles de ventas ese día. Con el volumen real de la tienda, 30 días son alrededor de 1 MB.
- Iniciar sesión tarda casi 1 s con 40 a la vez, a propósito: la contraseña se comprueba con BCrypt, que es lento para frenar a quien intente adivinarla. Solo ocurre al entrar.

### 2.4 Hallazgos y correcciones

1. **El servidor se quedaba sin memoria** (`OutOfMemoryError`): cada consulta del historial de movimientos armaba 37 MB, y tres administradores a la vez bastaban.
   - Después del error, un hilo del servidor quedó dañado ("Pre-bound JDBC Connection found") y **falló 121 peticiones seguidas** de cualquier pantalla.
   - Corrección: historiales acotados (punto 2) y arranque con `-XX:+ExitOnOutOfMemoryError` (§4). Si la memoria se agota, el servidor se reinicia en vez de quedar a medias.
2. **Historiales sin límite.** `GET /api/sales` y `GET /api/inventory-movements` devolvían todo. Ahora consultan siempre un periodo:
   - Sin fechas: los **últimos 30 días**.
   - Como máximo **93 días** (unos 3 meses). Si se pide más, responde `400` con *"Elige un periodo de máximo 93 días (unos 3 meses)."*
   - Solo `to`: los 30 días que terminan ese día. Solo `from`: hasta hoy.
   - En la app:
     - **Ventas** tiene atajos (Hoy, 7 días, 30 días, 3 meses) y fechas *Desde / Hasta*.
     - El **historial de movimientos** tiene fechas, también en la ventana de un producto.
3. **N+1 en el historial de ventas.** El detalle de cada venta (productos y unidades) se cargaba con consultas aparte.
   - Ahora llega en la misma consulta.
   - Además, `default_batch_fetch_size: 100` carga en bloque las relaciones de todas las listas (usuario, tipo de movimiento, pagos…).
4. **Pantalla congelada con listas largas.** Ventas y movimientos se dibujan de a 100 con el botón **"Ver más (quedan N)"**.

Compras, producción y personalizadas siguen completas: crecen poco (≈1 por día) y las personalizadas con saldo pendiente deben verse siempre. Con 1.000 personalizadas la lista tarda 20 ms y pesa 0,6 MB. Revisar al superar unas 5.000.

### 2.5 Memoria

| Medida (40 usuarios) | Valor |
| --- | --- |
| Memoria Java usada (heap) | 273 MB de 512 MB |
| Memoria total del proceso | ≈ 850 MB |
| Arranque | 5,6 s |

Con `-Xmx512m` sobra margen. El equipo donde se instale debe tener al menos **2 GB de RAM libres** para el backend y PostgreSQL.

### 2.6 Límites conocidos (REN-15)

- Hasta 93 días por consulta de historial. Para periodos más largos, consultar por partes.
- Probado hasta 40 usuarios simultáneos y 3 años de datos (33.000 ventas). Sin errores y con tiempos bajos.
- La primera consulta después de arrancar tarda más (≈0,3 s) mientras el servidor "calienta".
- Fotos de perfil: se guardan tal como se suben (máximo 20 MB), sin reducirlas (REN-08, pendiente para la Fase 7).

## 3. Monitoreo

### 3.1 Estado del sistema

`GET /actuator/health` es público y no muestra detalles internos:

```json
{"status":"UP","components":{"db":{"status":"UP"},"diskSpace":{"status":"UP"},"ping":{"status":"UP"}, ...}}
```

- `db`: PostgreSQL responde.
- `diskSpace`: quedan más de 2 GB libres en el disco del backend. Se cambia con `DISK_MIN_FREE` (por ejemplo `5GB`).
- Si algo falla, `status` es `DOWN` y la respuesta es `503`.
- Actuator no publica nada más: `/actuator/env`, `/actuator/metrics` y los demás piden sesión, y aun con sesión no existen.
- Además, **cada 5 minutos** el backend revisa ese mismo estado:
  - Si algo falla, escribe `ERROR … Estado del sistema: DOWN (db: DOWN, …)`.
  - Al recuperarse, escribe `Estado del sistema recuperado: UP`.

Para revisarlo a mano, abrir en el navegador `http://localhost:8080/actuator/health`.

### 3.2 Log en archivo

- Ubicación: `backend/logs/mantasguajiras.log`. Se cambia con `LOG_FILE`.
- Rotación: cada archivo hasta 10 MB. Se guardan 30 días y nunca más de 300 MB en total; lo más antiguo se borra solo.
- Crecimiento medido: unos 160 bytes por operación. Con unas 300 operaciones al día, **menos de 2 MB al mes**.
- `backend/logs/` está en `.gitignore`.

### 3.3 Qué queda registrado

Una línea por cada operación que **cambia datos**, con usuario, resultado y duración. Nunca se registran contraseñas, tokens, cuerpos de las peticiones ni parámetros de la URL.

```text
INFO  OK POST /api/sales 201 12 ms usuario=vend1
INFO  OK POST /api/sales 201 48 ms usuario=vend2 [sin conexión]
WARN  Rechazada POST /api/sales 400 3 ms usuario=vend3 [sin conexión]: No hay suficiente inventario de Manta modelo 11. Disponible: 0, solicitado: 1.
WARN  Rechazada POST /api/inventory/adjustments 409 9 ms usuario=vend1: La operación choca con un registro existente...
WARN  Rechazada POST /auth/login 401 120 ms usuario=-: Usuario o contraseña incorrectos.
WARN  Lenta GET /api/sales 1234 ms usuario=admin
ERROR Error interno [a1b2c3d4] en POST /api/purchases   (con el detalle técnico)
ERROR Estado del sistema: DOWN (db: DOWN, diskSpace: UP)
WARN  Inicio de sesión bloqueado 15 min para 'vend1' tras 5 intentos fallidos
```

- `[sin conexión]`: la venta llegó desde la cola de ventas hechas sin internet (OBS-04). Así los rechazos al sincronizar también quedan en el servidor, no solo en el dispositivo.
- `Rechazada`: cualquier 4xx de una operación, con el mismo motivo que vio el usuario. Incluye los conflictos 409 (OBS-05).
- `Lenta`: cualquier petición que tarde más de 1 s (`SLOW_REQUEST_MS`) (OBS-14).
- Las consultas normales (ver productos, inventario…) no se registran, para no llenar el log.

### 3.4 Conexiones a la base

| Ajuste | Valor | Para qué |
| --- | --- | --- |
| `maximum-pool-size` | 10 (`DB_POOL_SIZE`) | Atendió 40 usuarios simultáneos sin esperas |
| `connection-timeout` | 10 s | Si PostgreSQL no responde, error claro en 10 s en vez de 30 |
| `leak-detection-threshold` | 60 s | Aviso en el log si una conexión queda tomada |

## 4. Arranque en producción

```bash
java -Xmx512m -XX:+ExitOnOutOfMemoryError -jar backend-1.0.0-rc.1.jar
```

- `-Xmx512m`: memoria máxima de Java (§2.5).
- `-XX:+ExitOnOutOfMemoryError`: si algún día se agota la memoria, el proceso termina en vez de seguir respondiendo con errores (§2.4).
- Para que **vuelva a arrancar solo**, instalarlo como servicio que se reinicie automáticamente: un servicio de Windows (por ejemplo con WinSW o NSSM) o `systemd` en Linux, con `Restart=always`. Se define en la Fase 7 (`docs/06-despliegue.md`).

## 5. Repetir la prueba de carga

> **Nunca contra la base de trabajo.** Crea productos, vendedores, miles de ventas y ajustes.

1. Crear una base vacía, por ejemplo `createdb -U postgres mantas_perf`.
2. Arrancar el backend contra ella con el perfil `prod`: las mismas variables que en `frontend/e2e/README.md`, con:
   - `INITIAL_ADMIN_USERNAME=adminperf`, `INITIAL_ADMIN_PASSWORD=Prueba123`;
   - `LOGIN_MAX_ATTEMPTS=1000`, para que los muchos inicios de sesión de la prueba no bloqueen;
   - `java -Xmx512m -XX:+ExitOnOutOfMemoryError -jar …`.
3. Cargar los datos:

   ```bash
   python3 scripts/perf/seed.py http://localhost:8080
   ```
   ```bash
   psql -U postgres -d mantas_perf -f scripts/perf/volume.sql
   ```
   ```bash
   psql -U postgres -d mantas_perf -f scripts/backup/integrity-check.sql
   ```

4. Ejecutar 90 segundos con 40 usuarios:

   ```bash
   python3 scripts/perf/load.py http://localhost:8080 90 40
   ```

5. Volver a ejecutar `integrity-check.sql`: todo debe seguir en 0.

## 6. Respuesta a incidentes (OBS-15)

Primero: abrir `http://localhost:8080/actuator/health` y el log `backend/logs/mantasguajiras.log`.

| Síntoma | Qué revisar | Qué hacer |
| --- | --- | --- |
| La app no carga o dice "sin conexión" en todos los equipos | ¿Responde `/actuator/health`? | Si no responde, el backend está apagado: arrancarlo (§4). Mientras tanto, los vendedores pueden seguir vendiendo sin conexión: las ventas se envían solas al volver. |
| `health` dice `db: DOWN` | ¿Está encendido el servicio de PostgreSQL? | Iniciar el servicio `postgresql-x64-16` (Windows: *Servicios*). Si no arranca, `docs/09-copias-de-seguridad.md` §4 (restaurar). |
| `health` dice `diskSpace: DOWN` | Espacio libre del disco | Liberar espacio. Las copias viejas se borran solas; revisar descargas y la papelera. **Sin disco, PostgreSQL deja de guardar.** |
| Un usuario ve "Error interno… Código de referencia: a1b2c3d4" | Buscar `a1b2c3d4` en el log | La línea tiene el detalle técnico. Si se repite, reportarlo con esa línea. |
| Un vendedor dice que una venta sin conexión "no pasó" | Buscar `[sin conexión]` y su usuario en el log | `Rechazada … : motivo` dice por qué (normalmente falta de inventario). Ajustar el inventario y pulsar **Reintentar** en el dispositivo. |
| Todo va lento | Buscar `Lenta` en el log | Ver qué pantalla es. Si es un historial, consultar un periodo más corto. |
| Alguien no puede entrar | Buscar `Inicio de sesión bloqueado` | Espera 15 min, o un administrador le restablece la contraseña. |
| El backend se cerró solo | Buscar `OutOfMemoryError` en la consola o en el log | Volverá a arrancar si está como servicio (§4). Si se repite, avisar a desarrollo. |
| Se perdió o dañó información | — | `docs/09-copias-de-seguridad.md` §4 (restaurar la última copia). |
