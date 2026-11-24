# Matriz de certificación 10/10

| Campo                    | Valor                          |
| ------------------------ | ------------------------------ |
| **Proyecto**             | Mantas Guajiras                |
| **Documento**            | Matriz de certificación        |
| **Código**               | DOC-10                         |
| **Versión**              | v1.0.0-rc.1                    |
| **Estado**               | En curso                       |
| **Última actualización** | 24/11/2026 (Fase 1)            |

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
| `T-ctx` | `BackendApplicationTests`: las 21 migraciones desde una base vacía y validación del esquema | `backend/src/test` |
| `T-perm` | `RolePermissionsTest` (9 pruebas HTTP por rol) | `backend/src/test` |
| `T-conc` | `ConcurrentSaleTest` (6): concurrencia, rechazo, rollback, idempotencia, cambio de unidad | `backend/src/test` |
| `T-prod` | `ProductRulesTest` (11): categoría ↔ unidad, enteros, precios | `backend/src/test` |
| `T-img` | `ImageSignatureTest` (6): firma real de imágenes | `backend/src/test` |
| `T-sale` | `SalePricingTest` (9): caso $80.000 → $70.000, varios productos, precio histórico, rechazos, producto desactivado, ventas inmodificables | `backend/src/test` |
| `T-com` | `PurchaseFlowTest` (6): entradas, total, comprable/activo, sin cambios parciales, reintento, inmodificables | `backend/src/test` |
| `T-pro` | `ProductionFlowTest` (5): consumo de tela, mantas, insuficiente, validaciones, reintento | `backend/src/test` |
| `T-ord` | `CustomOrderFlowTest` (6): abono, pagos, saldo, pagada, límites, reintentos de pedido y pago | `backend/src/test` |
| `T-mov` | `MovementFilterTest` (3): ajuste 15 → 12 con motivo y usuario; filtros por tipo, origen, usuario y fechas | `backend/src/test` |
| `T-api` | `ApiValidationTest` (10): datos inválidos por HTTP → 400/404/405, nunca 500 | `backend/src/test` |
| `T-log` | `UnexpectedErrorLoggingTest`: error interno registrado con código, sin detalles al usuario | `backend/src/test` |
| `E2E` | `frontend/e2e/offline-sync.e2e.cjs` (7 escenarios, 30 comprobaciones, navegador real) | se ejecuta en CI |
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
| 5 | Una venta puede quedar guardada parcialmente | ✅ No ocurre | `T-conc` (rollback) |
| 6 | La sincronización offline atribuye ventas al usuario equivocado | ✅ No ocurre | `E2E` escenario 4 |
| 7 | Se exponen contraseñas o secretos | ✅ No ocurre | `.gitignore` revisado; `log-resolved-exception: false`; `T-log` (sin detalles al usuario); contraseñas con BCrypt |
| 8 | Se pierden datos al restaurar o actualizar | ⏳ | Restaurar nunca sobrescribe (`BKP`). Falta probar una actualización de versión sobre una base con datos (DB-09). |
| 9 | Las migraciones destruyen datos existentes | ⏳ | V20 solo agrega columnas. Falta la prueba DB-09. |
| 10 | Los backups no pueden restaurarse | ✅ (Linux) · ⏳ (Windows) | `BKP` con los scripts `.sh`. Los `.ps1` deben ejecutarse una vez en el PC del negocio. |
| 11 | Vulnerabilidades críticas sin resolver | ⏳ | `AUD` frontend: 0. Falta escanear las dependencias Maven del backend. |
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
| ARQ-09 | Sin código sin utilidad | Bj | ⏳ | Se borraron 5 archivos vacíos; falta un análisis completo |
| ARQ-10 | Sin lógica de negocio duplicada | M | ⏳ | Categoría → unidad se calcula en frontend y backend (el backend manda) |
| ARQ-11 | Excepciones centralizadas | M | ✅ | `GlobalExceptionHandler`, `T-log` |
| ARQ-12 | Nombres comprensibles | Bj | ⏳ | `/api/unit` en singular frente al resto en plural |
| ARQ-13 | Sin imports ni código muerto | Bj | ⏳ | ESLint lo cubre en frontend; backend sin herramienta |
| ARQ-14 | Configuración dev/test/prod separada | A | ✅ | Perfiles; la `CI` usa `prod` y `test` |
| ARQ-15 | Los cambios no rompen lo existente | A | ✅ | `CI` en cada push |
| ARQ-16 | Sin advertencias de compilación | M | ✅ | Compilación con `showDeprecation`: 0 advertencias |
| ARQ-17 | Formato y estilo consistentes | Bj | ❌ | Indentación irregular (p. ej. `ProductCategoryController`, `DataInitializer`); sin formateador |
| ARQ-18 | Operaciones complejas documentadas | M | ✅ | Comentarios en inventario, ventas y offline; `docs/08` §30A |
| ARQ-19 | Dependencias justificadas y al día | M | ⏳ | `AUD` frontend; backend sin escaneo ni Dependabot |
| ARQ-20 | Decisiones arquitectónicas documentadas | M | ✅ | `docs/03`, `docs/02` RN-24A y RN-27 |

