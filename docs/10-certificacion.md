# Matriz de certificación 10/10

| Campo                    | Valor                          |
| ------------------------ | ------------------------------ |
| **Proyecto**             | Mantas Guajiras                |
| **Documento**            | Matriz de certificación        |
| **Código**               | DOC-10                         |
| **Versión**              | v1.0.0-rc.1                    |
| **Estado**               | En curso                       |
| **Última actualización** | 25/11/2026 (auditoría externa) |

---

## Cómo leer este documento

Cada criterio tiene **prioridad**, **estado** y **evidencia**. Un criterio solo está **Verificado** si hay una prueba ejecutada, un registro o una revisión documentada; que el código "parezca implementarlo" no basta.

| Estado | Significado |
| ------ | ----------- |
| ✅ Verificado | Prueba ejecutada o revisión documentada con resultado correcto. |
| ⏳ Pendiente | Falta la comprobación. Si dice *"implementado"*, el código existe pero no hay prueba. |
| ❌ Fallido | Defecto o carencia confirmada que hay que corregir. |
| ➖ No aplica | Fuera del alcance acordado. |

| Prioridad | Significado |
| --------- | ----------- |
| **B** | Bloqueador absoluto (sección 21 del checklist). |
| **A** | Alta: debe resolverse antes de usar datos reales. |
| **M** | Media: antes de declarar 10/10. |
| **Bj** | Baja: mejora. |

### Evidencias citadas

| Clave | Evidencia | Dónde |
| ----- | --------- | ----- |
| `T-ctx` | `BackendApplicationTests`: las 22 migraciones desde una base vacía y validación del esquema | `backend/src/test` |
| `T-perm` | `RolePermissionsTest` (9 pruebas HTTP por rol) | `backend/src/test` |
| `T-conc` | `ConcurrentSaleTest` (6): concurrencia, rechazo, rollback, idempotencia, cambio de unidad | `backend/src/test` |
| `T-prod` | `ProductRulesTest` (11): categoría ↔ unidad, enteros, precios | `backend/src/test` |
| `T-img` | `ImageSignatureTest` (6): firma real de imágenes | `backend/src/test` |
| `T-sale` | `SalePricingTest` (9): caso $80.000 → $70.000, varios productos, precio histórico, rechazos, producto desactivado, ventas inmodificables | `backend/src/test` |
| `T-com` | `PurchaseFlowTest` (6): entradas, total, comprable/activo, sin cambios parciales, reintento, inmodificables | `backend/src/test` |
| `T-pro` | `ProductionFlowTest` (5): consumo de tela, mantas, insuficiente, validaciones, reintento | `backend/src/test` |
| `T-ord` | `CustomOrderFlowTest` (6): abono, pagos, saldo, pagada, límites, reintentos de pedido y pago | `backend/src/test` |
| `T-mov` | `MovementFilterTest` (3): ajuste 15 → 12 con motivo y usuario; filtros por tipo, origen, usuario y fechas | `backend/src/test` |
| `T-name` | `ProductNameTest` (4): nombres de producto únicos | `backend/src/test` |
| `T-sim` | `SimultaneousRetryTest` (2): misma venta o compra enviada 4 veces a la vez | `backend/src/test` |
| `T-auth` | `AuthenticationSecurityTest` (13): inicio de sesión real, tokens, usuarios desactivados, permisos por HTTP, registro y su límite, bloqueo por intentos, desbloqueo al restablecer la contraseña | `backend/src/test` |
| `T-lock` | `LoginAttemptServiceTest` (12): límites por cuenta, por conexión y por registro; sobreviven al reinicio; nadie bloquea a otra persona; fallos simultáneos; sin datos en claro | `backend/src/test` |
| `T-api` | `ApiValidationTest` (10): datos inválidos por HTTP → 400/404/405, nunca 500 | `backend/src/test` |
| `UPG` | Actualización V19 → V21 con datos reales de prueba (versión anterior `a6f5ada` → actual): datos idénticos, integridad y 12 comprobaciones funcionales | 24/11/2026; `docs/09` §4.2 |
| `SIM` | Simulacro de pérdida total con 3 años de datos: recuperación técnica en 7,5 s, datos idénticos | 24/11/2026; `docs/09` §7 |
| `OSV` | Escaneo de dependencias en CI (OSV-Scanner): la ejecución #6 encontró 32 avisos en el backend (Spring Boot 4.1.0); tras actualizar a 4.1.3, la #7 da **0**. Desde el 25/11, Spring Boot 4.2.0: **0** (159 paquetes del backend y 185 del frontend) | GitHub Actions |
| `T-flow` | `BusinessFlowHttpTest`: recorrido completo del negocio por HTTP (todos los controladores) | `backend/src/test` |
| `T-user` | `UserManagementTest` (6) y `LastAdminRuleTest` | `backend/src/test` |
| `T-cat` | `CatalogAdminTest` (2) e `InventoryQueryTest` (3) | `backend/src/test` |
| `COV` | JaCoCo: 93,0 % líneas / 74,4 % ramas global (135 pruebas, 25/11; Fase 6: 92,5 % / 73,9 %; Fase 5: 92,1 % / 72,7 %); el 24/11, 91,2 % reglas críticas; 92,0 % servicios. Umbral en el build: 85 % / 65 % | 24/11/2026; artefacto `cobertura-backend` en la CI |
| `T-log` | `UnexpectedErrorLoggingTest`: error interno registrado con código, sin detalles al usuario | `backend/src/test` |
| `T-per` | `DateRangeTest` (8), `HistoryAndMonitoringHttpTest` (4) y `MonitoringUnitTest` (3): historiales por periodo, estado del sistema, registro de operaciones y rechazos, peticiones lentas, alertas | `backend/src/test` |
| `LOAD` | Prueba de carga con 3 años de datos (33.000 ventas): 15 y 40 usuarios simultáneos, antes y después de las correcciones; integridad comprobada | 24/11/2026; `docs/12` §2; `scripts/perf` |
| `SQL` | Conteo de consultas por historial con `log_statement`: 3 por historial de ventas o movimientos (antes, varias por cada venta) | 24/11/2026; `docs/12` §1 |
| `E2E` | `frontend/e2e/offline-sync.e2e.cjs` (13 escenarios, 58 comprobaciones, navegador real), también por HTTPS a través del kit de producción | se ejecuta en CI (dos veces) |
| `KIT` | Kit `deploy/` instalado desde cero: 18/18 comprobaciones de `verify.sh`; solo 80/443 abiertos; PostgreSQL inaccesible; usuario de base sin privilegios; E2E por HTTPS; copia y prueba de restauración; importación de datos (41.259 ventas) | 24/11/2026; `docs/13` §8; trabajo *Kit de producción* en la CI |
| `DRILL` | Simulacros sobre el kit: PostgreSQL `kill -9` y corte de luz con ventas en curso (0 de 13.159 confirmadas perdidas), backend caído, actualización con copia previa (≈9 s sin servicio) y versión rota con vuelta atrás automática | 24/11/2026; `docs/13` §8 |
| `T-db` | `DatabaseDownTest` (3): base caída → 503, nunca 401 ni 500 | `backend/src/test` |
| `ACC` | Ensayo de aceptación `frontend/e2e/acceptance.e2e.cjs`: los 24 escenarios del checklist por la interfaz (vendedor en celular, administrador en computador), **24/24, 69 comprobaciones**; 3 corridas seguidas sin fallas; también por HTTPS en el kit | 24/11/2026; CI en cada cambio |
| `CI` | GitHub Actions: backend + frontend + E2E. Ejecución #1 en verde (24/11/2026) | pestaña **Actions** |
| `BKP` | Pruebas de los scripts de respaldo contra PostgreSQL: copia sana, archivo dañado, inventario alterado, falta de `.sha256`, retención en `CopyTo` | sesión del 23-24/11/2026; `scripts/backup` |
| `AUD` | `npm audit`: 0 vulnerabilidades (producción y desarrollo) tras `npm audit fix` | 24/11/2026 |
| `REV` | Revisión de código documentada en la auditoría | este documento |
| `MAN` | Prueba manual informada por la responsable (inicio de sesión, navegación, foto de perfil), sin registro por operación | auditoría 24/11/2026 |

---

## 0. Bloqueadores absolutos

Ninguno puede quedar abierto para dar 10/10.

