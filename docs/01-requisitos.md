# Documento de Requisitos

| Campo | Valor |
|--------|-------|
| **Proyecto** | Mantas Guajiras |
| **Documento** | Requisitos del Sistema |
| **Código** | DOC-01 |
| **Versión** | v0.3.0 |
| **Estado** | En desarrollo |
| **Responsable** | Equipo de Desarrollo |
| **Última actualización** | 14/08/2026 |

---

## Control de versiones

| Versión | Fecha | Descripción | Responsable |
|----------|--------|-------------|-------------|
| v0.1.0 | 29/07/2026 | Creación inicial del documento. | Equipo de Desarrollo |

---

# 1. Introducción

Este documento define los requisitos funcionales y no funcionales del sistema **Mantas Guajiras**.

Su propósito es establecer el comportamiento esperado del sistema y servir como guía para el diseño, desarrollo y validación del software.

---

# 2. Requisitos funcionales

## RF-01. Autenticación de usuarios

El sistema deberá permitir el inicio de sesión mediante usuario y contraseña.

---

## RF-02. Gestión de usuarios

El administrador podrá:

- Crear usuarios.
- Modificar usuarios.
- Desactivar usuarios.
- Asignar roles.

---

## RF-03. Gestión de clientes

El sistema deberá permitir:

- Registrar clientes.
- Editar información de clientes.
- Consultar clientes.
- Buscar clientes por nombre o teléfono.

---

## RF-04. Gestión de mantas

El sistema deberá permitir:

- Registrar nuevos tipos de manta.
- Modificar su información.
- Consultar existencias.
- Registrar ajustes de inventario (solo administrador).

---

## RF-05. Gestión de telas

El sistema deberá permitir:

- Registrar nuevos tipos de tela.
- Editar su información.
- Consultar metros disponibles.
- Registrar ajustes de inventario (solo administrador).

---

## RF-06. Venta de mantas

El sistema deberá permitir:

- Registrar ventas de mantas.
- Registrar ventas al detal.
- Registrar ventas por docena con precio especial.
- Actualizar automáticamente el inventario.
- Registrar el valor total de la venta.

---

## RF-07. Venta de telas

El sistema deberá permitir:

- Registrar ventas de tela.
- Vender por cantidad de metros.
- Calcular automáticamente el valor de la venta.
- Descontar automáticamente los metros vendidos del inventario.

---

## RF-08. Producción de mantas

El sistema deberá permitir registrar la fabricación de mantas indicando:

- Tipo de manta.
- Cantidad producida.
- Tipo de tela utilizada.
- Cantidad de tela consumida.

Al confirmar la producción, el sistema deberá:

- Aumentar el inventario de mantas.
- Disminuir el inventario de tela.

---

## RF-09. Pedidos personalizados

El sistema deberá permitir:

- Registrar pedidos por encargo.
- Definir un precio personalizado.
- Registrar la cantidad solicitada.
- Registrar observaciones.
- Registrar la fecha estimada de entrega.

---

## RF-10. Gestión de pagos

Cada pedido podrá registrar:

- Pago completo.
- Uno o varios abonos.
- Saldo pendiente.

El sistema calculará automáticamente el saldo restante.

---

## RF-11. Inventario

El sistema deberá mantener actualizado el inventario de manera automática mediante los movimientos generados por:

- Ventas.
- Producción.
- Ajustes administrativos.

---

## RF-12. Reportes

El administrador podrá consultar:

- Inventario actual.
- Ventas por período.
- Producción realizada.
- Pedidos pendientes.
- Pedidos entregados.

---

## RF-13. Sincronización

En versiones futuras el sistema deberá sincronizar automáticamente la información entre diferentes dispositivos utilizando una base de datos centralizada.

La sincronización deberá permitir continuar trabajando cuando no exista conexión a Internet y actualizar la información una vez la conexión sea restablecida.

---

# 3. Requisitos no funcionales

## RNF-01. Usabilidad

La interfaz deberá ser sencilla, intuitiva y fácil de utilizar por personas con poca experiencia en herramientas tecnológicas.

---

## RNF-02. Rendimiento

Las operaciones comunes deberán ejecutarse en pocos segundos incluso con un volumen considerable de información.

---

## RNF-03. Disponibilidad

El sistema deberá continuar funcionando localmente cuando no exista conexión a Internet.

---

## RNF-04. Seguridad

Cada usuario solo podrá acceder a las funcionalidades correspondientes a su rol.

---

## RNF-05. Integridad

El sistema no permitirá generar movimientos que produzcan inventarios negativos.

---

## RNF-06. Escalabilidad

La arquitectura deberá permitir agregar nuevos módulos sin modificar significativamente los existentes.

---

## RNF-07. Mantenibilidad

El código deberá organizarse siguiendo principios de arquitectura limpia y buenas prácticas de desarrollo.

---

## RNF-08. Portabilidad

El sistema deberá poder ejecutarse en computadores con Windows y, en futuras versiones, permitir el acceso desde dispositivos móviles mediante una interfaz web.

---

# 4. Restricciones

El proyecto utilizará las siguientes tecnologías:

- React
- TypeScript
- Vite
- Java 21
- Spring Boot
- PostgreSQL
- Git
- GitHub

---

# 5. Criterios de aceptación

El sistema será considerado funcional cuando:

- El inventario refleje correctamente las existencias reales.
- Todas las ventas actualicen automáticamente el inventario.
- La producción actualice correctamente mantas y telas.
- Los pedidos administren correctamente sus pagos.
- Los usuarios puedan utilizar el sistema sin necesidad de conocimientos técnicos avanzados.
- El sistema pueda crecer hacia una solución sincronizada en la nube sin requerir un rediseño completo.

---

# 6. Observaciones

Los requisitos definidos en este documento constituyen la base funcional del proyecto y podrán ampliarse conforme evolucionen las necesidades del negocio, manteniendo siempre la compatibilidad con las versiones anteriores del sistema.