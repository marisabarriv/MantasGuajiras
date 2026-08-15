# Documento de Arquitectura

| Campo | Valor |
|--------|-------|
| **Proyecto** | Mantas Guajiras |
| **Documento** | Arquitectura del Sistema |
| **Código** | DOC-03 |
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

Este documento describe la arquitectura que seguirá el sistema Mantas Guajiras durante su desarrollo.

El objetivo es definir una estructura organizada, mantenible y escalable que facilite la incorporación de nuevas funcionalidades sin afectar el funcionamiento del sistema.

---

# 2. Arquitectura general

El sistema seguirá una arquitectura cliente-servidor.

Estará compuesto por tres componentes principales:

- Frontend
- Backend
- Base de datos

```
┌───────────────┐
│   Frontend    │
│ React + Vite  │
└───────┬───────┘
        │ HTTP / REST
        │
┌───────▼───────┐
│    Backend    │
│ Spring Boot   │
└───────┬───────┘
        │
        │ JPA / Hibernate
        │
┌───────▼───────┐
│ PostgreSQL    │
└───────────────┘
```

---

# 3. Frontend

El frontend será desarrollado utilizando:

- React
- TypeScript
- Vite
- Tailwind CSS

Será una aplicación web responsiva, diseñada para funcionar correctamente en computadores y dispositivos móviles.

Toda la lógica del negocio permanecerá en el backend.

El frontend será responsable únicamente de:

- Mostrar información.
- Capturar datos del usuario.
- Consumir la API.
- Validaciones básicas de formularios.

---

# 4. Backend

El backend será desarrollado utilizando:

- Java 21
- Spring Boot

Será responsable de:

- Autenticación.
- Autorización.
- Reglas de negocio.
- Validación de datos.
- Gestión del inventario.
- Gestión de ventas.
- Gestión de pedidos.
- Gestión de producción.
- Persistencia de datos.

Toda operación importante deberá ejecutarse desde el backend.

---

# 5. Base de datos

La información será almacenada en PostgreSQL.

La base de datos será la única fuente de verdad del sistema.

Toda modificación deberá realizarse mediante el backend.

No existirán accesos directos desde el frontend hacia la base de datos.

---

# 6. Arquitectura del backend

El backend seguirá una arquitectura por capas inspirada en los principios de Clean Architecture.

```
Controller
      │
      ▼
Service
      │
      ▼
Repository
      │
      ▼
Database
```

Cada capa tendrá una responsabilidad específica.

### Controller

- Recibir solicitudes HTTP.
- Validar parámetros básicos.
- Enviar respuestas.

### Service

- Contener toda la lógica del negocio.
- Aplicar reglas de negocio.
- Coordinar operaciones.

### Repository

- Comunicarse con PostgreSQL mediante Spring Data JPA.

---

# 7. Arquitectura del frontend

El frontend se organizará por funcionalidades.

Ejemplo:

```
src/
│
├── components/
├── pages/
├── services/
├── hooks/
├── layouts/
├── routes/
├── types/
├── utils/
└── assets/
```

Cada módulo tendrá independencia respecto a los demás.

---

# 8. Comunicación

La comunicación entre frontend y backend se realizará mediante una API REST.

Las respuestas utilizarán formato JSON.

Las operaciones principales serán:

- Consultar inventario.
- Registrar ventas.
- Registrar producción.
- Registrar pedidos.
- Consultar reportes.

---

# 9. Seguridad

El acceso al sistema requerirá autenticación.

Cada usuario tendrá un rol asignado.

Los permisos serán administrados desde el backend.

Inicialmente existirán dos roles:

- Administrador
- Vendedor

---

# 10. Funcionamiento sin conexión

Durante la primera versión el sistema funcionará utilizando una única base de datos PostgreSQL instalada en el negocio.

En una fase posterior se implementará una base de datos centralizada en la nube.

Cada equipo trabajará sobre una copia local de la información y sincronizará automáticamente los cambios cuando exista conexión con Internet.

La sincronización se realizará de forma periódica sin intervención del usuario.

---

# 11. Escalabilidad

La arquitectura permitirá incorporar nuevos módulos sin modificar significativamente los existentes.

Entre las posibles ampliaciones futuras se contemplan:

- Dashboard administrativo.
- Estadísticas.
- Facturación electrónica.
- Aplicación móvil.
- Integraciones con otros servicios.

---

# 12. Tecnologías seleccionadas

| Componente | Tecnología |
|------------|------------|
| Frontend | React |
| Lenguaje Frontend | TypeScript |
| Estilos | Tailwind CSS |
| Bundler | Vite |
| Backend | Spring Boot |
| Lenguaje Backend | Java 21 |
| Base de datos | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Control de versiones | Git |
| Repositorio | GitHub |

---

# 13. Principios arquitectónicos

El desarrollo seguirá los siguientes principios:

- Separación de responsabilidades.
- Bajo acoplamiento.
- Alta cohesión.
- Escalabilidad.
- Reutilización de código.
- Mantenibilidad.
- Seguridad.
- Documentación continua.

---

# 14. Observaciones

La arquitectura descrita en este documento servirá como base para el desarrollo de todas las versiones del sistema.

Cualquier cambio importante deberá reflejarse en este documento antes de ser implementado.