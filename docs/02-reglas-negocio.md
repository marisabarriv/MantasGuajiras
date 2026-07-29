# Documento de Reglas de Negocio

| Campo | Valor |
|--------|-------|
| **Proyecto** | Mantas Guajiras |
| **Documento** | Reglas de Negocio |
| **Código** | DOC-02 |
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

Este documento define las reglas de negocio que gobiernan el funcionamiento del sistema Mantas Guajiras.

Las reglas aquí descritas representan el comportamiento real del negocio y deberán cumplirse independientemente de la implementación técnica utilizada.

---

# 2. Inventario

## RN-01. Inventario de mantas

Cada tipo de manta tendrá una cantidad disponible dentro del inventario.

---

## RN-02. Inventario de telas

Cada tipo de tela almacenará la cantidad disponible expresada en metros.

---

## RN-03. Inventario no negativo

El sistema no permitirá que la existencia de mantas o telas sea inferior a cero.

---

## RN-04. Ajustes de inventario

Los ajustes manuales de inventario únicamente podrán ser realizados por un usuario con rol de Administrador.

Todo ajuste deberá generar un movimiento de inventario.

---

# 3. Producción

## RN-05. Fabricación de mantas

La fabricación de mantas incrementará automáticamente el inventario del tipo de manta producido.

---

## RN-06. Consumo de tela

Toda fabricación de mantas disminuirá automáticamente la cantidad de tela utilizada.

---

## RN-07. Registro de producción

Cada proceso de producción deberá registrar como mínimo:

- Fecha.
- Usuario responsable.
- Tipo de manta.
- Cantidad fabricada.
- Tipo de tela utilizada.
- Cantidad de tela consumida.

---

# 4. Ventas

## RN-08. Venta de mantas

Toda venta disminuirá automáticamente la cantidad correspondiente del inventario.

---

## RN-09. Venta de telas

Las telas se venderán únicamente por metros.

Cada venta disminuirá automáticamente la cantidad de metros vendidos.

---

## RN-10. Precio por docena

Las mantas podrán tener un precio especial para ventas por docena.

Cuando una venta cumpla esta condición, el sistema utilizará automáticamente el precio correspondiente.

---

## RN-11. Registro de ventas

Toda venta deberá registrar:

- Fecha.
- Usuario.
- Cliente (cuando aplique).
- Productos vendidos.
- Cantidades.
- Valor total.
- Método de pago.

---

# 5. Pedidos

## RN-12. Pedidos personalizados

El sistema permitirá registrar pedidos personalizados con un precio definido manualmente.

---

## RN-13. Estado del pedido

Todo pedido deberá encontrarse en uno de los siguientes estados:

- Pendiente.
- En producción.
- Listo para entregar.
- Entregado.
- Cancelado.

---

## RN-14. Pagos

Cada pedido podrá registrar uno o varios pagos.

Los pagos podrán realizarse como:

- Abono.
- Pago total.

---

## RN-15. Saldo pendiente

El saldo pendiente será calculado automáticamente como:

Saldo pendiente = Valor total del pedido − Total pagado.

---

## RN-16. Entrega

Un pedido únicamente podrá marcarse como entregado cuando el saldo pendiente sea igual a cero.

---

# 6. Usuarios

## RN-17. Roles

El sistema contará inicialmente con dos roles:

- Administrador.
- Vendedor.

---

## RN-18. Permisos del Administrador

El Administrador tendrá acceso completo al sistema.

---

## RN-19. Permisos del Vendedor

El Vendedor podrá:

- Registrar ventas.
- Registrar producción.
- Gestionar pedidos.
- Consultar inventarios.

No podrá administrar usuarios ni realizar ajustes manuales de inventario.

---

# 7. Sincronización

## RN-20. Funcionamiento sin conexión

El sistema deberá continuar operando cuando no exista conexión con Internet.

---

## RN-21. Sincronización

Cuando la conexión sea restablecida, el sistema sincronizará automáticamente la información con la base de datos central.

---

## RN-22. Conflictos

En caso de existir conflictos durante la sincronización, prevalecerá la información almacenada en la base de datos central, aplicando las reglas definidas para la resolución de conflictos.

---

# 8. Auditoría

## RN-23. Movimientos

Todo cambio que afecte el inventario deberá generar un movimiento de inventario.

Los movimientos nunca serán eliminados.

---

## RN-24. Registro de operaciones

Las operaciones importantes deberán registrar:

- Usuario.
- Fecha.
- Hora.
- Tipo de operación.

---

# 9. Principios generales

## RN-25. Consistencia

Toda operación deberá mantener la consistencia de la información del sistema.

---

## RN-26. Integridad

No se permitirá registrar operaciones que produzcan datos inconsistentes.

---

## RN-27. Escalabilidad

Las reglas de negocio deberán mantenerse independientes de la tecnología utilizada para permitir futuras ampliaciones del sistema.

---

# 10. Observaciones

Las reglas descritas en este documento constituyen la lógica del negocio y servirán como referencia para el desarrollo del backend, el diseño de la base de datos y la implementación de las pruebas del sistema.