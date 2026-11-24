# Documento de Base de Datos

| Campo                    | Valor                   |
| ------------------------ | ----------------------- |
| **Proyecto**             | Mantas Guajiras         |
| **Documento**            | Diseño de Base de Datos |
| **Código**               | DOC-04                  |
| **Versión**              | v1.0.0-rc.1            |
| **Estado**               | Versión candidata       |
| **Responsable**          | Equipo de Desarrollo    |
| **Última actualización** | 23/11/2026              |

---

## Control de versiones

| Versión | Fecha      | Descripción                                | Responsable          |
| ------- | ---------- | ------------------------------------------ | -------------------- |
| v0.1.0  | 29/07/2026 | Creación inicial del documento.            | Equipo de Desarrollo |
| v0.4.0  | 05/09/2026 | Actualización del modelo de base de datos. | Equipo de Desarrollo |
| v1.0.0-rc.1 | 23/11/2026 | Alineación con la implementación real tras la auditoría técnica. | Equipo de Desarrollo |

---

# 1. Introducción

Este documento describe el diseño de la base de datos del sistema **Mantas Guajiras**.

El objetivo es almacenar de forma organizada y consistente la información necesaria para el funcionamiento del sistema, incluyendo usuarios, productos, categorías, unidades, inventario, movimientos de inventario, ventas y producción.

La implementación de la base de datos se realiza utilizando PostgreSQL.

La estructura se gestiona mediante migraciones de Flyway para mantener un historial controlado de los cambios realizados en la base de datos.

---

# 2. Objetivos

La base de datos debe permitir:

* Gestionar usuarios y roles.
* Administrar productos.
* Clasificar productos mediante categorías.
* Definir las unidades utilizadas por los productos.
* Controlar las existencias actuales.
* Registrar movimientos de inventario.
* Registrar ventas y sus detalles.
* Registrar operaciones de producción.
* Mantener la trazabilidad de las operaciones relacionadas con el inventario.
* Garantizar la integridad de las relaciones entre las entidades.

---

# 3. Principios de diseño

El diseño de la base de datos sigue los siguientes principios:

* Evitar la duplicidad innecesaria de información.
* Mantener la integridad referencial.
* Separar la información del producto de su existencia en inventario.
* Mantener trazabilidad mediante movimientos de inventario.
* Utilizar relaciones entre entidades en lugar de duplicar información.
* Permitir la ampliación progresiva del sistema.
* Mantener consistencia entre las operaciones de negocio y el inventario.

---

# 4. Entidades principales

La estructura actual del sistema utiliza entidades generales para representar los productos y su inventario.

## Seguridad

* User
* Role

---

## Productos

* Product
* ProductCategory
* Unit

Los productos se clasifican mediante una categoría.

Actualmente las categorías principales corresponden a:

* Manta.
* Tela.

La unidad se determina de acuerdo con el tipo de producto:

* Las mantas se manejan en unidades enteras (#).
* Las telas se manejan en metros.

---

## Inventario

* Inventory
* InventoryMovement
* MovementType
* SourceType

`Inventory` almacena la existencia actual de cada producto.

`InventoryMovement` registra los movimientos que modifican dicha existencia.

`MovementType` identifica si el movimiento corresponde a una entrada (`IN`) o una salida (`OUT`).

`SourceType` identifica el origen de la operación, como compra, venta, producción o ajuste.

---

## Ventas

La gestión de ventas utiliza entidades relacionadas con:

* Venta.
* Detalle de venta.

El detalle conserva el producto, la cantidad y el precio aplicado en la operación.

---

## Producción

La producción utiliza los productos existentes para representar tanto los materiales consumidos como los productos generados.

Una operación de producción puede consumir tela y generar mantas.

---

# 5. Relaciones principales

El modelo de datos contempla relaciones como las siguientes:

* Un rol puede estar asociado a múltiples usuarios.
* Una categoría puede estar asociada a múltiples productos.
* Una unidad puede estar asociada a múltiples productos.
* Un producto tiene un registro de inventario asociado.
* Un producto puede tener múltiples movimientos de inventario.
* Un movimiento de inventario pertenece a un único producto.
* Una venta puede contener múltiples detalles de venta.
* Cada detalle de venta corresponde a un producto.
* Una producción puede generar movimientos de salida y entrada de inventario.

La relación entre producto e inventario permite mantener separada la información descriptiva del producto de su existencia actual.

---

# 6. Integridad de los datos

La base de datos y el backend trabajan conjuntamente para mantener la integridad de la información.

Las operaciones deben garantizar que:

* Las referencias entre entidades correspondan a registros existentes.
* Los productos utilizados en las operaciones sean válidos.
* Los movimientos de inventario tengan un tipo de movimiento válido.
* Los movimientos tengan una cantidad válida.
* Las operaciones de inventario mantengan coherencia con la existencia actual.
* Las operaciones transaccionales no dejen el sistema en un estado inconsistente.

Las reglas de negocio que no pueden garantizarse únicamente mediante restricciones de base de datos son validadas desde el backend.

---

# 7. Estrategia de inventario

La existencia actual de cada producto se almacena en la entidad `Inventory`.

La cantidad de inventario **no corresponde al campo `minimumStock` de `Product`**.

`minimumStock` representa un valor utilizado como referencia para el nivel mínimo de existencias y no la cantidad disponible actualmente.

La cantidad disponible se mantiene en:

```text
Inventory.quantity
```

Los movimientos registrados en `InventoryMovement` permiten mantener la trazabilidad de las entradas y salidas realizadas.

---

# 8. Movimientos de inventario

Los movimientos de inventario representan las operaciones que modifican las existencias.

Los tipos principales de movimiento son:

* `IN`: entrada de inventario.
* `OUT`: salida de inventario.

Los movimientos pueden estar relacionados con diferentes fuentes de operación mediante `SourceType`.

Entre los orígenes contemplados se encuentran:

* Compra.
* Venta.
* Producción.
* Ajuste.

Cada movimiento registra información relacionada con:

* Producto.
* Tipo de movimiento.
* Tipo de origen.
* Identificador de origen.
* Cantidad.
* Observaciones.
* Fechas de registro y actualización.

La cantidad registrada determina el cambio que debe aplicarse sobre el inventario del producto.

---

# 9. Estrategia de productos

La entidad `Product` contiene la información propia del producto.

Entre los datos administrados se encuentran:

* Categoría.
* Código interno.
* Código de barras.
* Unidad.
* Nombre.
* Precio de compra.
* Precio unitario.
* Precio mayorista.
* Cantidad mínima para precio mayorista.
* Stock mínimo.
* Estado de actividad.

El precio del producto constituye el precio base utilizado por el sistema.

En las ventas puede aplicarse un precio especial o personalizado específicamente para una compra determinada. Este precio aplicado a la venta no modifica permanentemente el precio base almacenado en el producto.

---

# 10. Estrategia de ventas

Las ventas almacenan la información correspondiente a la operación y sus productos asociados.

El detalle de venta permite conservar información como:

* Producto.
* Cantidad.
* Precio unitario aplicado.
* Subtotal.

Esto permite conservar el precio utilizado en una venta aunque posteriormente cambie el precio registrado para el producto.

Cuando se registra una venta, el inventario correspondiente se actualiza mediante un movimiento de salida (`OUT`).

---

# 11. Producción

La producción relaciona un producto utilizado como materia prima con un producto generado.

En el caso de Mantas Guajiras, una producción puede utilizar tela para generar mantas.

La operación contempla:

* Producto de tela utilizado.
* Cantidad de tela utilizada por unidad producida.
* Producto de manta generado.
* Cantidad de mantas producidas.

La cantidad total de tela consumida se obtiene a partir de la cantidad de tela utilizada por unidad y la cantidad de mantas producidas.

Al registrar una producción:

* Se genera una salida (`OUT`) del producto utilizado.
* Se genera una entrada (`IN`) del producto producido.
* Se actualiza el inventario de ambos productos.
* La operación se ejecuta dentro de una transacción para mantener la consistencia de los datos.

Las mantas se producen en cantidades enteras, mientras que el consumo de tela puede utilizar cantidades decimales expresadas en metros.

---

# 12. Auditoría y trazabilidad

La trazabilidad del inventario se mantiene mediante `InventoryMovement`.

Cada movimiento permite identificar:

* Qué producto fue afectado.
* Qué tipo de movimiento se realizó.
* Cuál fue el origen de la operación.
* Qué cantidad fue modificada.
* Observaciones asociadas a la operación.
* Información temporal del registro.

De esta forma, las operaciones de inventario no dependen únicamente del valor actual almacenado en `Inventory`.

---

# 13. Migraciones de base de datos

Los cambios estructurales de la base de datos se gestionan mediante Flyway.

Las migraciones permiten mantener un historial ordenado de modificaciones sobre el esquema de PostgreSQL.

Las nuevas modificaciones deben agregarse mediante una nueva migración, evitando alterar innecesariamente migraciones que ya forman parte del historial del proyecto.

---

# 14. Diagramas

Los diagramas entidad-relación (ERD) del sistema se almacenarán en:

```text
docs/
└── diagrams/
    └── erd/
```

Los diagramas deberán mantenerse actualizados cuando se produzcan cambios estructurales relevantes en el modelo de datos.

---

# 15. Observaciones

Este documento describe el modelo de base de datos de acuerdo con la arquitectura actual del sistema Mantas Guajiras.

El modelo utiliza una estructura generalizada de productos e inventario, evitando mantener estructuras independientes para mantas y telas.

La base de datos podrá ampliarse conforme se incorporen nuevas funcionalidades, manteniendo la integridad y compatibilidad con la información existente.

Los cambios futuros deberán reflejarse en las migraciones correspondientes y, cuando sean relevantes para la estructura del sistema, actualizar este documento.

---

# Anexo A. Notas de mantenimiento de migraciones

Las migraciones están en `backend/src/main/resources/db/migration` (V1 a V21). La carpeta `database/exports` solo guarda una exportación del esquema como referencia.

## Migraciones vacías históricas

`V6__insert_source_types.sql`, `V11__add_profile_pic.sql` y `V12__add_profile_pic.sql` están vacías. Quedaron de versiones anteriores y **no deben borrarse ni modificarse**: Flyway guarda en `flyway_schema_history` una huella (checksum) de cada versión aplicada, y si un archivo desaparece o cambia, la validación falla al arrancar. Toda modificación nueva del esquema va en una migración con un número mayor (V20, V21…).

## V20: ventas sin conexión

Agrega a `sale`:

- `client_request_id` (UUID, único): identificador que genera el dispositivo para cada venta. Si la misma venta llega dos veces, el backend devuelve la existente y no descuenta el inventario otra vez.
- `offline_created_at`: hora real de la venta hecha sin conexión. `created_at` sigue siendo la hora en que llegó al servidor. La pantalla de Ventas y el resumen del día usan la hora real.

## V21: compras, producción, personalizadas y pagos sin duplicados

Agrega `client_request_id` (UUID, único) a `purchase`, `production`, `orders` y `order_payment`, con el mismo mecanismo que las ventas: si la conexión se corta justo al guardar y se vuelve a enviar, el backend devuelve la operación ya registrada y no suma inventario, no descuenta tela ni cobra un pago dos veces.

## Tipo de movimiento `ADJUSTMENT`

`DataInitializer` crea el tipo de movimiento `ADJUSTMENT`, pero la lógica de inventario solo registra movimientos `IN` u `OUT`. Un ajuste se registra como `IN` u `OUT` por la diferencia entre el conteo físico y el inventario, y se identifica por su **tipo de origen** `ADJUSTMENT`. El registro `ADJUSTMENT` de `movement_type` se conserva para no alterar bases que ya están en uso; no se debe usar para nuevos movimientos.

## Verificación

- **Actualización con datos:** probada desde V19 (versión anterior) con datos reales de prueba hasta V21; datos idénticos e integridad correcta. Detalle en `docs/09-copias-de-seguridad.md` §4.2.
- La prueba `BackendApplicationTests` aplica las 21 migraciones sobre una base vacía y comprueba que el esquema coincide con las entidades (`ddl-auto: validate`).
- `scripts/backup/integrity-check.sql` comprueba que el inventario de cada producto sea igual a la suma de sus movimientos. Se ejecuta en cada prueba de restauración (ver `docs/09-copias-de-seguridad.md`).