`BaseEntity` / `AuditableEntity`: no se considera defecto (decisión acordada).

## 2. Backend y API REST

| ID | Criterio | Pr. | Estado | Evidencia / nota |
| -- | -------- | --- | ------ | ---------------- |
| API-01 | Códigos HTTP apropiados | M | ⏳ | 400/401/403/404 probados (`T-perm`); faltan los demás por endpoint |
| API-02 | Estructura de respuesta consistente | M | ⏳ | Implementado (`ErrorResponse`); falta prueba |
| API-03 | Mensajes de error claros | M | ⏳ | Implementado; falta revisión por endpoint |
| API-04 | Validación en el servidor | A | ✅ | `T-perm` (400 con cuerpo vacío), `T-prod` |
| API-05 | Rechaza valores inválidos sin depender del frontend | A | ✅ | `T-prod`, `T-conc` (manta en metros) |
| API-06 | Identificadores inexistentes controlados | M | ✅ | `T-api` (404 inexistente, 400 mal escrito; antes 500) |
| API-07 | Recursos desactivados manejados | M | ✅ | `T-sale`, `T-com` (producto desactivado rechazado) |
| API-08 | Transacciones en operaciones múltiples | A | ✅ | `REV` + `T-conc` |
| API-09 | Rollback ante error | A | ✅ | `T-conc` |
| API-10 | No expone trazas ni datos sensibles | A | ✅ | `T-log` |
| API-11 | Filtros validan parámetros | Bj | ✅ | Filtros del historial validados (`T-mov`, `T-api`). `status` de personalizadas ignora valores desconocidos |
| API-12 | Consultas sin cargar registros innecesarios | M | ❌ | Historiales sin paginación; ver REN-03 |
| API-13 | Integridad de relaciones | A | ✅ | Claves foráneas (`T-ctx`) |
| API-14 | Operaciones críticas idempotentes ante repetición | B | ✅ | Ventas, compras, producción, personalizadas y pagos (`T-conc`, `T-com`, `T-pro`, `T-ord`, `E2E` esc. 5) |
| API-15 | No se modifican campos no controlables | A | ⏳ | Implementado (mappers ignoran id, total, createdBy); falta prueba |
| API-16 | Documentación de la API coincide | M | ⏳ | `docs/05` revisado a mano contra los controladores; sin prueba automática |
| API-17 | Errores inesperados en log sin secretos | A | ✅ | `T-log` (corregido el 24/11: antes no se registraban) |
| API-18 | Endpoints con pruebas de integración | M | ✅ | `T-sale`, `T-com`, `T-pro`, `T-ord`, `T-mov`, `T-api`, `T-perm` |
| API-19 | Tiempos de espera y fallos de dependencias | M | ⏳ | Frontend: 20 s. Backend: valores por defecto del pool |
| API-20 | Correcta con solicitudes simultáneas | A | ✅ | `T-conc` |

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
| DB-07 | Migraciones versionadas | A | ✅ | V1–V21 |
| DB-08 | Migran desde base vacía | A | ✅ | `T-ctx`, `CI`, `E2E` |
| DB-09 | Base existente se actualiza desde la versión anterior | B | ⏳ | Falta prueba con datos en V19 → V20 |
| DB-10 | Esquema = entidades | A | ✅ | `ddl-auto: validate` en `T-ctx` |
| DB-11 | Dinero en decimales | A | ✅ | `NUMERIC(12,2)` / `BigDecimal` |
| DB-12 | Sin punto flotante para dinero | A | ✅ | Búsqueda en código: ningún `double`/`float` |
| DB-13 | Fechas y zonas coherentes | M | ⏳ | Las ventas offline convierten la zona (`T-conc`); el servidor usa su zona local: fijar `TZ` en el despliegue |
| DB-14 | Sin registros huérfanos | A | ✅ | Claves foráneas + `integrity-check.sql` |
| DB-15 | Consistencia transaccional | A | ✅ | `T-conc` |
| DB-16 | Concurrencia en registros críticos | A | ✅ | Bloqueo pesimista de inventario, `T-conc` |
| DB-17 | Rendimiento aceptable | M | ⏳ | Sin medir |
| DB-18 | Sin N+1 importantes | M | ❌ | El historial de ventas carga los productos de cada venta por separado |
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
| PROD-17 | Búsquedas y filtros | M | ⏳ | Falta prueba |
| PROD-18 | Cambios reflejados en inventario | A | ✅ | `E2E` (stock inicial) |

