# Documento de Reglas de Negocio

| Campo                    | Valor                |
| ------------------------ | -------------------- |
| **Proyecto**             | Mantas Guajiras      |
| **Documento**            | Reglas de Negocio    |
| **Código**               | DOC-02               |
| **Versión**              | v0.4.0               |
| **Estado**               | En desarrollo        |
| **Responsable**          | Equipo de Desarrollo |
| **Última actualización** | 05/09/2026           |

---

## Control de versiones

| Versión | Fecha      | Descripción                              | Responsable          |
| ------- | ---------- | ---------------------------------------- | -------------------- |
| v0.1.0  | 29/07/2026 | Creación inicial del documento.          | Equipo de Desarrollo |
| v0.4.0  | 05/09/2026 | Actualización de las reglas funcionales. | Equipo de Desarrollo |

---

# 1. Introducción

Este documento define las reglas de negocio que determinan el comportamiento del sistema Mantas Guajiras.

Las reglas deberán cumplirse independientemente de la interfaz utilizada por el usuario.

---

# 2. Productos

## RN-01. Categorías

Los productos pertenecen a una categoría.

Las categorías utilizadas actualmente contemplan principalmente:

* Manta.
* Tela.

---

## RN-02. Unidad de producto

La unidad utilizada depende del tipo de producto.

| Producto  | Unidad              |
| --------- | ------------------- |
| **Manta** | Unidad entera (`#`) |
| **Tela**  | Metro               |

---

## RN-03. Cantidad de mantas

Las mantas se manejan mediante cantidades enteras.

No se deberán registrar fracciones de manta en operaciones que requieran unidades completas.

---

## RN-04. Cantidad de tela

Las telas se manejan mediante metros.

La cantidad podrá contener valores decimales.

---

# 3. Precios

## RN-05. Precio base

Cada producto tendrá un precio base asociado.

El precio de compra y el precio unitario se consideran actualmente como el mismo concepto dentro del modelo funcional del sistema.

---

## RN-06. Precio especial en una venta

El precio de un producto podrá modificarse manualmente para una venta específica.

El precio especial:

* Solo afectará esa venta.
* Podrá aplicarse incluso a una unidad.
* No dependerá exclusivamente de la cantidad.
* No modificará permanentemente el precio base del producto.

---

## RN-07. Descuento

Cuando se establezca un precio especial inferior al precio original, el sistema podrá calcular el descuento correspondiente tomando como referencia el precio base.

---

# 4. Inventario

## RN-08. Stock actual

El stock actual representa la cantidad realmente disponible de un producto.

El stock actual pertenece al registro de inventario.

---

## RN-09. Stock mínimo

El stock mínimo representa un umbral de referencia para identificar necesidades de reposición.

El stock mínimo no representa la cantidad actualmente disponible.

---

## RN-10. Inventario no negativo

El sistema no permitirá que una operación produzca un stock inferior a cero.

---

## RN-11. Movimientos

Toda operación que modifique el stock deberá generar el movimiento de inventario correspondiente.

---

## RN-12. Ajustes administrativos

Los ajustes manuales de inventario estarán destinados a usuarios con permisos administrativos.

Los ajustes deberán quedar registrados como movimientos de inventario.

---

# 5. Compras

## RN-13. Entrada por compra

Una compra registrada correctamente deberá aumentar el inventario del producto correspondiente.

---

# 6. Ventas

## RN-14. Salida por venta

Una venta registrada correctamente deberá disminuir el inventario de los productos vendidos.

---

## RN-15. Cantidad vendida

La cantidad vendida deberá respetar la unidad asociada al producto.

Las telas podrán venderse en cantidades decimales.

Las mantas deberán venderse en unidades enteras.

---

## RN-16. Precio histórico

El precio aplicado durante una venta deberá conservarse en el detalle de la venta.

Los cambios posteriores al precio base del producto no deberán modificar el valor histórico de una venta ya registrada.

---

# 7. Producción

## RN-17. Consumo de tela

La producción consume tela del inventario.

---

## RN-18. Generación de mantas

La producción genera mantas y aumenta su inventario.

---

## RN-19. Cantidad de producción

La cantidad producida de mantas deberá ser un número entero.

---

## RN-20. Cantidad de tela

La cantidad de tela consumida podrá utilizar valores decimales.

---

## RN-21. Operación transaccional

La disminución de tela y el aumento de mantas correspondientes a una producción deberán ejecutarse como una única operación transaccional.

Si la operación falla, no deberá quedar solamente una de las dos modificaciones aplicada.

---

# 8. Usuarios

## RN-22. Roles

El sistema contempla inicialmente:

* Administrador.
* Vendedor.

---

## RN-23. Administrador

El administrador podrá ejecutar las operaciones correspondientes a sus permisos, incluyendo las operaciones administrativas sobre inventario.

---

## RN-24. Vendedor

El vendedor podrá ejecutar las operaciones comerciales que tenga autorizadas.

No deberá tener permisos para realizar ajustes administrativos de inventario ni administrar usuarios si dichas operaciones están restringidas al administrador.

---

# 9. Auditoría

## RN-25. Trazabilidad de inventario

Los movimientos de inventario deberán conservar la información necesaria para identificar:

* Producto.
* Tipo de movimiento.
* Origen de la operación.
* Cantidad.
* Observaciones cuando correspondan.
* Fecha de la operación.

---

## RN-26. Movimientos no eliminables

Los movimientos de inventario representan trazabilidad histórica y no deberán eliminarse como mecanismo para corregir una operación.

Las correcciones deberán realizarse mediante los mecanismos de ajuste correspondientes.

---

# 10. Pedidos y pagos

Las reglas específicas de pedidos y pagos deberán mantenerse alineadas con el modelo y las funcionalidades realmente implementadas en el backend.

No se deberán considerar como implementadas reglas adicionales que todavía no hayan sido desarrolladas y verificadas.

---

# 11. Sincronización

## RN-27. Estado actual

El sistema actualmente no implementa sincronización offline con una base de datos central.

---

## RN-28. Evolución futura

La sincronización entre dispositivos y el funcionamiento offline podrán implementarse posteriormente como una evolución de la arquitectura.

---

# 12. Principios generales

## RN-29. Consistencia

Toda operación deberá mantener consistencia entre los datos registrados y el estado real del negocio.

---

## RN-30. Integridad

No deberán permitirse operaciones que produzcan información inválida o inconsistencias entre entidades relacionadas.

---

## RN-31. Backend como responsable de las reglas

Las reglas de negocio deberán validarse en el backend.

Las validaciones del frontend sirven como apoyo a la experiencia de usuario, pero no sustituyen las validaciones del servidor.

---

# 13. Observaciones

Las reglas descritas en este documento constituyen la referencia funcional para el desarrollo del backend, frontend y base de datos.

Cuando una regla cambie, deberá actualizarse la documentación correspondiente antes o junto con la implementación.
