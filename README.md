# MantasGuajiras

> Sistema de gestión para el control de inventario, producción y ventas del negocio **Mantas Guajiras**.

> **Versión:** `v0.3.0`
>
> **Estado:** 🚧 En desarrollo

---

## 📖 Descripción

Mantas Guajiras es un sistema desarrollado para digitalizar la administración del negocio, reemplazando los registros manuales por una plataforma moderna, intuitiva y confiable.

El sistema permitirá controlar el inventario de mantas y telas, registrar ventas, gestionar pedidos personalizados, administrar la producción de mantas y generar reportes para facilitar la toma de decisiones.

Inicialmente el sistema funcionará de forma local y, una vez estabilizado, se implementará la sincronización en la nube para permitir el trabajo desde múltiples dispositivos sin perder información.

---

## 🎯 Objetivos

- Controlar el inventario de mantas y telas.
- Registrar ventas de mantas y telas.
- Gestionar pedidos personalizados.
- Controlar la producción de mantas.
- Registrar ingresos y pagos.
- Generar reportes administrativos.
- Permitir funcionamiento offline con sincronización automática en futuras versiones.

---

### Implementado actualmente

- Estructura inicial del backend con Spring Boot.
- Gestión de productos.
- Gestión de categorías de productos.
- Gestión de unidades.
- Gestión de inventario.
- Registro de compras.
- Registro de producción.
- Registro de pedidos (`Order`).
- Registro de ventas (`Sale`).
- Manejo de precios normales y precios especiales en ventas.
- Descuentos personalizados por producto en una venta.
- Migraciones de base de datos mediante versiones.
- Pruebas de endpoints mediante Swagger.

### Pendiente

- Finalizar módulos restantes del backend.
- Completar reglas de negocio pendientes.
- Desarrollo del frontend.
- Integración frontend-backend.
- Pruebas funcionales completas.
- Implementación local.
- Sincronización y despliegue en la nube.

---
## 🛠️ Tecnologías

### Frontend

- React
- TypeScript
- Vite
- Tailwind CSS

### Backend

- Java 21
- Spring Boot

### Base de datos

- PostgreSQL

### Herramientas

- Git
- GitHub

---

## 📁 Estructura del proyecto

```text
MantasGuajiras/
│
├── backend/
│
├── frontend/
│
├── database/
│   ├── migrations/
│   └── seeds/
│
├── docs/
│   ├── diagrams/
│   │   ├── architecture/
│   │   ├── business-flows/
│   │   ├── erd/
│   │   └── use-cases/
│   │
│   ├── 00-vision.md
│   ├── 01-requisitos.md
│   ├── 02-reglas-negocio.md
│   ├── 03-arquitectura.md
│   ├── 04-base-de-datos.md
│   ├── 05-api.md
│   └── 06-despliegue.md
│
└── README.md
```

---

## 📄 Documentación

Toda la documentación técnica y funcional del proyecto se encuentra en la carpeta **docs/**.

Incluye:

- Visión del proyecto.
- Requisitos funcionales y no funcionales.
- Reglas de negocio.
- Arquitectura del sistema.
- Modelo de base de datos.
- Documentación de la API.
- Diagramas técnicos y funcionales.

---

## 👥 Roles del sistema

El sistema contará inicialmente con dos tipos de usuario:

- **Administrador:** acceso completo al sistema.
- **Vendedor:** gestión de ventas, producción, pedidos y consulta de inventario.

---

## 🚀 Estado del proyecto

**Versión actual**

**v0.1.0**

**Estado**

🚧 En desarrollo

---

## 📈 Evolución del proyecto

El proyecto seguirá un esquema de versionado semántico. Cada versión representa un hito importante dentro del desarrollo.

| Versión | Objetivo |
|----------|----------|
| **v0.1.0** | Estructura inicial del proyecto, repositorio y documentación. |
| **v0.2.0** | Diseño e implementación inicial de la base de datos. |
| **v0.3.0** | Inicio del desarrollo del backend y establecimiento de la arquitectura y lógica base del sistema. |
| **v0.4.0** | Consolidación del backend y finalización de los módulos principales. |
| **v0.5.0** | Desarrollo del frontend e integración inicial con la API. |
| **v0.6.0** | Integración completa del sistema y pruebas funcionales. |
| **v0.7.0** | Pruebas con datos reales y corrección de errores. |
| **v0.8.0** | Implementación local en el negocio y conteo físico del inventario inicial. |
| **v0.9.0** | Sincronización y despliegue en la nube. |
| **v1.0.0** | Primera versión oficial utilizada por el negocio. |

> A partir de la versión **v1.0.0**, las nuevas funcionalidades se desarrollarán mediante versiones incrementales (v1.1.0, v1.2.0, etc.), manteniendo siempre una versión estable en producción.

---

## 📅 Plan de desarrollo

### Sprint 0
Análisis y diseño del sistema.

### Sprint 1
Diseño de la base de datos.

### Sprint 2
Desarrollo del backend. 🚧 En progreso

- Arquitectura base.
- Entidades y persistencia.
- CRUD de módulos principales.
- Reglas de negocio.
- Gestión de inventario.
- Compras.
- Producción.
- Pedidos.
- Ventas.
- Documentación de API.

### Sprint 3
Desarrollo del frontend.

### Sprint 4
Integración completa del sistema.

### Sprint 5
Pruebas con datos reales.

### Sprint 6
Implementación local.

### Sprint 7
Sincronización y despliegue en la nube.

---

## 📌 Filosofía del proyecto

Este software está siendo desarrollado para un negocio real.

Cada decisión de diseño prioriza:

- Simplicidad para el usuario.
- Confiabilidad de la información.
- Mantenibilidad del código.
- Escalabilidad para futuras versiones.
- Documentación clara y actualizada.

La tecnología se adapta al negocio, no el negocio a la tecnología.

---

## 📜 Licencia

Actualmente este proyecto es de uso privado y se encuentra en desarrollo.