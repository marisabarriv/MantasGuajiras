# Documento de Modelo de Datos

| Campo                    | Valor                |
| ------------------------ | -------------------- |
| **Proyecto**             | Mantas Guajiras      |
| **Documento**            | Modelo de Datos      |
| **Código**               | DOC-07               |
| **Versión**              | v0.4.0               |
| **Estado**               | En desarrollo        |
| **Responsable**          | Equipo de Desarrollo |
| **Última actualización** | 05/09/2026           |

---

## Control de versiones

| Versión | Fecha      | Descripción                                                       | Responsable          |
| ------- | ---------- | ----------------------------------------------------------------- | -------------------- |
| v0.2.0  | 29/07/2026 | Creación inicial del modelo de datos.                             | Equipo de Desarrollo |
| v0.4.0  | 05/09/2026 | Actualización del modelo de datos según la implementación actual. | Equipo de Desarrollo |

---

# 1. Introducción

Este documento define el modelo lógico de datos utilizado por el sistema **Mantas Guajiras**.

Su propósito es describir las principales entidades, relaciones y reglas utilizadas para representar la información del sistema.

El modelo sirve como referencia para:

* La estructura de la base de datos PostgreSQL.
* Las entidades JPA del backend.
* Las relaciones entre los diferentes módulos.
* Las migraciones de Flyway.
* El desarrollo de las funcionalidades del sistema.

---

# 2. Principios de diseño

El modelo de datos sigue los siguientes principios:

* Evitar duplicidad innecesaria de información.
* Mantener la integridad referencial.
* Separar la información del producto de su inventario.
* Mantener trazabilidad de los movimientos de inventario.
* Reutilizar entidades generales cuando diferentes tipos de productos comparten características.
* Mantener consistencia entre las operaciones del backend y los datos almacenados.
* Permitir ampliaciones futuras sin modificar innecesariamente las estructuras existentes.

---

# 3. Catálogos principales

## Rol

Define los roles disponibles para los usuarios del sistema.

Actualmente se contemplan principalmente:

* Administrador.
* Vendedor.

---

## Categoría de producto

Clasifica los productos del sistema.

Las categorías principales utilizadas actualmente son:

* Manta.
* Tela.

---

## Unidad

Define la unidad utilizada para manejar las cantidades de cada producto.

La unidad se determina según el tipo de producto:

* Manta: unidades enteras (#).
* Tela: metros.

---

## Tipo de movimiento

Define si un movimiento representa una entrada o una salida de inventario.

Los valores utilizados son:

* `IN`
* `OUT`

---

## Tipo de origen

Permite identificar el origen de un movimiento de inventario.

Entre los orígenes contemplados se encuentran:

* Compra.
* Venta.
* Producción.
* Ajuste.

---

# 4. Entidades principales

El modelo actual se organiza principalmente en los siguientes grupos.

## Seguridad

* User
* Role

---

## Productos

* Product
* ProductCategory
* Unit

`Product` contiene la información general del producto.

`ProductCategory` permite clasificarlo.

`Unit` define la unidad utilizada para manejar sus cantidades.

---

## Inventario

* Inventory
* InventoryMovement
* MovementType
* SourceType

`Inventory` almacena la existencia actual de cada producto.

`InventoryMovement` registra las operaciones que modifican dicha existencia.

---

## Ventas

El módulo de ventas utiliza entidades para representar:

* Venta.
* Detalle de venta.

El detalle permite almacenar el producto, la cantidad y el precio aplicado durante la operación.

---

## Producción

El módulo de producción registra las operaciones en las que se consume un producto, como tela, para generar otro producto, como una manta.

---

# 5. Relaciones principales

Las relaciones principales del modelo incluyen:

* `Role` → `User` (1:N)
* `ProductCategory` → `Product` (1:N)
* `Unit` → `Product` (1:N)
* `Product` → `Inventory` (1:1)
* `Product` → `InventoryMovement` (1:N)

En las operaciones comerciales:

* Una venta puede contener múltiples detalles.
* Cada detalle de venta corresponde a un producto.

En producción:

* Una operación puede utilizar un producto como materia prima.
* Una operación puede generar otro producto.
* La producción genera los movimientos de inventario correspondientes.

---

# 6. Producto e inventario

El modelo separa la información descriptiva del producto de su existencia.

`Product` contiene información como:

* Nombre.
* Código interno.
* Código de barras.
* Categoría.
* Unidad.
* Precio.
* Stock mínimo.
* Estado.

`Inventory` contiene la cantidad disponible actualmente.

Por lo tanto:

```text id="0g0gqa"
Product
   │
   │ 1:1
   ▼
Inventory
   │
   │ cantidad actual
   ▼
InventoryMovement
```

El campo `minimumStock` del producto no representa la existencia actual.

La existencia actual se mantiene mediante `Inventory.quantity`.

---

# 7. Precios

El producto mantiene su información de precios base.

El modelo contempla información como:

* Precio de compra.
* Precio unitario.
* Precio mayorista.
* Cantidad mínima para precio mayorista.

En una venta puede establecerse un precio especial o personalizado para un producto determinado.

Este precio se aplica únicamente a la operación de venta correspondiente y no modifica el precio base almacenado en `Product`.

El detalle de venta conserva el precio aplicado durante la operación para mantener el valor histórico de la venta.

---

# 8. Inventario y movimientos

Los cambios de inventario se registran mediante `InventoryMovement`.

Cada movimiento se relaciona con:

* Producto.
* Tipo de movimiento.
* Tipo de origen.
* Identificador del origen.
* Cantidad.
* Observaciones.
* Información temporal del registro.

Los movimientos principales son:

```text id="q1qg2w"
IN  → Entrada
OUT → Salida
```

Las operaciones que pueden generar movimientos incluyen:

* Compras.
* Ventas.
* Producción.
* Ajustes de inventario.

El registro de movimientos permite mantener la trazabilidad de las operaciones que afectan las existencias.

---

# 9. Reglas generales

El modelo debe mantener las siguientes reglas:

* Cada producto tiene un registro de inventario asociado.
* La existencia actual se almacena en `Inventory.quantity`.
* `minimumStock` representa el nivel mínimo de referencia y no la existencia actual.
* Los movimientos deben estar asociados a un producto existente.
* Los movimientos deben utilizar un tipo de movimiento válido.
* Las cantidades de inventario deben mantenerse consistentes con las operaciones realizadas.
* Las mantas se manejan mediante cantidades enteras.
* Las telas pueden manejar cantidades decimales expresadas en metros.
* Los precios aplicados en las ventas deben conservarse en sus respectivos detalles.

---

# 10. Producción

La producción utiliza productos existentes dentro del modelo general.

El flujo de datos puede representarse de la siguiente manera:

```text id="q7zj4f"
Producto de entrada
       │
       │ OUT
       ▼
   Producción
       │
       │ IN
       ▼
Producto generado
```

Cuando se registra una producción:

* Se registra el consumo del producto utilizado.
* Se registra la generación del producto producido.
* Se actualizan los inventarios correspondientes.
* Se crean los movimientos de inventario asociados.

Las operaciones de producción se ejecutan de manera transaccional.

---

# 11. Convenciones

## Claves primarias

Las entidades utilizan identificadores UUID.

---

## Fechas

Las entidades incluyen información temporal para controlar la creación y actualización de los registros cuando corresponde.

---

## Nombres

Las entidades y atributos del backend siguen las convenciones utilizadas por Java y JPA.

Las tablas y columnas de PostgreSQL siguen las convenciones establecidas por el esquema de la base de datos y sus migraciones.

---

## Restricciones

La integridad de los datos se mantiene mediante una combinación de:

* Claves primarias.
* Claves foráneas.
* Restricciones de unicidad.
* Valores no nulos cuando corresponde.
* Validaciones del backend.
* Reglas de negocio.

---

# 12. Migraciones

La estructura de la base de datos se mantiene mediante Flyway.

Cada cambio estructural importante debe registrarse mediante una nueva migración.

Las migraciones permiten conservar el historial de cambios realizados sobre el modelo de datos.

No se deben modificar innecesariamente migraciones que ya forman parte del historial del proyecto.

---

# 13. Diagramas

Los diagramas entidad-relación (ERD) del sistema se almacenarán en:

```text id="c7j8is"
docs/
└── diagrams/
    └── erd/
```

Los diagramas deberán actualizarse cuando se produzcan cambios estructurales relevantes en el modelo.

---

# 14. Mejoras futuras

El modelo podrá ampliarse conforme evolucionen las necesidades del sistema.

Entre las posibles mejoras se encuentran nuevas funcionalidades relacionadas con producción, reportes, facturación e integraciones.

Estas mejoras se incorporarán cuando formen parte del alcance real del proyecto.

---

# 15. Observaciones

Este documento describe el modelo de datos de acuerdo con la implementación actual del sistema Mantas Guajiras.

El modelo utiliza entidades generales para productos, categorías, unidades e inventario, permitiendo manejar tanto mantas como telas sin crear estructuras de inventario independientes.

Cualquier modificación estructural relevante deberá reflejarse en las migraciones correspondientes y actualizar este documento cuando sea necesario.