| # | Bloqueador | Estado | Evidencia / acción |
| - | ---------- | ------ | ------------------ |
| 1 | Se puede vender inventario inexistente | ✅ No ocurre | `T-conc`, `E2E` (conflicto) |
| 2 | Se pueden duplicar ventas o compras confirmadas | ✅ No ocurre | Ventas (`T-conc`, `E2E`), compras (`T-com`, `E2E` esc. 5), producción (`T-pro`), personalizadas y pagos (`T-ord`). Cerrado en la Fase 1 (V21). |
| 3 | Un vendedor puede hacer operaciones restringidas | ✅ No ocurre | `T-perm` |
| 4 | Se pueden modificar precios históricos de ventas | ✅ No ocurre | `T-sale` (ventas inmodificables; la venta antigua conserva su precio tras cambiar el del producto) |
| 5 | Una venta puede quedar guardada parcialmente | ✅ No ocurre | `T-conc` (rollback); `DRILL`: con la base caída de golpe y por corte de luz, ninguna venta sin productos |
| 6 | La sincronización offline atribuye ventas al usuario equivocado | ✅ No ocurre | `E2E` escenario 4 |
| 7 | Se exponen contraseñas o secretos | ✅ En la aplicación · ⏳ (claves de la instalación) | Ramas y etiquetas revisadas el 25/11: sin claves. Se cierra al instalar con claves nuevas (`docs/13` §3). `.gitignore` revisado; `log-resolved-exception: false`; `T-log` (sin detalles al usuario); contraseñas con BCrypt |
| 8 | Se pierden datos al restaurar o actualizar | ✅ No ocurre | Restaurar nunca sobrescribe (`BKP`); actualización con datos sin pérdidas (`UPG`); recuperación total sin pérdidas (`SIM`) |
| 9 | Las migraciones destruyen datos existentes | ✅ No ocurre | `UPG` (V19 → V21: datos idénticos) |
| 10 | Los backups no pueden restaurarse | ✅ No ocurre | `BKP` y `SIM` con los scripts `.sh` (pérdida total recuperada en 7,5 s); en el kit de producción, prueba de restauración en cada CI (`KIT`). En el PC de Windows, copia, prueba de restauración y simulacro hechos el 24/11 (`docs/09` §7). |
| 11 | Vulnerabilidades críticas sin resolver | ✅ No hay | `OSV`: se encontraron críticas (Tomcat, Spring Web) y se corrigieron con Spring Boot 4.1.3; la CI lo revisa en cada push |
| 12 | Las pruebas modifican la base de trabajo | ✅ No ocurre | Perfil `test` con base propia; CI con bases temporales |

---

## 1. Arquitectura y calidad del código

| ID | Criterio | Pr. | Estado | Evidencia / nota |
| -- | -------- | --- | ------ | ---------------- |
| ARQ-01 | Frontend y backend separados | M | ✅ | `REV` |
| ARQ-02 | Reglas de negocio en el backend | A | ✅ | `T-prod`, `T-conc` |
| ARQ-03 | Controladores sin lógica compleja | M | ✅ | `REV`: delegan en servicios |
| ARQ-04 | Servicios concentran reglas por dominio | M | ✅ | `REV` |
| ARQ-05 | Repositorios solo acceden a datos | M | ✅ | `REV` |
| ARQ-06 | Entidades no se exponen directamente | M | ✅ | `REV`: los controladores devuelven DTO |
| ARQ-07 | DTO de entrada y salida claros | M | ✅ | `REV` |
| ARQ-08 | Sin dependencias circulares problemáticas | Bj | ⏳ | Sin análisis automático |
| ARQ-09 | Sin código sin utilidad | Bj | ✅ | Eliminados `UuidUtils`, `DateUtils`, `StringUtils`, `Constants`, `PageResponse` y `ValidationException` (sin uso) y 5 componentes vacíos del frontend |
| ARQ-10 | Sin lógica de negocio duplicada | M | ⏳ | Categoría → unidad se calcula en frontend y backend (el backend manda) |
| ARQ-11 | Excepciones centralizadas | M | ✅ | `GlobalExceptionHandler`, `T-log` |
| ARQ-12 | Nombres comprensibles | Bj | ⏳ | `/api/unit` en singular frente al resto en plural |
| ARQ-13 | Sin imports ni código muerto | Bj | ✅ | 0 imports sin uso en el código de producción (revisión con script, 24/11); ESLint en el frontend |
| ARQ-14 | Configuración dev/test/prod separada | A | ✅ | Perfiles; la `CI` usa `prod` y `test` |
| ARQ-15 | Los cambios no rompen lo existente | A | ✅ | `CI` en cada push |
| ARQ-16 | Sin advertencias de compilación | M | ✅ | Compilación con `showDeprecation`: 0 advertencias |
| ARQ-17 | Formato y estilo consistentes | Bj | ✅ | `.editorconfig`; corregida la sangría de `ProductCategoryController` y `DataInitializer` |
| ARQ-18 | Operaciones complejas documentadas | M | ✅ | Comentarios en inventario, ventas y offline; `docs/08` §30A |
| ARQ-19 | Dependencias justificadas y al día | M | ✅ | `OSV` en cada push + Dependabot semanal; Spring Boot 4.2.0 (25/11) |
| ARQ-20 | Decisiones arquitectónicas documentadas | M | ✅ | `docs/03`, `docs/02` RN-24A y RN-27 |

`BaseEntity` / `AuditableEntity`: no se considera defecto (decisión acordada).

## 2. Backend y API REST

| ID | Criterio | Pr. | Estado | Evidencia / nota |
| -- | -------- | --- | ------ | ---------------- |
| API-01 | Códigos HTTP apropiados | M | ✅ | 200/201/204/400/401/403/404/405/409/429 verificados (`T-api`, `T-auth`, `T-flow`, `T-perm`) |
| API-02 | Estructura de respuesta consistente | M | ✅ | `ErrorResponse` común; mensajes comprobados en `T-api`, `T-auth`, `T-flow` |
| API-03 | Mensajes de error claros | M | ✅ | Mensajes concretos comprobados en `T-api`, `T-sale`, `T-com`, `T-ord`, `T-auth` |
| API-04 | Validación en el servidor | A | ✅ | `T-perm` (400 con cuerpo vacío), `T-prod` |
| API-05 | Rechaza valores inválidos sin depender del frontend | A | ✅ | `T-prod`, `T-conc` (manta en metros) |
| API-06 | Identificadores inexistentes controlados | M | ✅ | `T-api` (404 inexistente, 400 mal escrito; antes 500) |
| API-07 | Recursos desactivados manejados | M | ✅ | `T-sale`, `T-com` (producto desactivado rechazado) |
| API-08 | Transacciones en operaciones múltiples | A | ✅ | `REV` + `T-conc` |
| API-09 | Rollback ante error | A | ✅ | `T-conc` |
| API-10 | No expone trazas ni datos sensibles | A | ✅ | `T-log` |
| API-11 | Filtros validan parámetros | Bj | ✅ | Filtros del historial validados (`T-mov`, `T-api`). `status` de personalizadas ignora valores desconocidos |
| API-12 | Consultas sin cargar registros innecesarios | M | ✅ | Historiales por periodo (30 días por defecto, máximo 93) y detalle en la misma consulta (`T-per`, `LOAD`) |
| API-13 | Integridad de relaciones | A | ✅ | Claves foráneas (`T-ctx`) |
| API-14 | Operaciones críticas idempotentes ante repetición | B | ✅ | Ventas, compras, producción, personalizadas y pagos (`T-conc`, `T-com`, `T-pro`, `T-ord`, `E2E` esc. 5) |
| API-15 | No se modifican campos no controlables | A | ⏳ | Implementado (mappers ignoran id, total, createdBy); falta prueba |
| API-16 | Documentación de la API coincide | M | ⏳ | `docs/05` revisado a mano contra los controladores; sin prueba automática |
| API-17 | Errores inesperados en log sin secretos | A | ✅ | `T-log` (corregido el 24/11: antes no se registraban) |
| API-18 | Endpoints con pruebas de integración | M | ✅ | `T-sale`, `T-com`, `T-pro`, `T-ord`, `T-mov`, `T-api`, `T-perm` |
| API-19 | Tiempos de espera y fallos de dependencias | M | ⏳ | Frontend: 20 s. Backend: valores por defecto del pool |
| API-20 | Correcta con solicitudes simultáneas | A | ✅ | `T-conc`, `T-sim` |

**Pruebas adicionales:** campos vacíos ✅ (`T-perm`, `T-api`), cantidades negativas ✅ (`T-api`), precios negativos ✅ (`T-api`), UUID inválido ✅ (`T-api`: antes respondía 500), id inexistente ✅ (`T-api`), tipos incorrectos ✅ (`T-api`), repetición ✅ (todas las operaciones), interrupción ✅ (`E2E` esc. 2 y 5), simultáneas ✅ (`T-conc`).

## 3. Base de datos y persistencia