**Casos extremos (comportamiento definido):**

| Caso | Comportamiento actual | Estado |
| ---- | --------------------- | ------ |
| Dos productos con el mismo nombre | Se permite (solo el código de barras y el código interno son únicos) | ⏳ Decisión de negocio pendiente |
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
| PRO-10 | Transiciones de estado válidas | M | ⏳ | Estado calculado (pendiente → pagada por pagos). **No existe "cancelada"**: decisión de negocio pendiente |
| PRO-11 | Pagos y abonos | A | ✅ | `T-ord` |
| PRO-12 | Saldos correctos | A | ✅ | `T-ord` |
| PRO-13 | Sin pagos superiores al saldo | A | ✅ | `T-ord` |
| PRO-14 | Movimientos del pedido conservados | A | ✅ | `T-ord` (salida con origen `ORDER`) |
| PRO-15 | Permisos ADMIN/VENDEDOR | A | ⏳ | Decisión: ambos roles. Falta prueba de permisos |
| PRO-16 | Sin duplicados por reintento | B | ✅ | `T-pro`, `T-ord` |
| PRO-17 | Transaccional | A | ✅ | `T-pro`, `T-ord` |
| PRO-18 | Historial de completados/cancelados | M | ⏳ | Depende de PRO-10 |

## 9. Usuarios, roles y seguridad

| ID | Criterio | Pr. | Estado | Evidencia / nota |
| -- | -------- | --- | ------ | ---------------- |
| SEG-01 | Inicio de sesión | B | ✅ | `E2E`, `MAN` |
| SEG-02 | Hash seguro | B | ✅ | BCrypt (`REV`) |
| SEG-03 | Contraseñas nunca en respuestas | B | ⏳ | `UserResponse` no tiene el campo (`REV`); falta prueba |
| SEG-04 | JWT validado | B | ✅ | `T-perm`, `E2E` |
| SEG-05 | Tokens vencidos rechazados | A | ⏳ | Implementado; falta prueba |
| SEG-06 | Rutas protegidas | B | ✅ | `T-perm` |
| SEG-07 | Roles en el backend | B | ✅ | `T-perm` |
| SEG-08 | Vendedor sin funciones de administrador | B | ✅ | `T-perm` |
| SEG-09 | No cambiar el rol propio | B | ✅ | `/users/**` solo ADMIN (`T-perm`); `/api/me` no acepta rol (`REV`) |
| SEG-10 | Sin acceso a recursos ajenos por id | A | ⏳ | `/api/me` usa la sesión, no un id (`REV`); falta prueba |
| SEG-11 | Inactivos no entran | A | ⏳ | Implementado (cuenta deshabilitada, rechazada en cada petición); falta prueba |
| SEG-12 | Identificadores únicos | A | ✅ | UNIQUE |
| SEG-13 | Teléfonos únicos | A | ⏳ | Implementado; falta prueba |
| SEG-14 | Usuarios únicos | A | ⏳ | Implementado; falta prueba |
| SEG-15 | Sesiones correctas | A | ✅ | `E2E` |
| SEG-16 | Cierre de sesión | A | ✅ | `E2E` |
| SEG-17 | Sin datos sensibles en logs | A | ✅ | `log-resolved-exception: false`; `T-log` |
| SEG-18 | Sin secretos en git | B | ✅ | `.gitignore` y `git ls-files` revisados |
| SEG-19 | CORS solo autorizados | A | ⏳ | Configurable (`CORS_ALLOWED_ORIGINS`); valor de producción pendiente |
| SEG-20 | Endpoints sensibles protegidos | B | ✅ | `T-perm` |
| SEG-21 | Protección contra fuerza bruta | A | ❌ | No hay límite de intentos de inicio de sesión |
| SEG-22 | Sin inyección | A | ✅ | JPA con parámetros, Bean Validation (`REV`) |
| SEG-23 | Sin XSS | A | ✅ | Sin HTML sin escapar (`REV`); CSP sin violaciones (`E2E`) |
| SEG-24 | Dependencias revisadas | A | ⏳ | `AUD` frontend 0; backend pendiente |
| SEG-25 | Errores de autenticación sin información extra | A | ✅ | Mensaje único "Usuario o contraseña incorrectos" (`REV`) |

