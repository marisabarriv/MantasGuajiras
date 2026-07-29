# Documento de Base de Datos

| Campo | Valor |
|--------|-------|
| **Proyecto** | Mantas Guajiras |
| **Documento** | Diseño de Base de Datos |
| **Código** | DOC-04 |
| **Versión** | v0.1.0 |
| **Estado** | En desarrollo |
| **Responsable** | Equipo de Desarrollo |
| **Última actualización** | 29/07/2026 |

---

## Control de versiones

| Versión | Fecha | Descripción | Responsable |
|----------|--------|-------------|-------------|
| v0.1.0 | 29/07/2026 | Creación inicial del documento. | Equipo de Desarrollo |

---

# 1. Introducción

Este documento describe el diseño lógico de la base de datos del sistema **Mantas Guajiras**.

El objetivo es almacenar de manera organizada toda la información relacionada con inventarios, ventas, producción, pedidos y usuarios, garantizando la integridad y consistencia de los datos.

La implementación física se realizará en PostgreSQL.

---

# 2. Objetivos

La base de datos deberá permitir:

- Gestionar usuarios y roles.
- Administrar clientes.
- Controlar inventarios de mantas y telas.
- Registrar ventas.
- Registrar producción.
- Gestionar pedidos personalizados.
- Registrar pagos y abonos.
- Mantener trazabilidad mediante movimientos de inventario.

---

# 3. Principios de diseño

El diseño de la base de datos seguirá los siguientes principios:

- Evitar duplicidad de información.
- Mantener la integridad referencial.
- Permitir crecimiento futuro del sistema.
- Registrar todas las operaciones importantes.
- Facilitar la generación de reportes.

---

# 4. Entidades principales

La primera versión del sistema estará compuesta por las siguientes entidades:

## Seguridad

- Usuario
- Rol

---

## Clientes

- Cliente

---

## Inventario

- Tipo de manta
- Inventario de mantas

- Tipo de tela
- Inventario de telas

---

## Producción

- Producción

---

## Ventas

- Venta
- Detalle de venta

---

## Pedidos

- Pedido
- Pago de pedido

---

## Auditoría

- Movimiento de inventario

---

# 5. Relaciones principales

El sistema deberá soportar las siguientes relaciones:

- Un rol puede tener muchos usuarios.
- Un cliente puede realizar muchas ventas.
- Un cliente puede tener muchos pedidos.
- Una venta puede contener varios productos.
- Un pedido puede registrar múltiples pagos.
- Un movimiento de inventario pertenece a un único producto.
- Una producción consume tela y genera mantas.

---

# 6. Integridad de los datos

La base de datos deberá garantizar que:

- No existan inventarios negativos.
- Toda venta tenga al menos un detalle.
- Todo pedido tenga un estado válido.
- Todo pago pertenezca a un pedido existente.
- Todo movimiento de inventario tenga una fecha y un usuario responsable.

---

# 7. Estrategia de inventario

El inventario no dependerá únicamente de los movimientos registrados.

Cada producto almacenará su existencia actual para facilitar consultas rápidas.

Los movimientos de inventario servirán como auditoría y trazabilidad de las operaciones realizadas.

---

# 8. Tipos de movimientos

Los movimientos de inventario podrán originarse por:

- Compra de inventario.
- Venta.
- Producción.
- Ajuste manual.
- Corrección administrativa.

Cada movimiento registrará:

- Fecha.
- Usuario.
- Tipo de movimiento.
- Producto.
- Cantidad.
- Observaciones.

---

# 9. Estrategia de ventas

Cada venta almacenará:

- Cliente.
- Usuario.
- Fecha.
- Valor total.
- Método de pago.

El detalle de venta almacenará:

- Producto.
- Cantidad.
- Precio unitario aplicado.
- Subtotal.

Esto permitirá conservar el precio histórico aunque el precio del producto cambie posteriormente.

---

# 10. Estrategia de pedidos

Cada pedido almacenará:

- Cliente.
- Precio acordado.
- Cantidad.
- Estado.
- Fecha de creación.
- Fecha estimada de entrega.
- Observaciones.

Los pagos asociados permitirán registrar:

- Abonos parciales.
- Pago total.
- Fecha del pago.
- Valor pagado.

El saldo pendiente será calculado automáticamente.

---

# 11. Producción

Cada registro de producción almacenará:

- Usuario responsable.
- Tipo de manta fabricada.
- Cantidad producida.
- Tipo de tela utilizada.
- Metros consumidos.
- Fecha.

Al registrarse una producción:

- Aumentará el inventario de mantas.
- Disminuirá el inventario de telas.
- Se crearán los movimientos correspondientes.

---

# 12. Sincronización futura

La estructura de la base de datos será compatible con un modelo de sincronización entre una base local y una base central.

Para ello, cada registro podrá incorporar en versiones futuras campos relacionados con:

- Fecha de creación.
- Fecha de modificación.
- Estado de sincronización.
- Identificadores globales.

Estos campos no serán implementados en la primera versión, pero el diseño permitirá incorporarlos sin afectar la estructura existente.

---

# 13. Diagramas

Los diagramas entidad-relación (ERD) del sistema se almacenarán en:

```
docs/
└── diagrams/
    └── erd/
```

---

# 14. Observaciones

Este documento define el diseño lógico de la base de datos y servirá como base para la elaboración del modelo entidad-relación y las migraciones de PostgreSQL.

El modelo podrá ampliarse en futuras versiones conforme evolucionen las necesidades del negocio, manteniendo la compatibilidad con los datos existentes.