| ID | Criterio | Pr. | Estado | Evidencia / nota |
| -- | -------- | --- | ------ | ---------------- |
| DB-01 | Esquema representa el negocio | A | ✅ | `REV`, `docs/07` |
| DB-02 | Claves primarias | A | ✅ | `T-ctx` (`validate`) |
| DB-03 | Claves foráneas | A | ✅ | Esquema revisado |
| DB-04 | Restricciones UNIQUE | A | ✅ | Código interno, código de barras, usuario, teléfono, `client_request_id` |
| DB-05 | NOT NULL en obligatorias | A | ✅ | `T-ctx` |
| DB-06 | Índices en consultas importantes | M | ⏳ | Existen en movimientos; sin análisis de planes |
| DB-07 | Migraciones versionadas | A | ✅ | V1–V22 |
| DB-08 | Migran desde base vacía | A | ✅ | `T-ctx`, `CI`, `E2E` |
| DB-09 | Base existente se actualiza desde la versión anterior | B | ✅ | `UPG` |
| DB-10 | Esquema = entidades | A | ✅ | `ddl-auto: validate` en `T-ctx` |
| DB-11 | Dinero en decimales | A | ✅ | `NUMERIC(12,2)` / `BigDecimal` |
| DB-12 | Sin punto flotante para dinero | A | ✅ | Búsqueda en código: ningún `double`/`float` |
| DB-13 | Fechas y zonas coherentes | M | ⏳ | Las ventas offline convierten la zona (`T-conc`); el servidor usa su zona local: fijar `TZ` en el despliegue |
| DB-14 | Sin registros huérfanos | A | ✅ | Claves foráneas + `integrity-check.sql` |
| DB-15 | Consistencia transaccional | A | ✅ | `T-conc` |
| DB-16 | Concurrencia en registros críticos | A | ✅ | Bloqueo pesimista de inventario, `T-conc` |
| DB-17 | Rendimiento aceptable | M | ✅ | 3 años de datos (47 MB): consultas de venta e inventario en milisegundos; historiales acotados (`LOAD`, `SQL`) |
| DB-18 | Sin N+1 importantes | M | ✅ | Corregido: el historial de ventas carga productos y unidades en la misma consulta; listas en bloques de 100. 3 consultas por historial (`SQL`) |
| DB-19 | Historial conservado | A | ✅ | `T-sale`, `T-com`, `T-pro`, `T-ord` (no se editan ni borran) |
| DB-20 | Eliminaciones respetan historial | A | ✅ | `T-sale` (producto desactivado conserva su historial) |

**Prueba de instalación desde cero (7 pasos):** ✅ base vacía → backend → migraciones → datos básicos → administrador inicial → inicio de sesión → módulos principales, sin SQL manual (`E2E` y `CI` lo hacen en cada ejecución).

## 4. Productos, categorías y unidades

| ID | Criterio | Pr. | Estado | Evidencia / nota |
| -- | -------- | --- | ------ | ---------------- |
| PROD-01 | Crear productos | A | ✅ | `E2E`, `T-conc` |
| PROD-02 | Editar productos | A | ✅ | `T-sale` (cambio de precio) |
| PROD-03 | Desactivar productos | A | ✅ | `T-sale` |
| PROD-04 | Consultar activos | A | ✅ | `E2E` |
| PROD-05 | Categorías válidas | A | ✅ | `T-prod` |
| PROD-06 | Mantas en `#` | A | ✅ | `T-prod` |
| PROD-07 | Telas en metros | A | ✅ | `T-prod` |
| PROD-08 | Sin unidades incompatibles | A | ✅ | `T-prod`, `T-conc` |
| PROD-09 | Sin fracciones de manta | A | ✅ | `T-prod` |
| PROD-10 | Decimales en tela | A | ✅ | `T-prod` |
| PROD-11 | Precios no negativos | A | ✅ | `T-api` (precio 0 y negativo → 400) |
| PROD-12 | Precio de compra = precio unitario | A | ✅ | `T-prod` |
| PROD-13 | Sin opciones comprable/fabricable en la interfaz | M | ✅ | `REV` de `ProductForm` |
| PROD-14 | Con historial no se rompe la consistencia | A | ✅ | `T-conc` (cambio de unidad bloqueado) |
| PROD-15 | Desactivados no se usan en operaciones nuevas | A | ✅ | `T-sale` (venta), `T-com` (compra) |
| PROD-16 | Códigos internos no se duplican | A | ✅ | Restricción UNIQUE + secuencia |
| PROD-17 | Búsquedas y filtros | M | ⏳ | Falta prueba de búsqueda y filtros en Productos |
| PROD-18 | Cambios reflejados en inventario | A | ✅ | `E2E` (stock inicial) |

**Casos extremos (comportamiento definido):**

| Caso | Comportamiento actual | Estado |
| ---- | --------------------- | ------ |
| Dos productos con el mismo nombre | **No se permite** (decisión 24/11): sin distinguir mayúsculas ni espacios, incluidos los desactivados | ✅ `T-name` |
| Precio cero | Se rechaza (`unitPrice` debe ser > 0) | ✅ `T-api` |
| Cambiar la unidad de una manta con existencias | Se rechaza | ✅ `T-conc` |
| Cambiar la categoría de un producto con historial | Permitido solo si la unidad no cambia | ⏳ Falta prueba |
| Desactivar un producto usado en ventas | Se permite; el historial se conserva | ✅ `T-sale` |
| Vender un producto desactivado | Se rechaza | ✅ `T-sale` |

## 5. Inventario y movimientos

| ID | Criterio | Pr. | Estado | Evidencia / nota |
| -- | -------- | --- | ------ | ---------------- |
| INV-01 | Stock inicial | A | ✅ | `E2E` |
| INV-02 | Entradas incrementan | A | ✅ | `E2E` (ajuste hacia arriba) |
| INV-03 | Salidas disminuyen | A | ✅ | `T-conc`, `E2E` |
| INV-04 | Ajustes correctos | A | ✅ | `E2E` (7 → 4) |
| INV-05 | Tipo válido | A | ✅ | Clave foránea + `integrity-check.sql` |
| INV-06 | Origen identificable | A | ✅ | `source_type` |
| INV-07 | Relación con la operación de origen | A | ✅ | `T-com`, `T-pro`, `T-ord` (`source_id` de cada operación) |
| INV-08 | Sin negativos | B | ✅ | `T-conc`, `E2E` |
| INV-09 | Movimientos simultáneos | A | ✅ | `T-conc` |
| INV-10 | No vender más de lo disponible | B | ✅ | `T-conc` |
| INV-11 | Historial no alterable | A | ✅ | `REV`: sin endpoints de edición o borrado |
| INV-12 | Inventario = suma de movimientos | A | ✅ | `integrity-check.sql` en `BKP` |
| INV-13 | Cantidades según la unidad | A | ✅ | `T-prod` |
| INV-14 | Ajuste con causa identificable | A | ✅ | Motivo obligatorio (`T-api`, `E2E` esc. 6); queda con el usuario (`T-mov`) |
| INV-15 | Usuario responsable | A | ✅ | `E2E` (`sellerUsername`); `created_by` |
| INV-16 | Movimientos por producto | M | ✅ | `?productId=` |
| INV-17 | Movimientos por fecha | M | ✅ | `T-mov`, `E2E` esc. 6 |
| INV-18 | Movimientos por tipo | M | ✅ | `T-mov` |
| INV-19 | Movimientos por usuario | M | ✅ | `T-mov`, `E2E` esc. 6 |
| INV-20 | Consistente tras error y rollback | A | ✅ | `T-conc` |

**Pruebas exigidas:** última unidad con dos ventas simultáneas ✅ (`T-conc`) · ajuste físico 15 → 12 con movimiento −3 y usuario responsable ✅ (`T-mov`, exactamente el caso del checklist) · fallo a mitad de venta sin cambios parciales ✅ (`T-conc`).

## 6. Ventas

| ID | Criterio | Pr. | Estado | Evidencia / nota |
| -- | -------- | --- | ------ | ---------------- |
| VEN-01 | Registrar ventas | B | ✅ | `T-conc`, `E2E` |
| VEN-02 | Varios productos | A | ✅ | `T-sale` |
| VEN-03 | Cantidad validada por producto | A | ✅ | `T-conc` |
| VEN-04 | Precios correctos | A | ✅ | `T-sale` |
| VEN-05 | Subtotales correctos | A | ✅ | `T-sale` |
| VEN-06 | Totales correctos | A | ✅ | `T-sale` |
| VEN-07 | Actualiza inventario | B | ✅ | `T-conc`, `E2E` |
| VEN-08 | Genera movimientos | A | ✅ | `integrity-check.sql` |
| VEN-09 | Registra el vendedor | A | ✅ | `E2E` |
| VEN-10 | Conserva historial | A | ✅ | `REV`: no se edita ni borra |
| VEN-11 | Sin productos desactivados | A | ✅ | `T-sale` |
| VEN-12 | Sin cantidades superiores al stock | B | ✅ | `T-conc` |
| VEN-13 | Ventas simultáneas | B | ✅ | `T-conc` |
| VEN-14 | Sin doble registro offline | B | ✅ | `T-conc`, `E2E` |
| VEN-15 | Sin ventas parciales | B | ✅ | `T-conc` |
| VEN-16 | Consulta según permisos | A | ✅ | `T-perm` |
| VEN-17 | Detalle con precios aplicados | A | ✅ | `T-sale` |
| VEN-18 | Precisión decimal | A | ✅ | `BigDecimal` / `NUMERIC` |