**Pruebas HTTP de permisos:** vendedor crea producto ✅ · ve historial monetario ✅ · registra compra ✅ · crea administrador (`POST /users`) ⏳ · cambia su rol ⏳ · sin sesión registra venta ⏳ · token vencido ⏳.

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
| OFF-07 | Sin duplicados al repetir | B | ✅ | `E2E` esc. 2, `T-conc` |
| OFF-08 | Venta confirmada no se reprocesa | B | ✅ | `E2E`, `T-conc` |
| OFF-09 | Informa pendientes | A | ✅ | `E2E` |
| OFF-10 | Informa fallos | A | ✅ | `E2E` esc. 3 |
| OFF-11 | Errores recuperables | A | ✅ | `E2E` ("Reintentar") |
| OFF-12 | Interrupción en la transmisión | A | ✅ | `E2E` esc. 1–2 |
| OFF-13 | Interrupción en la respuesta | B | ✅ | `E2E` esc. 2 |
| OFF-14 | Sin pérdidas al reiniciar el navegador | A | ✅ | `E2E` |
| OFF-15 | Cambio de usuario con pendientes | B | ✅ | `E2E` esc. 4 |
| OFF-16 | Identidad correcta | B | ✅ | `E2E` esc. 4 |
| OFF-17 | Stock validado al sincronizar | B | ✅ | `E2E` esc. 3, `T-conc` |
| OFF-18 | Conflictos local/remoto | A | ✅ | `E2E` esc. 3 |
| OFF-19 | No se presenta como confirmada | A | ✅ | "Guardada sin conexión", "Total estimado" (`E2E`) |
| OFF-20 | Resultado verificable por operación | M | ✅ | `client_request_id` en el servidor + estado en el dispositivo |

**Escenarios A, B, C y D:** ✅ (`E2E` escenarios 1, 2, 4 y 3; se ejecutan en cada push en `CI`).

## 11. Frontend y experiencia de usuario

