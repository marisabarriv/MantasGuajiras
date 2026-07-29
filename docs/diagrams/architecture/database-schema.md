# Database Schema

**Proyecto:** Mantas Guajiras  
**Versión:** v1.0  
**Última actualización:** 2026-07-29

---

# 1. Objetivo

Este documento describe la estructura lógica de la base de datos del sistema **Mantas Guajiras**, así como las decisiones de diseño adoptadas durante la fase de análisis.

El objetivo principal del modelo es soportar la gestión de:

- Inventario.
- Producción.
- Compras.
- Ventas.
- Pedidos personalizados.
- Usuarios del sistema.

El modelo fue diseñado para PostgreSQL y será implementado mediante migraciones con Flyway.

---

# 2. Principios de diseño

Durante el diseño del modelo se siguieron los siguientes principios:

- Mantener la base de datos normalizada para evitar duplicidad de información.
- Separar los catálogos de las tablas transaccionales.
- Registrar el historial completo de los movimientos de inventario.
- Facilitar la escalabilidad del sistema sin afectar la estructura existente.
- Mantener una nomenclatura consistente en tablas, columnas y relaciones.

---

# 3. Organización del modelo

La base de datos está dividida en los siguientes módulos.

## Catálogos

Contienen información relativamente estática utilizada por el resto del sistema.

Tablas:

- product_category
- unit
- movement_type
- production_operation
- source_type
- order_status
- role

---

## Productos

Representa todos los productos administrados por el sistema.

Incluye tanto:

- materias primas (telas),
- productos terminados (mantas).

Tabla:

- product

Cada producto define:

- categoría,
- unidad de medida,
- precio de compra,
- precio de venta,
- precio por mayor,
- stock mínimo,
- si puede comprarse,
- si puede fabricarse.

---

## Inventario

Este módulo mantiene el estado actual del inventario y el historial completo de sus movimientos.

Tablas:

- inventory
- inventory_movement

El inventario representa únicamente la cantidad actual disponible.

Todos los cambios quedan registrados en la tabla de movimientos.

---

## Producción

Registra cada proceso de fabricación realizado por el negocio.

Tablas:

- production
- production_item

Cada producción puede consumir materias primas y generar productos terminados.

La operación realizada sobre cada producto se identifica mediante el catálogo:

- production_operation

---

## Compras

Permite registrar las compras de materias primas para aumentar el inventario.

Tablas:

- purchase
- purchase_item

---

## Clientes

Almacena la información básica de los clientes.

Tabla:

- customer

---

## Ventas

Representa las ventas realizadas directamente al cliente.

Tablas:

- sale
- sale_item

---

## Pedidos personalizados

Gestiona los pedidos realizados por encargo.

Tablas:

- custom_order
- custom_order_item
- custom_order_payment

Este módulo permite registrar múltiples abonos antes de entregar el pedido.

---

## Seguridad

Gestiona los usuarios autorizados para utilizar el sistema.

Tabla:

- app_user

Cada usuario pertenece a un rol definido en el catálogo correspondiente.

---

# 4. Flujo del inventario

Toda modificación del inventario genera un registro en la tabla:

inventory_movement

El origen del movimiento se identifica mediante:

- movement_type
- source_type

Esto permite conocer exactamente:

- qué ocurrió,
- cuándo ocurrió,
- qué producto fue afectado,
- cuál fue el proceso responsable del movimiento.

---

# 5. Flujo operativo

El comportamiento general del sistema puede resumirse de la siguiente manera:

Compra

↓

Inventario

↓

Producción

↓

Inventario

↓

Venta

o

Pedido personalizado

---

# 6. Convenciones

Durante el desarrollo se utilizarán las siguientes convenciones.

## Tablas

- Singular.
- Nombre en inglés.
- snake_case.

Ejemplo:

product

sale_item

custom_order

---

## Claves primarias

- UUID para tablas transaccionales.
- SMALLINT para catálogos.

---

## Claves foráneas

Todas las relaciones se implementarán mediante claves foráneas explícitas.

---

## Fechas

Las tablas transaccionales almacenan:

- created_at

Las tablas con información modificable también almacenan:

- updated_at

---

# 7. Decisiones de diseño

Durante el análisis se tomaron las siguientes decisiones.

## Inventario separado

La cantidad disponible de un producto se almacena únicamente en la tabla:

inventory

El historial queda registrado en:

inventory_movement

---

## Sin subtotales

Las tablas de detalle no almacenan subtotales.

Estos valores se calculan a partir de:

cantidad × precio

De esta forma se evita información redundante.

---

## Compras sin proveedores

El sistema registra únicamente las compras realizadas.

No se implementó una entidad de proveedores debido a que actualmente no forma parte de las necesidades operativas del negocio.

---

## Producción explícita

Cada producto involucrado en una producción indica si corresponde a una entrada o salida de inventario mediante:

production_operation

Esto simplifica la lógica del backend.

---

# 8. Mejoras futuras

Las siguientes funcionalidades quedan previstas para versiones posteriores.

## Bill of Materials (BOM)

Permitirá asociar automáticamente el consumo de tela a cada tipo de manta.

Con esta funcionalidad el sistema podrá descontar automáticamente las materias primas durante una producción.

---

## Sincronización en la nube

La primera versión del sistema funcionará de manera local.

En una versión futura se implementará un mecanismo de sincronización para permitir el acceso desde múltiples dispositivos.

---

# 9. Documentos relacionados

- ERD (`docs/diagrams/erd_dbml/mantas-guajiras.dbml`)
- Exportaciones del diagrama (`docs/diagrams/exports/`)
- README del proyecto