**Precios especiales y descuentos**

| ID | Criterio | Pr. | Estado | Evidencia / nota |
| -- | -------- | --- | ------ | ---------------- |
| DES-01 | Precio por producto del carrito | A | ✅ | `T-sale` (precio especial en un producto, normal en otro) |
| DES-02 | Aplicable a una sola unidad | A | ✅ | `T-sale` (una unidad) |
| DES-03 | No depende de la cantidad | A | ✅ | `T-sale` |
| DES-04 | No depende del precio mayorista | A | ✅ | `T-sale` (sin precio mayorista) |
| DES-05 | Precio normal o especial | A | ✅ | `T-sale` |
| DES-06 | Descuento correcto | A | ✅ | `T-sale` ($10.000) |
| DES-07 | Porcentaje correcto | A | ✅ | `T-sale` (12,5 % en ambos sentidos) |
| DES-08 | Solo afecta esa venta | A | ✅ | `T-sale` |
| DES-09 | No cambia el precio base | A | ✅ | `T-sale` |
| DES-10 | Se configura en `NewSalePage` | M | ✅ | `REV` |
| DES-11 | Popup por producto del carrito | M | ✅ | `REV` |
| DES-12 | Guardado en el detalle | A | ✅ | `T-sale` |
| DES-13 | Venta antigua muestra el precio aplicado | A | ✅ | `T-sale` (venta antigua tras subir el precio) |
| DES-14 | Rechaza valores inválidos | A | ✅ | `T-sale` (0, ≥ normal, ≥ 100 %, negativo, ambos a la vez) |
| DES-15 | Comportamiento definido > 100 % y > precio normal | A | ✅ | Definido: descuento ≥ 100 % o precio especial ≥ precio normal → rechazado con mensaje (`SaleServiceImpl`) |

**Caso monetario de referencia ($80.000 → $70.000; descuento $10.000; 12,5 %):** ✅ en el backend (`T-sale`: registro, detalle, total, inventario, venta consultada después). ⏳ Falta comprobarlo en la pantalla (carrito y confirmación) en la aceptación del negocio.

## 7. Compras

| ID | Criterio | Pr. | Estado | Evidencia / nota |
| -- | -------- | --- | ------ | ---------------- |
| COM-01 | Registrar compras | A | ✅ | `T-com` |
| COM-02 | Varios productos | A | ✅ | `T-com` |
| COM-03 | Valida cantidades | A | ✅ | `T-com`, `T-api` |
| COM-04 | Valida precios | A | ⏳ | Implementado; falta prueba |
| COM-05 | Total correcto | A | ✅ | `T-com` |
| COM-06 | Incrementa inventario | A | ✅ | `T-com` |
| COM-07 | Movimientos de entrada | A | ✅ | `T-com` |
| COM-08 | Usuario responsable | A | ✅ | `T-com` |
| COM-09 | Historial | A | ✅ | `T-com` |
| COM-10 | Sin productos incompatibles | A | ✅ | `T-com` |
| COM-11 | Sin cambios parciales al fallar | A | ✅ | `T-com` (tela válida + manta inválida → nada) |
| COM-12 | Sin duplicados por reintento | B | ✅ | `T-com`, `E2E` esc. 5 |
| COM-13 | Vendedor sin acceso | A | ✅ | `T-perm` |
| COM-14 | Historial consistente | A | ✅ | `T-com` |
| COM-15 | Detalle completo | M | ✅ | `T-com` |

**Modificar una compra con movimientos:** rechazado siempre (`PUT`/`DELETE`). ✅ `T-com`.

## 8. Producción y pedidos personalizados

| ID | Criterio | Pr. | Estado | Evidencia / nota |
| -- | -------- | --- | ------ | ---------------- |
| PRO-01 | Crear producción | A | ✅ | `T-pro` |
| PRO-02 | Consultar producción | A | ✅ | `T-pro` |
| PRO-03 | Registra materiales | A | ✅ | `T-pro` |
| PRO-04 | Descuenta materiales | A | ✅ | `T-pro` |
| PRO-05 | Genera entradas | A | ✅ | `T-pro` |
| PRO-06 | Sin materiales inexistentes | A | ✅ | `T-pro` |
| PRO-07 | Responsables | A | ✅ | `T-pro` |
| PRO-08 | Historial | A | ✅ | `T-pro` |
| PRO-09 | Crear personalizadas | A | ✅ | `T-ord` |
| PRO-10 | Transiciones de estado válidas | M | ✅ | Decisión 24/11: solo pendiente → pagada (calculado por pagos); sin "cancelada". `T-ord` |
| PRO-11 | Pagos y abonos | A | ✅ | `T-ord` |
| PRO-12 | Saldos correctos | A | ✅ | `T-ord` |
| PRO-13 | Sin pagos superiores al saldo | A | ✅ | `T-ord` |
| PRO-14 | Movimientos del pedido conservados | A | ✅ | `T-ord` (salida con origen `ORDER`) |
| PRO-15 | Permisos ADMIN/VENDEDOR | A | ⏳ | Decisión: ambos roles. Falta prueba de permisos |
| PRO-16 | Sin duplicados por reintento | B | ✅ | `T-pro`, `T-ord` |
| PRO-17 | Transaccional | A | ✅ | `T-pro`, `T-ord` |
| PRO-18 | Historial de completados/cancelados | M | ✅ | Las pagadas conservan pagos e historial; no se eliminan (`T-ord`). No hay canceladas (decisión) |

## 9. Usuarios, roles y seguridad

| ID | Criterio | Pr. | Estado | Evidencia / nota |
| -- | -------- | --- | ------ | ---------------- |
| SEG-01 | Inicio de sesión | B | ✅ | `E2E`, `MAN` |
| SEG-02 | Hash seguro | B | ✅ | `T-auth` (la contraseña guardada empieza por `$2`, BCrypt) |
| SEG-03 | Contraseñas nunca en respuestas | B | ✅ | `T-auth` (`/auth/login`, `/api/me` y `/users` sin contraseñas ni hashes) |
| SEG-04 | JWT validado | B | ✅ | `T-auth` (token alterado o firmado con otra clave → 401) |
| SEG-05 | Tokens vencidos rechazados | A | ✅ | `T-auth` (token vencido → 401) |
| SEG-06 | Rutas protegidas | B | ✅ | `T-perm` |
| SEG-07 | Roles en el backend | B | ✅ | `T-perm` |
| SEG-08 | Vendedor sin funciones de administrador | B | ✅ | `T-perm` |
| SEG-09 | No cambiar el rol propio | B | ✅ | `/users/**` solo ADMIN (`T-perm`); `/api/me` no acepta rol (`REV`) |
| SEG-10 | Sin acceso a recursos ajenos por id | A | ✅ | `/api/me` usa la sesión (`T-flow`); `/users/{id}` solo ADMIN (`T-auth`) |
| SEG-11 | Inactivos no entran | A | ✅ | `T-auth` (no inicia sesión y su token deja de servir al momento) |
| SEG-12 | Identificadores únicos | A | ✅ | UNIQUE |
| SEG-13 | Teléfonos únicos | A | ✅ | `T-auth` (registro con teléfono repetido → 409) |
| SEG-14 | Usuarios únicos | A | ✅ | `T-auth` (registro con usuario repetido → 409) |
| SEG-15 | Sesiones correctas | A | ✅ | `E2E` |
| SEG-16 | Cierre de sesión | A | ✅ | `E2E` |
| SEG-17 | Sin datos sensibles en logs | A | ✅ | `log-resolved-exception: false`; `T-log` |
| SEG-18 | Sin secretos en git | B | ⏳ | Ramas y etiquetas revisadas el 25/11: sin claves. Secret scanning y push protection activos. Se cierra al instalar con claves nuevas (`docs/13` §3) |
| SEG-19 | CORS solo autorizados | A | ⏳ | Configurable (`CORS_ALLOWED_ORIGINS`); valor de producción pendiente |
| SEG-20 | Endpoints sensibles protegidos | B | ✅ | `T-perm` |
| SEG-21 | Protección contra fuerza bruta | A | ✅ | `T-auth`, `T-lock`. Límites por ventana de 15 minutos: 5 fallos de una cuenta desde la misma conexión, 30 de una conexión con cualquier cuenta y 50 de una cuenta desde muchas conexiones → 429. Guardado en la base: sobrevive al reinicio (25/11). El intento se cuenta antes de revisar la contraseña: 30 intentos simultáneos solo revisan los del límite (antes, todos). Registro público: 10 cuentas por conexión y 30 en total por hora |
| SEG-22 | Sin inyección | A | ✅ | JPA con parámetros, Bean Validation (`REV`) |
| SEG-23 | Sin XSS | A | ✅ | Sin HTML sin escapar (`REV`); CSP sin violaciones (`E2E`) |
| SEG-24 | Dependencias revisadas | A | ✅ | `OSV` (backend y frontend: 0) + `AUD` |
| SEG-25 | Errores de autenticación sin información extra | A | ✅ | `T-auth` (mismo mensaje para usuario inexistente y contraseña incorrecta) |