| ID | Criterio | Pr. | Estado | Evidencia / nota |
| -- | -------- | --- | ------ | ---------------- |
| UX-01 | Todas las páginas cargan | A | ⏳ | `MAN` sin registro; falta recorrido automático por página |
| UX-02 | Navegación coherente | M | ⏳ | `MAN` |
| UX-03 | Formularios validan obligatorios | A | ⏳ | Implementado; falta revisión sistemática |
| UX-04 | Errores con causa | A | ✅ | Mensaje real del backend (`apiClient`); `E2E` (motivo del rechazo) |
| UX-05 | Estados de carga | M | ⏳ | Implementados; falta revisión |
| UX-06 | Sin envíos dobles | A | ✅ | `REV` + identificador estable en formularios (`E2E` esc. 5) |
| UX-07 | Confirmación en acciones destructivas | A | ⏳ | Descartar venta y cerrar sesión con pendientes ✅; falta revisar desactivar usuario o producto |
| UX-08 | Tablas legibles | M | ⏳ | `MAN` |
| UX-09 | Filtros | M | ⏳ | Falta prueba |
| UX-10 | Sin pérdida de datos de formularios | M | ⏳ | Falta revisión |
| UX-11 | Pendiente frente a confirmada | A | ✅ | `E2E` |
| UX-12 | Fotos | M | ✅ | `MAN` |
| UX-13 | Éxito solo si se completó | A | ✅ | `E2E` (registrada frente a guardada sin conexión) |
| UX-14 | Permisos reflejados | A | ⏳ | `REV` (menú y botones por rol); falta prueba con vendedor |
| UX-15 | Errores de red | A | ✅ | `E2E` |
| UX-16 | Estados vacíos | Bj | ⏳ | Implementados en varias pantallas; falta revisión |
| UX-17 | Tamaños de pantalla | M | ⏳ | Diseño responsive; falta prueba en celular |
| UX-18 | Teclado | M | ⏳ | Falta auditoría |
| UX-19 | Etiquetas | M | ⏳ | Falta auditoría |
| UX-20 | Consistencia visual | Bj | ⏳ | — |
| UX-21 | Ortografía | Bj | ⏳ | — |
| UX-22 | Diálogos | M | ⏳ | `Modal` con Escape; falta revisión |
| UX-23 | Evitar perder información al salir | M | ❌ | Sin aviso al salir con un formulario a medio llenar |
| UX-24 | Errores de sesión | A | ✅ | 401 → inicio con aviso de sesión vencida (`REV`, `E2E`) |
| UX-25 | Retroalimentación tras cada operación | M | ⏳ | — |

**WCAG 2.2 AA:** ⏳ pendiente (contraste, foco visible, teclado, lectores de pantalla).

## 12. Fotografías y archivos

| ID | Criterio | Pr. | Estado | Evidencia |
| -- | -------- | --- | ------ | --------- |
| IMG-01 | Subir | M | ✅ | `MAN` |
| IMG-02 | 20 MB en frontend y backend | M | ⏳ | Implementado en ambos; falta prueba del 413 |
| IMG-03 | Formato real | A | ✅ | `T-img` |
| IMG-04 | Rechaza no imágenes | A | ✅ | `T-img` |
| IMG-05 | Archivos excesivos | M | ⏳ | Límites de Spring (`max-file-size`); falta prueba |
| IMG-06 | Recuperar | M | ✅ | `MAN` |
| IMG-07 | Actualizar | M | ⏳ | — |
| IMG-08 | Sin rutas internas | A | ✅ | Se guardan en la base (`REV`) |
| IMG-09 | Corruptos | M | ✅ | `T-img` (archivo truncado) |
| IMG-10 | No bloquea la app | Bj | ⏳ | — |
| IMG-11 | Fallos de transmisión | Bj | ⏳ | Tiempo límite de 120 s en el frontend |
| IMG-12 | Eliminación | Bj | ⏳ | — |

## 13. Rendimiento

| ID | Criterio | Pr. | Estado | Nota |
| -- | -------- | --- | ------ | ---- |
| REN-01 | Tiempos razonables | M | ⏳ | Sin medir |
| REN-02 | Sin datos innecesarios | M | ⏳ | — |
| REN-03 | Paginación | M | ❌ | Historiales de ventas, compras, movimientos y producción completos |
| REN-04 | Sin consultas costosas | M | ⏳ | — |
| REN-05 | Sin N+1 | M | ❌ | Ver DB-18 |
| REN-06 | Concurrencia consistente | A | ✅ | `T-conc` |
| REN-07 | Sin solicitudes innecesarias | Bj | ⏳ | Recarga automática cada 60 s por pantalla (intencional) |
| REN-08 | Imágenes | Bj | ⏳ | Sin redimensionar al subir |
| REN-09 | Pool de conexiones | M | ⏳ | Valores por defecto |
| REN-10 | Memoria | M | ⏳ | — |
| REN-11 | Usuarios simultáneos esperados | M | ⏳ | Falta definir la cifra |
| REN-12 | Tiempos bajo carga | M | ⏳ | Falta prueba de carga |
| REN-13 | Picos | Bj | ⏳ | — |
| REN-14 | Operaciones críticas | M | ⏳ | — |
| REN-15 | Límites conocidos | M | ⏳ | — |

## 14. Copias de seguridad y recuperación

