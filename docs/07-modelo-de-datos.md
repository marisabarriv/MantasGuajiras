# Documento de Modelo de Datos

| Campo | Valor |
|--------|-------|
| **Proyecto** | Mantas Guajiras |
| **Documento** | Modelo de Datos |
| **Código** | DOC-07 |
| **Versión** | v0.2.0 |
| **Estado** | En desarrollo |
| **Responsable** | Equipo de Desarrollo |
| **Última actualización** | 29/07/2026 |

---

## Control de versiones

| Versión | Fecha | Descripción | Responsable |
|----------|--------|-------------|-------------|
| v0.2.0 | 29/07/2026 | Creación inicial del modelo de datos. | Equipo de Desarrollo |

---

# 1. Introducción

Este documento define el modelo lógico de datos del sistema **Mantas Guajiras**.

Su propósito es establecer las entidades, atributos, relaciones y restricciones que conformarán la base de datos del sistema.

Este documento servirá como referencia para:

- Elaboración del diagrama entidad-relación (ERD).
- Implementación de PostgreSQL.
- Desarrollo de las entidades JPA del backend.
- Creación de migraciones.

---

# 2. Principios de diseño

El modelo de datos se basa en los siguientes principios:

- Evitar duplicidad de información.
- Mantener la integridad referencial.
- Facilitar futuras ampliaciones.
- Permitir auditoría completa.
- Mantener consistencia en los datos.
- Centralizar la información mediante entidades reutilizables.

---

# 3. Catálogos

Las siguientes entidades funcionarán como catálogos del sistema.

## Rol

Define los permisos disponibles.

Valores iniciales:

- Administrador
- Vendedor

---

## CategoriaProducto

Clasifica los productos vendidos por el negocio.

Valores iniciales:

- Manta
- Tela

---

## UnidadMedida

Define la unidad utilizada por un producto.

Valores iniciales:

- Unidad
- Metro

---

## MetodoPago

Representa los métodos de pago disponibles.

Valores iniciales:

- Efectivo
- Transferencia
- Nequi
- Daviplata

---

## EstadoPedido

Representa el estado de un pedido.

Valores iniciales:

- Pendiente
- En producción
- Listo para entregar
- Entregado
- Cancelado

---

## TipoMovimiento

Representa el motivo de un movimiento de inventario.

Valores iniciales:

- Venta
- Producción
- Ajuste
- Compra
- Corrección

---

# 4. Entidades principales

El sistema estará conformado por las siguientes entidades.

## Seguridad

- Rol
- Usuario

## Comercial

- Cliente
- Producto

## Inventario

- Inventario
- MovimientoInventario

## Producción

- Produccion

## Ventas

- Venta
- DetalleVenta

## Pedidos

- Pedido
- Pago

---

# 5. Relaciones

Las relaciones principales serán:

- Rol → Usuario (1:N)
- CategoriaProducto → Producto (1:N)
- UnidadMedida → Producto (1:N)
- Producto → Inventario (1:1)
- Producto → MovimientoInventario (1:N)
- Cliente → Venta (1:N)
- Venta → DetalleVenta (1:N)
- Producto → DetalleVenta (1:N)
- Cliente → Pedido (1:N)
- Pedido → Pago (1:N)
- Usuario → Venta (1:N)
- Usuario → Produccion (1:N)

---

# 6. Reglas generales

- Cada producto tendrá un único registro de inventario.
- Todo movimiento de inventario deberá quedar registrado.
- El inventario nunca podrá ser negativo.
- Toda venta deberá tener al menos un detalle.
- Los pedidos podrán registrar múltiples pagos.
- Los precios históricos nunca serán modificados.

---

# 7. Auditoría

Las operaciones que afecten inventario generarán automáticamente un registro en MovimientoInventario.

Cada movimiento almacenará:

- Usuario responsable.
- Fecha.
- Tipo de movimiento.
- Cantidad anterior.
- Cantidad modificada.
- Cantidad resultante.

---

# 8. Convenciones

Se adoptarán las siguientes convenciones para la base de datos.

## Claves primarias

Todas las tablas utilizarán UUID como clave primaria.

---

## Fechas

Todas las fechas se almacenarán utilizando TIMESTAMP.

---

## Nombres

Las tablas utilizarán nombres en singular.

Ejemplo:

- producto
- venta
- pedido

Los campos utilizarán snake_case.

Ejemplo:

- created_at
- updated_at
- categoria_producto_id

---

## Restricciones

Siempre que sea posible se utilizarán:

- PRIMARY KEY
- FOREIGN KEY
- UNIQUE
- NOT NULL
- CHECK

para garantizar la integridad de la información.

---

# 9. Próximos pasos

Una vez aprobado este documento se procederá a:

1. Diseñar el diagrama entidad-relación (ERD).
2. Crear las migraciones de PostgreSQL.
3. Implementar las entidades JPA.
4. Construir los repositorios del backend.

---

# 10. Observaciones

El presente documento constituye la especificación oficial del modelo de datos del proyecto Mantas Guajiras.

Toda modificación estructural deberá reflejarse primero en este documento antes de implementarse en la base de datos.