**Pruebas HTTP de permisos:** vendedor crea producto ✅ · ve historial monetario ✅ · registra compra ✅ · crea administrador (`POST /users`) ✅ `T-auth` · cambia su rol (`PUT /users/{id}`, `/promote`) ✅ `T-auth` · sin sesión registra venta ✅ `T-auth` · token vencido ✅ `T-auth`.

**Archivos:** 20 MB ⏳ (implementado en frontend y backend; falta prueba del 413) · tipo ✅ `T-img` · firma real ✅ `T-img` · malformados ✅ `T-img` · suplantación ✅ `T-img` · memoria ⏳ · sin rutas internas ✅ (`REV`: la foto está en la base, no en disco).

## 10. Funcionamiento offline y sincronización

| ID | Criterio | Pr. | Estado | Evidencia |
| -- | -------- | --- | ------ | --------- |
| OFF-01 | Catálogo local | A | ✅ | `E2E` |
| OFF-02 | Consulta sin conexión | A | ✅ | `E2E` |
| OFF-03 | Ventas pendientes offline | A | ✅ | `E2E` |
| OFF-04 | Sobreviven al cierre | A | ✅ | `E2E` (app reabierta) |
| OFF-05 | Identificador único | B | ✅ | `E2E`, `T-conc` |
| OFF-06 | Sincroniza al volver | A | ✅ | `E2E` |
| OFF-07 | Sin duplicados al repetir | B | ✅ | `E2E` esc. 2 y 7 (dos pestañas), `T-conc`, `T-sim` (4 envíos simultáneos) |
| OFF-08 | Venta confirmada no se reprocesa | B | ✅ | `E2E`, `T-conc` |
| OFF-09 | Informa pendientes | A | ✅ | `E2E` |
| OFF-10 | Informa fallos | A | ✅ | `E2E` esc. 3 |
| OFF-11 | Errores recuperables | A | ✅ | `E2E` ("Reintentar") |
| OFF-12 | Interrupción en la transmisión | A | ✅ | `E2E` esc. 1, 2 y 9 (sin espacio: no se da por guardada, carrito conservado) |
| OFF-13 | Interrupción en la respuesta | B | ✅ | `E2E` esc. 2 |
| OFF-14 | Sin pérdidas al reiniciar el navegador | A | ✅ | `E2E` |
| OFF-15 | Cambio de usuario con pendientes | B | ✅ | `E2E` esc. 4 y 8 (sesión vencida sin conexión) |
| OFF-16 | Identidad correcta | B | ✅ | `E2E` esc. 4 |
| OFF-17 | Stock validado al sincronizar | B | ✅ | `E2E` esc. 3, `T-conc` |
| OFF-18 | Conflictos local/remoto | A | ✅ | `E2E` esc. 3 |
| OFF-19 | No se presenta como confirmada | A | ✅ | "Guardada sin conexión", "Total estimado" (`E2E`) |
| OFF-20 | Resultado verificable por operación | M | ✅ | `client_request_id` en el servidor + estado en el dispositivo |

**Escenarios A, B, C y D:** ✅ (`E2E` escenarios 1, 2, 4 y 3; se ejecutan en cada push en `CI`).

**Casos límite adicionales (Fase 2, 24/11/2026):** ✅ dos pestañas sincronizando a la vez (`E2E` esc. 7) · ✅ la misma venta o compra enviada 4 veces simultáneamente (`T-sim`) · ✅ sesión vencida mientras no hay conexión (`E2E` esc. 8) · ✅ dispositivo sin espacio para guardar (`E2E` esc. 9).

## 11. Frontend y experiencia de usuario

| ID | Criterio | Pr. | Estado | Evidencia / nota |
| -- | -------- | --- | ------ | ---------------- |
| UX-01 | Todas las páginas cargan | A | ✅ | `E2E` esc. 10: las 9 páginas cargan como administrador y como vendedor, sin errores de JavaScript |
| UX-02 | Navegación coherente | M | ⏳ | `MAN` |
| UX-03 | Formularios validan obligatorios | A | ✅ | Obligatorios y valores inválidos en productos, ajustes, producción, compras y usuarios, ensayados por la interfaz (`ACC` 3, 5, 7, 14, 18) |
| UX-04 | Errores con causa | A | ✅ | Mensaje real del backend (`apiClient`); `E2E` (motivo del rechazo) |
| UX-05 | Estados de carga | M | ⏳ | Implementados; falta revisión |
| UX-06 | Sin envíos dobles | A | ✅ | `REV` + identificador estable en formularios (`E2E` esc. 5) |
| UX-07 | Confirmación en acciones destructivas | A | ⏳ | Descartar venta y cerrar sesión con pendientes ✅; falta revisar desactivar usuario o producto |
| UX-08 | Tablas legibles | M | ⏳ | `MAN` |
| UX-09 | Filtros | M | ✅ | Filtros por origen, usuario y fechas (`ACC` 8, `T-mov`, `E2E` 6); periodos de ventas (`T-per`) |
| UX-10 | Sin pérdida de datos de formularios | M | ✅ | Carrito y formularios se conservan ante rechazos y sin espacio (`E2E` 9, `ACC` 12) y avisan antes de salir (`E2E` 13) |
| UX-11 | Pendiente frente a confirmada | A | ✅ | `E2E` |
| UX-12 | Fotos | M | ✅ | `MAN` |
| UX-13 | Éxito solo si se completó | A | ✅ | `E2E` (registrada frente a guardada sin conexión) |
| UX-14 | Permisos reflejados | A | ✅ | `E2E` esc. 10: menú, crear productos y ajustar según el rol |
| UX-15 | Errores de red | A | ✅ | `E2E` |
| UX-16 | Estados vacíos | Bj | ⏳ | Implementados en varias pantallas; falta revisión |
| UX-17 | Tamaños de pantalla | M | ✅ | Flujos del vendedor ensayados en tamaño de celular (Pixel 7) y del administrador en computador (`ACC`). Falta confirmar en un celular real durante la aceptación (`docs/15` §3) |
| UX-18 | Teclado | M | ⏳ | Falta auditoría |
| UX-19 | Etiquetas | M | ⏳ | Falta auditoría |
| UX-20 | Consistencia visual | Bj | ⏳ | — |
| UX-21 | Ortografía | Bj | ⏳ | — |
| UX-22 | Diálogos | M | ✅ | Ventanas con X y Escape; las de formularios preguntan antes de cerrarse con datos (`E2E` 13, `ACC`) |
| UX-23 | Evitar perder información al salir | M | ✅ | Aviso antes de salir con carrito o formulario a medio llenar (menú, cerrar sesión, X/Escape y recargar); `E2E` escenario 13 |
| UX-24 | Errores de sesión | A | ✅ | 401 → inicio con aviso de sesión vencida (`REV`, `E2E`) |
| UX-25 | Retroalimentación tras cada operación | M | ✅ | Cada operación confirma con un mensaje (venta, compra, producción, ajuste, usuario, personalizada), comprobado en `ACC` |

**WCAG 2.2 AA:** ⏳ pendiente (contraste, foco visible, teclado, lectores de pantalla).

## 12. Fotografías y archivos