| ID | Criterio | Pr. | Estado | Evidencia / nota |
| -- | -------- | --- | ------ | ---------------- |
| BKP-01 | Procedimiento automatizado | B | ✅ | `BKP` |
| BKP-02 | Periódico | A | ⏳ | Tarea de Windows cada 2 h (6:00–21:00): confirmar en el PC del negocio |
| BKP-03 | Comprimido | M | ✅ | `--compress=9` |
| BKP-04 | Integridad por archivo | A | ✅ | `pg_restore --list` + SHA-256 (`BKP`) |
| BKP-05 | SHA-256 | A | ✅ | `BKP` |
| BKP-06 | Sin checksum válido → rechazo | A | ✅ | `BKP` (24/11) |
| BKP-07 | Fuera del equipo | A | ⏳ | `CopyTo` (configuración del usuario) |
| BKP-08 | Segunda ubicación | A | ⏳ | Ídem |
| BKP-09 | Retención | M | ✅ | `BKP` |
| BKP-10 | Retención en principal y secundaria | M | ✅ | `BKP` (24/11) |
| BKP-11 | Protección contra acceso no autorizado | A | ❌ | Copias sin cifrar; dependen de los permisos de la carpeta |
| BKP-12 | Restaurar base completa | B | ✅ | `BKP` (restaurada y verificada) |
| BKP-13 | En entorno aislado | A | ✅ | Base temporal |
| BKP-14 | Integridad tras restaurar | A | ✅ | `integrity-check.sql` |
| BKP-15 | Registros y relaciones completos | A | ✅ | Conteos comparados (`BKP`) |
| BKP-16 | Procedimiento documentado | A | ✅ | `docs/09` |
| BKP-17 | Tiempo de recuperación medido | A | ⏳ | Falta simulacro cronometrado |
| BKP-18 | Pérdida total de la base principal | A | ⏳ | Falta simulacro |
| BKP-19 | Sin errores silenciosos | A | ✅ | La prueba falla con código ≠ 0 y deja aviso en el Escritorio |
| BKP-20 | Historial de copias | M | ✅ | `backup.log` |

**RPO / RTO propuestos (a aprobar):** RPO **2 horas** en horario de operación (copias cada 2 h de 6:00 a 21:00 y al encender el equipo). RTO **4 horas**, pendiente de demostrar con un simulacro (BKP-17/18).

## 15. Logs, monitoreo y observabilidad

| ID | Criterio | Pr. | Estado | Nota |
| -- | -------- | --- | ------ | ---- |
| OBS-01 | Errores registrados | A | ✅ | `T-log` (corregido 24/11) |
| OBS-02 | Operaciones críticas registradas | M | ⏳ | Quedan en la base con usuario y fecha, no en el log |
| OBS-03 | Errores por fecha y contexto | A | ✅ | Método, ruta y código (`T-log`) |
| OBS-04 | Fallos de sincronización | M | ❌ | Solo visibles en el dispositivo |
| OBS-05 | Fallos de base de datos | A | ⏳ | Los no previstos se registran; los conflictos (409) no |
| OBS-06 | Estado del backend | M | ❌ | Sin endpoint de salud |
| OBS-07 | Estado de PostgreSQL | M | ❌ | — |
| OBS-08 | Espacio en disco | M | ❌ | — |
| OBS-09 | Crecimiento de logs | M | ❌ | Log solo por consola |
| OBS-10 | Alertas críticas | M | ⏳ | Solo las copias avisan (archivo en el Escritorio) |
| OBS-11 | Rotación de logs | M | ❌ | — |
| OBS-12 | Usuario por operación | A | ✅ | `created_by` |
| OBS-13 | Solicitud ↔ error | M | ✅ | Código de referencia (`T-log`) |
| OBS-14 | Problemas de rendimiento | Bj | ❌ | — |
| OBS-15 | Respuesta a incidentes | M | ⏳ | Solo la recuperación de datos (`docs/09`) |

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
| QA-18 | Independientes del orden | M | ⏳ | Usan datos únicos; sin verificación en orden aleatorio |
| QA-19 | Pipeline: lint y build | A | ✅ | `CI` |
| QA-20 | Pipeline: backend | A | ✅ | `CI` |
| QA-21 | Un fallo bloquea el pipeline | A | ✅ | `CI` |
| QA-22 | Revisión antes de integrar | M | ⏳ | Se usó pull request; falta proteger `main` en GitHub |
| QA-23 | Sin pruebas desactivadas | A | ✅ | Ninguna |
| QA-24 | Cobertura medida | M | ❌ | Sin JaCoCo |
| QA-25 | Regresión antes de publicar | A | ✅ | `CI` en cada push y pull request |

