# Documento de Visión

| Campo                    | Valor                |
| ------------------------ | -------------------- |
| **Proyecto**             | Mantas Guajiras      |
| **Documento**            | Visión del Proyecto  |
| **Código**               | DOC-00               |
| **Versión**              | v0.4.0               |
| **Estado**               | En desarrollo        |
| **Responsable**          | Equipo de Desarrollo |
| **Última actualización** | 05/09/2026           |

---

## Control de versiones

| Versión | Fecha      | Descripción                              | Responsable          |
| ------- | ---------- | ---------------------------------------- | -------------------- |
| v0.1.0  | 29/07/2026 | Creación inicial del documento.          | Equipo de Desarrollo |
| v0.4.0  | 05/09/2026 | Actualización de la visión del proyecto. | Equipo de Desarrollo |

---

# 1. Introducción

Mantas Guajiras es un sistema de gestión desarrollado para apoyar las operaciones administrativas y operativas del negocio mediante la digitalización de sus procesos.

El sistema busca centralizar la información relacionada con productos, inventario, ventas, producción, usuarios y demás procesos que formen parte de la operación del negocio.

Desde su diseño, el proyecto prioriza la facilidad de uso, la claridad de la información y la posibilidad de utilizar el sistema desde diferentes dispositivos.

La aplicación será una **aplicación web responsive**, por lo que la misma interfaz deberá adaptarse a:

* Computadores de escritorio.
* Computadores portátiles.
* Tablets.
* Teléfonos celulares.

No se contempla una aplicación móvil nativa independiente como parte de la arquitectura actual.

---

# 2. Problema

El negocio requiere una herramienta que permita centralizar y controlar la información de sus operaciones.

Entre las principales necesidades se encuentran:

* Mantener control sobre las existencias reales.
* Registrar entradas y salidas de inventario.
* Controlar el consumo de tela durante la producción.
* Registrar las ventas realizadas.
* Mantener un historial organizado de las operaciones.
* Reducir errores derivados de registros manuales.
* Facilitar el acceso a la información desde diferentes dispositivos.

---

# 3. Solución propuesta

Mantas Guajiras centralizará la información del negocio mediante una aplicación web conectada a un backend y una base de datos PostgreSQL.

El sistema permitirá administrar productos y sus categorías, controlar inventarios mediante movimientos, registrar ventas, gestionar producción y proporcionar las funcionalidades administrativas necesarias para la operación.

La arquitectura contempla:

Computador ──┐
             │
Laptop ──────┤
             │
Tablet ──────┤──► Aplicación Web Responsive
             │          │
Celular ─────┘          ▼
                     REST API
                         │
                         ▼
                    Spring Boot
                         │
                         ▼
                     PostgreSQL

La posibilidad de sincronización en la nube y funcionamiento offline corresponde a una evolución futura del proyecto y no a una característica actualmente implementada.

---

# 4. Objetivos

## 4.1 Objetivo general

Desarrollar un sistema de gestión que permita administrar de manera organizada y confiable las operaciones de Mantas Guajiras.

## 4.2 Objetivos específicos

* Mantener información actualizada de los productos.
* Controlar el inventario real.
* Registrar movimientos de inventario.
* Facilitar el registro de ventas.
* Permitir precios especiales personalizados dentro de una venta.
* Controlar el proceso de producción.
* Reducir errores en el registro de información.
* Facilitar la consulta de información.
* Proporcionar una interfaz usable desde computador y dispositivos móviles.
* Mantener una arquitectura que permita futuras ampliaciones.

---

# 5. Alcance

El sistema contempla progresivamente funcionalidades relacionadas con:

* Autenticación de usuarios.
* Gestión de usuarios y roles.
* Gestión de productos.
* Gestión de categorías de productos.
* Gestión de unidades.
* Gestión de inventario.
* Movimientos de inventario.
* Compras.
* Ventas.
* Producción.
* Pedidos.
* Pagos y demás funcionalidades administrativas que correspondan al modelo del sistema.

El frontend y backend se desarrollarán de forma integrada.

Las funcionalidades de sincronización en la nube, funcionamiento offline, facturación electrónica, integraciones externas y otras ampliaciones podrán incorporarse posteriormente.

---

# 6. Usuarios del sistema

Inicialmente se contemplan dos perfiles principales.

## Administrador

Tendrá permisos administrativos sobre las funcionalidades autorizadas del sistema.

Entre sus responsabilidades se encuentran:

* Administrar usuarios.
* Administrar productos y datos maestros.
* Gestionar inventario.
* Realizar ajustes administrativos de inventario.
* Consultar información administrativa.
* Gestionar las funcionalidades correspondientes a su rol.

## Vendedor

Tendrá acceso principalmente a las operaciones comerciales y de venta.

Entre sus responsabilidades se encuentran:

* Registrar ventas.
* Consultar productos disponibles.
* Aplicar precios especiales dentro de una venta cuando corresponda.
* Consultar información necesaria para realizar sus operaciones.

Los permisos definitivos serán controlados por el backend.

---

# 7. Principios del proyecto

Durante el desarrollo se adoptan los siguientes principios:

* La tecnología debe adaptarse al negocio.
* La interfaz debe ser sencilla e intuitiva.
* La información debe ser consistente y confiable.
* Las operaciones que afecten inventario deben quedar registradas.
* Los cambios deben realizarse de manera controlada.
* Se evitarán refactorizaciones innecesarias cuando la arquitectura existente funcione correctamente.
* El sistema debe poder evolucionar sin alterar innecesariamente sus componentes existentes.
* La documentación debe mantenerse alineada con la implementación real.
* La aplicación debe ser usable desde computadores y dispositivos móviles.

---

# 8. Visión a futuro

Una vez consolidada la versión inicial del sistema, podrán evaluarse funcionalidades como:

* Sincronización con la nube.
* Funcionamiento offline con sincronización posterior.
* Acceso desde múltiples equipos mediante infraestructura centralizada.
* Reportes avanzados.
* Estadísticas de ventas.
* Dashboard administrativo.
* Facturación electrónica.
* Integraciones con servicios externos.
* Nuevas funcionalidades según las necesidades del negocio.

Estas funcionalidades no deben considerarse implementadas hasta que hayan sido desarrolladas y verificadas.

---

# 9. Criterios de éxito

El proyecto se considerará exitoso cuando:

* El inventario represente correctamente las existencias reales.
* Las operaciones que afecten inventario generen los movimientos correspondientes.
* Las ventas puedan registrarse correctamente.
* Los precios especiales puedan aplicarse únicamente a la venta correspondiente.
* La producción actualice correctamente las existencias involucradas.
* Los usuarios puedan utilizar el sistema con facilidad.
* La aplicación pueda utilizarse correctamente desde computadores y dispositivos móviles.
* La información permanezca consistente entre frontend, backend y base de datos.

---

# 10. Observaciones

Este documento constituye la visión general del proyecto Mantas Guajiras y sirve como referencia para los demás documentos técnicos y funcionales.

La documentación deberá actualizarse cuando exista un cambio relevante en la arquitectura, reglas de negocio o funcionalidades implementadas.