| ID | Criterio | Pr. | Estado | Evidencia |
| -- | -------- | --- | ------ | --------- |
| IMG-01 | Subir | M | ✅ | `MAN` |
| IMG-02 | 20 MB en frontend y backend | M | ✅ | Backend `T-user` (21 MB rechazado); frontend valida antes de subir (`ProfilePage`) |
| IMG-03 | Formato real | A | ✅ | `T-img` |
| IMG-04 | Rechaza no imágenes | A | ✅ | `T-img` |
| IMG-05 | Archivos excesivos | M | ✅ | `T-user` |
| IMG-06 | Recuperar | M | ✅ | `MAN` |
| IMG-07 | Actualizar | M | ⏳ | — |
| IMG-08 | Sin rutas internas | A | ✅ | Se guardan en la base (`REV`) |
| IMG-09 | Corruptos | M | ✅ | `T-img` (archivo truncado) |
| IMG-10 | No bloquea la app | Bj | ⏳ | — |
| IMG-11 | Fallos de transmisión | Bj | ⏳ | Tiempo límite de 120 s en el frontend |
| IMG-12 | Eliminación | Bj | ✅ | `T-user`, `T-flow` (quitar foto → 404 al pedirla) |

## 13. Rendimiento

| ID | Criterio | Pr. | Estado | Nota |
| -- | -------- | --- | ------ | ---- |
| REN-01 | Tiempos razonables | M | ✅ | p95 ≤ 25 ms en operaciones de venta e inventario, ≤ 270 ms en historiales (`LOAD`) |
| REN-02 | Sin datos innecesarios | M | ✅ | Historiales por periodo: 26 MB → 1,1 MB (ventas), 37 MB → 1,4 MB (movimientos) (`LOAD`) |
| REN-03 | Paginación | M | ✅ | Ventas y movimientos por periodo (máx. 93 días) y en pantalla de a 100 ("Ver más"). Compras, producción y personalizadas completas: crecen ≈1/día; 1.000 personalizadas = 20 ms (`docs/12` §2.4) |
| REN-04 | Sin consultas costosas | M | ✅ | Sin consultas lentas en la carga; las de más de 1 s quedan en el log como "Lenta" (`LOAD`, `T-per`) |
| REN-05 | Sin N+1 | M | ✅ | Ver DB-18 (`SQL`) |
| REN-06 | Concurrencia consistente | A | ✅ | `T-conc` |
| REN-07 | Sin solicitudes innecesarias | Bj | ✅ | La recarga automática (60 s) solo corre con la pantalla visible y es liviana: inventario 14 KB en 3 ms; historiales acotados al periodo (`LOAD`) |
| REN-08 | Imágenes | Bj | ⏳ | Sin redimensionar al subir |
| REN-09 | Pool de conexiones | M | ✅ | Hikari: 10 conexiones, espera máx. 10 s, aviso de fuga a 60 s; atendió 40 usuarios (`LOAD`) |
| REN-10 | Memoria | M | ✅ | 273 MB de 512 MB de heap con 40 usuarios; `-XX:+ExitOnOutOfMemoryError` documentado (`LOAD`, `docs/12` §2.5 y §4) |
| REN-11 | Usuarios simultáneos esperados | M | ⏳ | Supuesto: hasta 10 personas conectadas; probado con 40 (`LOAD`). Falta que la responsable confirme la cifra (`docs/11`) |
| REN-12 | Tiempos bajo carga | M | ✅ | 15 y 40 usuarios simultáneos, 3 años de datos: 0 errores de servidor (antes: 123 errores 500 y falta de memoria) (`LOAD`) |
| REN-13 | Picos | Bj | ✅ | 40 usuarios a la vez (4 veces lo esperado) sin errores de servidor (`LOAD`) |
| REN-14 | Operaciones críticas | M | ✅ | Registrar venta p95 25 ms y ajuste p95 37 ms con 40 usuarios; inventario íntegro después (`LOAD`) |
| REN-15 | Límites conocidos | M | ✅ | `docs/12` §2.6 |

## 14. Copias de seguridad y recuperación