## 17. Infraestructura y despliegue

Pendiente en su mayoría hasta elegir la infraestructura (decisión de negocio).

| ID | Criterio | Pr. | Estado | Nota |
| -- | -------- | --- | ------ | ---- |
| DEP-01 | Infraestructura definida | A | ⏳ | Decisión pendiente |
| DEP-02 | HTTPS backend | B | ⏳ | — |
| DEP-03 | HTTPS frontend | B | ⏳ | — |
| DEP-04 | Secretos inyectados de forma segura | A | ✅ | Perfil `prod` solo con variables de entorno (`E2E`, `CI`) |
| DEP-05 | Sin credenciales productivas en el repo | B | ✅ | Revisado |
| DEP-06 | PostgreSQL no expuesto | A | ⏳ | Documentado (`docs/06` §14.3) |
| DEP-07 | Puertos mínimos | A | ⏳ | — |
| DEP-08 | Firewall | A | ⏳ | — |
| DEP-09 | Dominio | M | ⏳ | — |
| DEP-10 | Certificados | A | ⏳ | — |
| DEP-11 | CORS autorizado | A | ⏳ | Configurable; falta el valor real |
| DEP-12 | Swagger deshabilitado en producción | A | ✅ | 404 con perfil `prod` (verificado 23/11) |
| DEP-13 | Arranque automático | M | ⏳ | — |
| DEP-14 | Actualizar sin perder información | A | ⏳ | Depende de DB-09 |
| DEP-15 | Procedimiento de rollback | A | ❌ | No documentado |
| DEP-16 | Versión desplegada verificable | Bj | ⏳ | — |
| DEP-17 | Producción y pruebas separadas | B | ✅ | Bases y perfiles distintos |
| DEP-18 | Verificación tras desplegar | M | ⏳ | — |
| DEP-19 | Migraciones en entorno previo | A | ⏳ | — |
| DEP-20 | Recuperación del servicio documentada | M | ⏳ | — |

Referencias para la revisión final: OWASP ASVS nivel 2 y OWASP Top 10.

## 18. Errores y resiliencia

| ID | Criterio | Pr. | Estado | Nota |
| -- | -------- | --- | ------ | ---- |
| RES-01 | Fallo de PostgreSQL sin corrupción | A | ⏳ | Garantizado por PostgreSQL (WAL); `docs/09` §1.5; sin simulacro |
| RES-02 | Fallo del backend sin operaciones parciales | B | ✅ | `T-conc` |
| RES-03 | Errores de red comunicados | A | ✅ | `E2E` |
| RES-04 | Reintentos sin duplicar | B | ✅ | Todas las operaciones (ver API-14) |
| RES-05 | Errores inesperados registrados | A | ✅ | `T-log` |
| RES-06 | Procesos recuperables | A | ✅ | `E2E` |
| RES-07 | Consistencia transaccional | A | ✅ | `T-conc` |
| RES-08 | Sesiones vencidas | A | ✅ | `REV`, `E2E` |
| RES-09 | Recuperación tras reinicios | M | ⏳ | — |
| RES-10 | Saturación | Bj | ⏳ | — |
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
| DOC-11 | Proceso de compras | M | ⏳ | Breve |
| DOC-12 | Producción y pedidos | M | ⏳ | Breve |
| DOC-13 | Offline | A | ✅ | `docs/06` §15.4, `docs/08` §30A |
| DOC-14 | Copias y restauración | A | ✅ | `docs/09` |
| DOC-15 | Despliegue | A | ⏳ | Genérico hasta definir la infraestructura |
| DOC-16 | Rollback | A | ❌ | — |
| DOC-17 | Manual de usuario | A | ❌ | — |
| DOC-18 | Solución de problemas | M | ❌ | Solo para copias y offline |
| DOC-19 | Coincide con el sistema | A | ✅ | Revisado el 23-24/11 |
| DOC-20 | Historial de versiones | M | ❌ | Sin `CHANGELOG` |

## 20. Aceptación del negocio

NEG-01 a NEG-14: ⏳ **pendientes de validación por la responsable del negocio** con el checklist de 24 escenarios de la auditoría (productos, inventario, ventas, compras y producción, usuarios y seguridad, offline). Cada escenario aprobado se registra aquí con fecha.

---

## Resumen

Conteo automático de la matriz (24/11/2026, después de la Fase 1). Antes de la Fase 1: 189 ✅ · 169 ⏳ · 29 ❌.

| Área | Criterios | ✅ | ⏳ | ❌ |
| ---- | --------- | -- | -- | -- |
| 1. Arquitectura y calidad del código | 20 | 13 | 6 | 1 |
| 2. Backend y API REST | 20 | 13 | 6 | 1 |
| 3. Base de datos y persistencia | 20 | 15 | 4 | 1 |
| 4. Productos, categorías y unidades | 18 | 17 | 1 | 0 |
| 5. Inventario y movimientos | 20 | 20 | 0 | 0 |
| 6. Ventas | 33 | 33 | 0 | 0 |
| 7. Compras | 15 | 14 | 1 | 0 |
| 8. Producción y pedidos personalizados | 18 | 15 | 3 | 0 |
| 9. Usuarios, roles y seguridad | 25 | 16 | 8 | 1 |
| 10. Funcionamiento offline y sincronización | 20 | 20 | 0 | 0 |
| 11. Frontend y experiencia de usuario | 25 | 7 | 17 | 1 |
| 12. Fotografías y archivos | 12 | 6 | 6 | 0 |
| 13. Rendimiento | 15 | 1 | 12 | 2 |
| 14. Copias de seguridad y recuperación | 20 | 14 | 5 | 1 |
| 15. Logs, monitoreo y observabilidad | 15 | 4 | 4 | 7 |
| 16. Pruebas automatizadas y calidad continua | 25 | 22 | 2 | 1 |
| 17. Infraestructura y despliegue | 20 | 4 | 15 | 1 |
| 18. Errores y resiliencia | 12 | 9 | 3 | 0 |
| 19. Documentación y operación | 20 | 13 | 3 | 4 |
| 20. Aceptación del negocio | 14 | 0 | 14 | 0 |
| **Total** | **387** | **256** (66 %) | **110** | **21** |

Bloqueadores absolutos (sección 0): **8 cerrados** (1, 2, 3, 4, 5, 6, 7, 12), **0 abiertos** y **4 por comprobar** (8 y 9: actualización con datos; 10: copias en Windows; 11: dependencias Maven).

## Ruta propuesta

| Fase | Contenido | Cierra |
| ---- | --------- | ------ |
| **1. Reglas funcionales** ✅ | Hecha el 24/11/2026: identificador único en compras, producción, personalizadas y pagos (V21); 39 pruebas nuevas; motivo obligatorio; filtros del historial; 400 en lugar de 500 con datos mal escritos. Quedan 2 decisiones de negocio (nombres repetidos, estado "cancelada") | B-2, B-4, API-14, COM-*, PRO-*, DES-*, VEN-*, INV-14/17-19 |
| **2. Offline** | Ya verificado (escenarios A–D en `CI`) | — |
| **3. Seguridad** | Límite de intentos de inicio de sesión; pruebas de token vencido, usuario inactivo y `POST /users` por vendedor; escaneo de dependencias Maven | SEG-05/11/21/24 |
| **4. Recuperación** | Ejecutar los `.ps1` en el PC del negocio; simulacro cronometrado de pérdida total; prueba de actualización V19 → V20 con datos; definir RPO y RTO | BKP-02/17/18, DB-09, B-8/9 |
| **5. Calidad** | JaCoCo, protección de `main`, formateo | QA-22/24, ARQ-17 |
| **6. Rendimiento** | Paginación, N+1 del historial, endpoint de salud, logs a archivo con rotación, prueba de carga | REN-*, DB-18, OBS-* |
| **7. Producción** | Infraestructura, HTTPS, dominio, CORS, rollback | DEP-* |
| **8. Aceptación** | Recorrido de los 24 escenarios con la responsable del negocio; manual de usuario | NEG-*, DOC-17 |