| ID | Criterio | Pr. | Estado | Evidencia / nota |
| -- | -------- | --- | ------ | ---------------- |
| BKP-01 | Procedimiento automatizado | B | ✅ | `BKP` |
| BKP-02 | Periódico | A | ✅ | Tarea de Windows cada 2 h (6:00–21:00), comprobada en el PC el 24 y 25/11: copias correctas en el registro (requiere el equipo encendido) |
| BKP-03 | Comprimido | M | ✅ | `--compress=9` |
| BKP-04 | Integridad por archivo | A | ✅ | `pg_restore --list` + SHA-256 (`BKP`) |
| BKP-05 | SHA-256 | A | ✅ | `BKP` |
| BKP-06 | Sin checksum válido → rechazo | A | ✅ | `BKP` (24/11) |
| BKP-07 | Fuera del equipo | A | ⏳ | `CopyTo` (configuración del usuario) |
| BKP-08 | Segunda ubicación | A | ⏳ | Ídem |
| BKP-09 | Retención | M | ✅ | `BKP` |
| BKP-10 | Retención en principal y secundaria | M | ✅ | `BKP` (24/11) |
| BKP-11 | Protección contra acceso no autorizado | A | ⏳ | Copias con permisos `600` (solo su dueño) en el servidor y en Linux; en Windows, dentro de la carpeta personal del usuario. Copia en la nube **cifrada** con `rclone crypt` (`docs/13` §6), al configurarla (`docs/11` #21) |
| BKP-12 | Restaurar base completa | B | ✅ | `BKP` (restaurada y verificada) |
| BKP-13 | En entorno aislado | A | ✅ | Base temporal |
| BKP-14 | Integridad tras restaurar | A | ✅ | `integrity-check.sql` |
| BKP-15 | Registros y relaciones completos | A | ✅ | Conteos comparados (`BKP`) |
| BKP-16 | Procedimiento documentado | A | ✅ | `docs/09` |
| BKP-17 | Tiempo de recuperación medido | A | ✅ | `SIM`: 7,5 s técnicos (restaurar 0,6 s + arrancar 6,9 s) |
| BKP-18 | Pérdida total de la base principal | A | ✅ | `SIM`: base eliminada → restaurada → datos idénticos → operación normal |
| BKP-19 | Sin errores silenciosos | A | ✅ | La prueba falla con código ≠ 0 y deja aviso en el Escritorio |
| BKP-20 | Historial de copias | M | ✅ | `backup.log` |

**RPO / RTO propuestos (a aprobar por el negocio):** RPO **2 horas** en horario de operación. RTO **1 hora**; tiempo técnico demostrado **7,5 s** (`SIM`). Detalle en `docs/09` §6.

## 15. Logs, monitoreo y observabilidad

| ID | Criterio | Pr. | Estado | Nota |
| -- | -------- | --- | ------ | ---- |
| OBS-01 | Errores registrados | A | ✅ | `T-log` (corregido 24/11) |
| OBS-02 | Operaciones críticas registradas | M | ✅ | Una línea por operación que cambia datos, con usuario, resultado y duración, sin contraseñas (`T-per`) |
| OBS-03 | Errores por fecha y contexto | A | ✅ | Método, ruta y código (`T-log`) |
| OBS-04 | Fallos de sincronización | M | ✅ | Las ventas sincronizadas llegan marcadas `[sin conexión]` y los rechazos quedan en el log del servidor con el motivo (`T-per`, `E2E`) |
| OBS-05 | Fallos de base de datos | A | ✅ | Conflictos (409) y rechazos registrados con su motivo; errores internos con código (`T-per`, `T-log`) |
| OBS-06 | Estado del backend | M | ✅ | `GET /actuator/health`, sin detalles internos; la CI espera a que diga UP (`T-per`, `CI`) |
| OBS-07 | Estado de PostgreSQL | M | ✅ | Componente `db` del estado; revisión cada 5 min con alerta en el log (`T-per`) |
| OBS-08 | Espacio en disco | M | ✅ | Componente `diskSpace` (DOWN con menos de 2 GB libres, configurable) (`T-per`) |
| OBS-09 | Crecimiento de logs | M | ✅ | ≈160 bytes por operación: menos de 2 MB al mes con el uso esperado (`LOAD`); tope de 300 MB |
| OBS-10 | Alertas críticas | M | ⏳ | El backend deja ERROR en el log si la base o el disco fallan (`T-per`) y las copias avisan. Falta el aviso que llegue a alguien sin abrir el log: monitor externo sobre `/actuator/health` (`docs/11` #22) |
| OBS-11 | Rotación de logs | M | ✅ | Archivo con rotación: 10 MB por archivo, 30 días, 300 MB en total (`docs/12` §3.2) |
| OBS-12 | Usuario por operación | A | ✅ | `created_by` |
| OBS-13 | Solicitud ↔ error | M | ✅ | Código de referencia (`T-log`) |
| OBS-14 | Problemas de rendimiento | Bj | ✅ | Peticiones de más de 1 s registradas como "Lenta" (`T-per`) |
| OBS-15 | Respuesta a incidentes | M | ✅ | Guía de síntomas, qué revisar y qué hacer (`docs/12` §6) |

## 16. Pruebas automatizadas y calidad continua

| ID | Criterio | Pr. | Estado | Evidencia / nota |
| -- | -------- | --- | ------ | ---------------- |
| QA-01 | Unitarias pasan | A | ✅ | `CI` |
| QA-02 | Integración pasan | A | ✅ | `CI` |
| QA-03 | Bases aisladas | B | ✅ | Perfil `test`, servicios de `CI` |
| QA-04 | No tocan datos de trabajo | B | ✅ | Ídem |
| QA-05 | Reglas críticas cubiertas | A | ✅ | Ventas, productos, inventario, compras, producción y personalizadas |
| QA-06 | Permisos | A | ✅ | `T-perm` |
| QA-07 | Concurrencia | A | ✅ | `T-conc` |
| QA-08 | Validaciones | A | ✅ | `T-api`, `T-prod` |
| QA-09 | Transaccionales | A | ✅ | `T-conc`, `T-com`, `T-pro` |
| QA-10 | Offline | A | ✅ | `E2E` |
| QA-11 | Extremo a extremo | A | ✅ | `E2E` |
| QA-12 | Casos de error | A | ✅ | Rechazos en `T-conc`, `T-perm` y `E2E` |
| QA-13 | Límites de cantidades y precios | A | ✅ | `T-sale`, `T-api`, `T-ord` |
| QA-14 | Interrupciones de red | A | ✅ | `E2E` |
| QA-15 | Operaciones repetidas | A | ✅ | Ventas |
| QA-16 | Transiciones de estado | M | ✅ | `T-ord` (pendiente → pagada; pagada no acepta más pagos) |
| QA-17 | Reproducibles | A | ✅ | `E2E` 3 ejecuciones seguidas + `CI` |
| QA-18 | Independientes del orden | M | ✅ | Orden aleatorio permanente (`runOrder=random`); 104/104 con dos semillas distintas (24/11) |
| QA-19 | Pipeline: lint y build | A | ✅ | `CI` |
| QA-20 | Pipeline: backend | A | ✅ | `CI` |
| QA-21 | Un fallo bloquea el pipeline | A | ✅ | `CI` |
| QA-22 | Revisión antes de integrar | M | ✅ | Regla "Proteger main" activa: pull request, rama actualizada y las 5 comprobaciones de la CI obligatorias; sin excepciones (25/11, `docs/06` §14.6) |
| QA-23 | Sin pruebas desactivadas | A | ✅ | Ninguna |
| QA-24 | Cobertura medida | M | ✅ | `COV` con umbral en el build |
| QA-25 | Regresión antes de publicar | A | ✅ | `CI` en cada push y pull request |

## 17. Infraestructura y despliegue

Kit de instalación listo y probado (`docs/13-produccion.md`). Queda lo que depende de elegir el servidor y el dominio (decisión de negocio).

| ID | Criterio | Pr. | Estado | Nota |
| -- | -------- | --- | ------ | ---- |
| DEP-01 | Infraestructura definida | A | ⏳ | Kit de instalación listo y probado; recomendación: VPS Ubuntu con 2 GB (`docs/13` §2). Falta la decisión de la responsable (`docs/11` #17) |
| DEP-02 | HTTPS backend | B | ⏳ | HTTPS en el mismo dominio para app y API, con redirección y HSTS: probado con certificado local (`KIT`). El certificado real se emite solo al instalar con el dominio (`docs/11` #18-19) |
| DEP-03 | HTTPS frontend | B | ⏳ | Igual que DEP-02: probado con certificado local (`KIT`); falta el dominio |
| DEP-04 | Secretos inyectados de forma segura | A | ✅ | Perfil `prod` solo con variables de entorno (`E2E`, `CI`) |
| DEP-05 | Sin credenciales productivas en el repo | B | ✅ | Revisado |
| DEP-06 | PostgreSQL no expuesto | A | ✅ | Sin puertos publicados; inaccesible desde fuera, verificado en cada CI (`KIT`) |
| DEP-07 | Puertos mínimos | A | ✅ | Solo 80 y 443 (servidor web); SSH 22 para administrar (`KIT`, `docs/13` §7) |
| DEP-08 | Firewall | A | ⏳ | `ufw` con 22/80/443 documentado (`docs/13` §3 y §7); se aplica al instalar |
| DEP-09 | Dominio | M | ⏳ | Decisión de la responsable (`docs/11` #18) |
| DEP-10 | Certificados | A | ⏳ | Emisión y renovación automáticas con Caddy (Let's Encrypt); se comprueba al instalar con el dominio |
| DEP-11 | CORS autorizado | A | ✅ | App y API en el mismo dominio (no requiere CORS); `CORS_ALLOWED_ORIGINS` = `https://` + dominio, automático (`KIT`) |
| DEP-12 | Swagger deshabilitado en producción | A | ✅ | 404 con perfil `prod` (verificado 23/11) |
| DEP-13 | Arranque automático | M | ✅ | Todos los servicios se reinician solos: corte de luz simulado → todo arriba en 14 s (`DRILL`) |
| DEP-14 | Actualizar sin perder información | A | ✅ | `UPG` |
| DEP-15 | Procedimiento de rollback | A | ✅ | Vuelta atrás automática si la verificación falla, probada con una versión rota (`DRILL`); con cambios en la base: `docs/09` §4.1 y `import-data.sh` |
| DEP-16 | Versión desplegada verificable | Bj | ✅ | `/actuator/info` (versión, fecha y commit) y *Mi perfil*; `verify.sh` comprueba que sea la recién instalada (`KIT`, `T-per`) |
| DEP-17 | Producción y pruebas separadas | B | ✅ | Bases y perfiles distintos |
| DEP-18 | Verificación tras desplegar | M | ✅ | `deploy.sh` ejecuta `verify.sh` (18 comprobaciones) en cada instalación (`KIT`) |
| DEP-19 | Migraciones en entorno previo | A | ✅ | `UPG` en un entorno aparte con la versión anterior |
| DEP-20 | Recuperación del servicio documentada | M | ✅ | `docs/09` §3 (recuperación) y §7 (simulacro) |

Referencias para la revisión final: OWASP ASVS nivel 2 y OWASP Top 10.

## 18. Errores y resiliencia

| ID | Criterio | Pr. | Estado | Nota |
| -- | -------- | --- | ------ | ---- |
| RES-01 | Fallo de PostgreSQL sin corrupción | A | ✅ | `kill -9` de PostgreSQL y corte de luz con ventas en curso: 0 de 13.159 ventas confirmadas perdidas; recuperación automática (WAL) e integridad correcta (`DRILL`) |
| RES-02 | Fallo del backend sin operaciones parciales | B | ✅ | `T-conc` |
| RES-03 | Errores de red comunicados | A | ✅ | `E2E` |
| RES-04 | Reintentos sin duplicar | B | ✅ | Todas las operaciones (ver API-14) |
| RES-05 | Errores inesperados registrados | A | ✅ | `T-log` |
| RES-06 | Procesos recuperables | A | ✅ | `E2E` |
| RES-07 | Consistencia transaccional | A | ✅ | `T-conc` |
| RES-08 | Sesiones vencidas | A | ✅ | `E2E` esc. 8: vencida sin conexión → sigue vendiendo → pide iniciar sesión → envía |
| RES-09 | Recuperación tras reinicios | M | ✅ | Base: UP en 1,2 s; backend: 9 s; corte de luz: 14 s; sin intervención (`DRILL`). Con la base caída, 503 y la venta queda en el dispositivo (`T-db`, `E2E`) |
| RES-10 | Saturación | Bj | ✅ | 40 usuarios sin errores de servidor; si se agotara la memoria, el proceso se reinicia en vez de quedar dañado (`LOAD`, `docs/12` §2.4) |
| RES-11 | Sin reenvío de formularios | A | ✅ | Ver UX-06 |
| RES-12 | Operaciones en estado incierto identificadas | A | ✅ | Ventas pendientes y rechazadas visibles (`E2E`) |

## 19. Documentación y operación

| ID | Criterio | Pr. | Estado | Nota |
| -- | -------- | --- | ------ | ---- |
| DOC-01 | README completo | M | ✅ | — |
| DOC-02 | Requisitos | M | ✅ | `docs/01` |
| DOC-03 | Instalación desde cero | A | ✅ | `docs/06` §14.1 (la `CI` la ejecuta) |
| DOC-04 | Configuración del backend | A | ✅ | `docs/06` §14.2 |
| DOC-05 | Configuración del frontend | A | ✅ | `frontend/README.md` |
| DOC-06 | Variables de entorno | A | ✅ | `docs/06` §14.2 |
| DOC-07 | Administrador inicial | A | ✅ | `.env.example`, `docs/06` |
| DOC-08 | Roles y permisos | A | ✅ | `docs/02` RN-24A |
| DOC-09 | Reglas de inventario | A | ✅ | `docs/02` |
| DOC-10 | Proceso de ventas | A | ✅ | `docs/02`, `docs/08` |
| DOC-11 | Proceso de compras | M | ✅ | `docs/14` §10 (compras) y §5 (su efecto en el inventario) |
| DOC-12 | Producción y pedidos | M | ✅ | `docs/14` §6 (producción) y §4 (personalizadas con abonos y pagos) |
| DOC-13 | Offline | A | ✅ | `docs/06` §15.4, `docs/08` §30A |
| DOC-14 | Copias y restauración | A | ✅ | `docs/09` |
| DOC-15 | Despliegue | A | ✅ | `docs/13-produccion.md`: opciones, instalación paso a paso, actualizar, volver atrás, copias, seguridad y pruebas |
| DOC-16 | Rollback | A | ✅ | `docs/09` §4.1 |
| DOC-17 | Manual de usuario | A | ✅ | `docs/14-manual-de-usuario.md`: administrador y vendedor, con capturas reales del ensayo (`ACC`) |
| DOC-18 | Solución de problemas | M | ✅ | Servidor: `docs/12` §6; copias: `docs/09`; sin conexión: `docs/06` §15.4 |
| DOC-19 | Coincide con el sistema | A | ✅ | Revisado el 23-24/11 |
| DOC-20 | Historial de versiones | M | ✅ | `CHANGELOG.md` desde las versiones iniciales hasta la Fase 8 |

## 20. Aceptación del negocio

NEG-01 a NEG-14: ⏳ **pendientes de validación por la responsable del negocio**. Guía, preguntas y acta para firmar: **`docs/15-aceptacion.md`**.

Preparación ya hecha: los 24 escenarios del checklist de la auditoría se ensayaron por la interfaz, **24/24 correctos** (`ACC`). El recorrido humano confirma que eso es lo que la tienda necesita, con precios y procedimientos reales. Cada escenario aprobado se registra aquí con fecha.

---

## Resumen

Conteo de la matriz (25/11/2026, después de la auditoría externa). Evolución: inicio 189 ✅ → Fase 1 256 → Fase 2 258 → Fases 3-4 275 → Fase 5 288 → Fase 6 315 → Fase 7 324 → Fase 8 335 → auditoría externa 336 ✅ (BKP-02 y QA-22 comprobados; SEG-18 vuelve a pendiente hasta instalar con claves nuevas).

| Área | Criterios | ✅ | ⏳ | ❌ |
| ---- | --------- | -- | -- | -- |
| 1. Arquitectura y calidad del código | 20 | 17 | 3 | 0 |
| 2. Backend y API REST | 20 | 17 | 3 | 0 |
| 3. Base de datos y persistencia | 20 | 18 | 2 | 0 |
| 4. Productos, categorías y unidades | 18 | 17 | 1 | 0 |
| 5. Inventario y movimientos | 20 | 20 | 0 | 0 |
| 6. Ventas | 33 | 33 | 0 | 0 |
| 7. Compras | 15 | 14 | 1 | 0 |
| 8. Producción y pedidos personalizados | 18 | 17 | 1 | 0 |
| 9. Usuarios, roles y seguridad | 25 | 23 | 2 | 0 |
| 10. Funcionamiento offline y sincronización | 20 | 20 | 0 | 0 |
| 11. Frontend y experiencia de usuario | 25 | 16 | 9 | 0 |
| 12. Fotografías y archivos | 12 | 9 | 3 | 0 |
| 13. Rendimiento | 15 | 13 | 2 | 0 |
| 14. Copias de seguridad y recuperación | 20 | 17 | 3 | 0 |
| 15. Logs, monitoreo y observabilidad | 15 | 14 | 1 | 0 |
| 16. Pruebas automatizadas y calidad continua | 25 | 25 | 0 | 0 |
| 17. Infraestructura y despliegue | 20 | 14 | 6 | 0 |
| 18. Errores y resiliencia | 12 | 12 | 0 | 0 |
| 19. Documentación y operación | 20 | 20 | 0 | 0 |
| 20. Aceptación del negocio | 14 | 0 | 14 | 0 |
| **Total** | **387** | **336** (87 %) | **51** | **0** |

Bloqueadores absolutos (sección 0): **11 cerrados**, **0 abiertos** y **1 por cerrar al instalar** (7: claves nuevas).

## Ruta propuesta

| Fase | Contenido | Cierra |
| ---- | --------- | ------ |
| **1. Reglas funcionales** ✅ | Hecha el 24/11/2026: identificador único en compras, producción, personalizadas y pagos (V21); 39 pruebas nuevas; motivo obligatorio; filtros del historial; 400 en lugar de 500 con datos mal escritos. Quedan 2 decisiones de negocio (nombres repetidos, estado "cancelada") | B-2, B-4, API-14, COM-*, PRO-*, DES-*, VEN-*, INV-14/17-19 |
| **2. Offline** ✅ | Hecha el 24/11/2026: escenarios A–D más dos pestañas, envíos simultáneos, sesión vencida sin conexión y dispositivo sin espacio. Decisiones aplicadas: nombres únicos y sin estado "cancelada" | OFF-*, RES-08, PRO-10/18 |
| **3. Seguridad** ✅ | Hecha el 24/11/2026: bloqueo tras 5 intentos; 12 pruebas de seguridad con inicio de sesión real; escaneo de dependencias en CI (encontró y se corrigieron vulnerabilidades críticas); Dependabot | SEG-*, B-11 |
| **4. Recuperación** ✅ | Hecha el 24/11/2026: actualización V19 → V21 con datos sin pérdidas; simulacro de pérdida total con 3 años de datos (7,5 s); `restore-db.sh`; procedimiento de vuelta atrás; RPO/RTO propuestos. Los `.ps1` se ejecutaron en el PC de Windows el 24/11 | BKP-17/18, DB-09, DEP-14/15/19, B-8/9/11 |
| **5. Calidad** ✅ | Hecha el 24/11/2026: cobertura 92 % con umbral en el build; 13 pruebas nuevas (104 en total) incluido el recorrido completo por HTTP; orden aleatorio; código muerto eliminado; `.editorconfig`; E2E por todas las páginas y roles. `main` protegida desde el 24/11 | QA-18/24, ARQ-09/13/17, UX-01/14 |
| **6. Rendimiento** ✅ | Hecha el 24/11/2026: prueba de carga con 3 años de datos y 40 usuarios (encontró que el servidor se quedaba sin memoria y daba 123 errores 500; corregido); historiales por periodo; N+1 eliminado; listas de a 100; estado del sistema (`/actuator/health`); log a archivo con rotación y una línea por operación; guía de incidentes (`docs/12`); 15 pruebas nuevas (119 en total). **Queda en manos de la dueña:** confirmar cuántas personas usan la app a la vez | REN-*, DB-17/18, API-12, OBS-*, RES-10, DOC-18 |
| **7. Producción** ✅ | Hecha el 24/11/2026: kit `deploy/` (Docker, HTTPS automático, mismo dominio, PostgreSQL cerrado, copias, reinicio automático); `deploy.sh` con copia previa, verificación y vuelta atrás automática; `import-data.sh`; versión visible; simulacros de caída de la base, corte de luz y versión rota sin perder ventas; corregido el 401 con la base caída (ahora 503 y la venta queda en el dispositivo); CI instala el kit y repite la E2E por HTTPS. **Queda en manos de la dueña:** servidor, dominio e instalación (`docs/11` #17-23) | DEP-*, RES-01/09, DOC-15 |
| **Auditoría externa** ✅ | Hecha el 25/11/2026 (82/100): 5 comprobaciones obligatorias y rama actualizada en `main`; CodeQL, secret scanning, push protection y reporte privado; permisos mínimos y acciones fijadas en la CI; `SECURITY.md`; límites de acceso por cuenta, conexión y registro guardados en la base (V22, 16 pruebas nuevas, 135 en total). **Por decisión de la dueña, para después:** copia externa y despliegue | SEG-21, QA-22, BKP-02 |
| **8. Aceptación** ⏳ | Preparada el 24/11/2026: los 24 escenarios ensayados por la interfaz (24/24, `ACC`); manual de usuario con capturas (`docs/14`); guía, criterios del negocio y acta (`docs/15`); aviso al salir con datos sin guardar; copias legibles solo por su dueño; `CHANGELOG.md`. **Falta lo que solo hace la dueña:** recorrer los escenarios con datos reales y firmar el acta (`docs/11` #15-16) | NEG-*, DOC-11/12/17/20, UX-*, BKP-11 |
