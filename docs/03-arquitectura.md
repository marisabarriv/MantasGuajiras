# Documento de Arquitectura

| Campo                    | Valor                    |
| ------------------------ | ------------------------ |
| **Proyecto**             | Mantas Guajiras          |
| **Documento**            | Arquitectura del Sistema |
| **Código**               | DOC-03                   |
| **Versión**              | v0.4.0                   |
| **Estado**               | En desarrollo            |
| **Responsable**          | Equipo de Desarrollo     |
| **Última actualización** | 05/09/2026               |

---

## Control de versiones

| Versión | Fecha      | Descripción                                   | Responsable          |
| ------- | ---------- | --------------------------------------------- | -------------------- |
| v0.1.0  | 29/07/2026 | Creación inicial del documento.               | Equipo de Desarrollo |
| v0.4.0  | 05/09/2026 | Actualización de la arquitectura del sistema. | Equipo de Desarrollo |

---

# 1. Introducción

Este documento describe la arquitectura actual del sistema Mantas Guajiras y establece la organización de sus principales componentes.

El objetivo es mantener una estructura organizada, mantenible y escalable que permita incorporar nuevas funcionalidades sin afectar innecesariamente los módulos existentes.

La arquitectura está compuesta principalmente por un frontend web, un backend y una base de datos.

---

# 2. Arquitectura general

Mantas Guajiras utiliza una arquitectura cliente-servidor compuesta por tres componentes principales:

* Frontend web.
* Backend.
* Base de datos.

La comunicación entre estos componentes se realiza de la siguiente manera:

```text
┌──────────────────────┐
│      Frontend        │
│   React + Vite       │
│ JavaScript / JSX / CSS│
└──────────┬───────────┘
           │
           │ HTTP / REST / JSON
           │
┌──────────▼───────────┐
│       Backend        │
│    Spring Boot       │
│       Java 21        │
└──────────┬───────────┘
           │
           │ JPA / Hibernate
           │
┌──────────▼───────────┐
│      PostgreSQL      │
└──────────────────────┘
```

El frontend se encarga de la interacción con el usuario y del consumo de los servicios disponibles en el backend.

El backend concentra la lógica de negocio, las validaciones, la seguridad y el acceso a los datos.

PostgreSQL almacena la información persistente del sistema.

---

# 3. Frontend

El frontend es una aplicación web desarrollada con React y Vite.

Las tecnologías principales utilizadas son:

* React.
* JavaScript.
* JSX.
* CSS.
* Vite.
* React Router.

La aplicación está diseñada para funcionar de manera responsiva en:

* Computadores de escritorio.
* Portátiles.
* Tabletas.
* Teléfonos celulares.

No se desarrolla una aplicación móvil nativa independiente. El acceso desde dispositivos móviles se realiza mediante la misma aplicación web adaptada a diferentes tamaños de pantalla.

El frontend es responsable de:

* Mostrar información al usuario.
* Capturar datos mediante formularios.
* Gestionar la navegación entre páginas.
* Consumir los endpoints del backend.
* Realizar validaciones básicas de formularios.
* Mostrar mensajes claros cuando una operación no puede realizarse.

La lógica principal del negocio permanece en el backend.

---

# 4. Estructura del frontend

La estructura actual del frontend se organiza de la siguiente manera:

```text
frontend/
│
├── public/
│
├── src/
│   ├── assets/
│   ├── components/
│   ├── pages/
│   ├── services/
│   ├── App.jsx
│   ├── App.css
│   ├── index.css
│   └── main.jsx
│
├── index.html
├── package.json
├── vite.config.js
└── ...
```

Las principales responsabilidades son:

### `components/`

Contiene componentes reutilizables de la interfaz.

### `pages/`

Contiene las páginas principales de la aplicación.

Actualmente incluye funcionalidades como:

* Inicio de sesión.
* Registro.
* Productos.
* Nueva venta.
* Producción.

### `services/`

Contiene los servicios encargados de comunicarse con el backend.

### `assets/`

Contiene recursos utilizados por la aplicación.

### `App.jsx`

Define la configuración principal de la aplicación y sus rutas.

### `main.jsx`

Es el punto de entrada de React.

### `App.css`

Contiene estilos generales y estilos específicos de los componentes y páginas.

### `index.css`

Contiene los estilos globales de la aplicación.

### `index.html`

Es el documento HTML base utilizado por Vite.

---

# 5. Backend

El backend está desarrollado utilizando:

* Java 21.
* Spring Boot.
* Spring Data JPA.
* Hibernate.
* Bean Validation.
* MapStruct.
* Lombok.
* Flyway.

El backend es responsable de:

* Autenticación.
* Autorización.
* Validación de datos.
* Reglas de negocio.
* Gestión de productos.
* Gestión de inventario.
* Gestión de ventas.
* Gestión de producción.
* Persistencia de información.
* Control de transacciones.

Las operaciones importantes relacionadas con los datos y las reglas del sistema se ejecutan en el backend.

---

# 6. Arquitectura por capas del backend

El backend utiliza una arquitectura organizada por capas.

```text
Controller
     │
     ▼
 Service
     │
     ▼
Repository
     │
     ▼
  Entity
     │
     ▼
PostgreSQL
```

Cada capa tiene una responsabilidad específica.

### Controller

Se encarga de:

* Recibir solicitudes HTTP.
* Recibir parámetros y datos de entrada.
* Validar la información correspondiente.
* Invocar los servicios.
* Construir las respuestas HTTP.

### Service

Contiene la lógica de negocio.

Se encarga de:

* Aplicar las reglas del sistema.
* Validar operaciones.
* Coordinar diferentes repositorios.
* Gestionar transacciones cuando corresponde.
* Ejecutar operaciones relacionadas con inventario, ventas y producción.

### Repository

Se encarga del acceso a los datos mediante Spring Data JPA.

### Entity

Representa las entidades persistentes utilizadas por el sistema y su relación con las tablas de PostgreSQL.

---

# 7. Base de datos

La información persistente del sistema se almacena en PostgreSQL.

La base de datos constituye la fuente principal de información del sistema.

El frontend no tiene acceso directo a PostgreSQL.

Todas las operaciones de lectura y modificación de datos se realizan mediante el backend.

Las modificaciones de la estructura de la base de datos se gestionan mediante Flyway para mantener un historial controlado de migraciones.

---

# 8. Comunicación entre frontend y backend

La comunicación se realiza mediante una API REST.

El frontend consume los endpoints proporcionados por el backend y recibe respuestas en formato JSON.

El flujo general es:

```text
Usuario
   │
   ▼
Frontend
   │
   │ HTTP
   ▼
Controller
   │
   ▼
Service
   │
   ▼
Repository
   │
   ▼
PostgreSQL
```

El frontend no implementa directamente las reglas de negocio.

Cuando una operación no puede realizarse, el backend proporciona la razón correspondiente para que el frontend pueda informar al usuario de manera clara.

Las pruebas funcionales de los endpoints se realizan desde el frontend cuando corresponde.

---

# 9. Seguridad

El sistema requiere autenticación para acceder a las funcionalidades protegidas.

La autenticación utiliza tokens JWT.

Los usuarios cuentan con roles que determinan las operaciones que pueden realizar.

Actualmente se contemplan principalmente los siguientes roles:

* Administrador.
* Vendedor.

La autorización y las reglas de acceso se controlan desde el backend.

El frontend adapta la interfaz según las funcionalidades disponibles para el usuario, pero las restricciones de seguridad son responsabilidad del backend.

---

# 10. Gestión de inventario

El inventario se encuentra integrado con las operaciones del sistema.

La cantidad actual disponible de un producto se almacena en el registro de inventario correspondiente.

Los movimientos de inventario permiten registrar operaciones como:

* Entradas de inventario.
* Salidas de inventario.
* Compras.
* Ventas.
* Producción.
* Ajustes manuales.

Las operaciones relacionadas con inventario deben mantener coherencia entre la existencia actual y los movimientos registrados.

Los cambios de inventario que forman parte de operaciones como ventas y producción se realizan desde el backend dentro de las transacciones correspondientes.

---

# 11. Gestión de producción

La producción utiliza el inventario existente para registrar el consumo de tela y la generación de mantas.

El flujo general es:

```text
Tela disponible
      │
      ▼
Consumo de tela
      │
      ▼
Proceso de producción
      │
      ▼
Generación de mantas
      │
      ▼
Actualización del inventario
```

La producción registra tanto el consumo del producto utilizado como la entrada del producto generado.

Las operaciones de producción se ejecutan de forma transaccional para mantener la consistencia de los datos.

---

# 12. Escalabilidad

La arquitectura permite incorporar nuevos módulos y funcionalidades sin modificar innecesariamente los componentes existentes.

La separación entre frontend, backend y base de datos permite ampliar el sistema de manera progresiva.

Entre las posibles ampliaciones futuras se encuentran:

* Dashboard administrativo.
* Estadísticas.
* Facturación electrónica.
* Integraciones con otros servicios.
* Funcionalidades adicionales de gestión.

Estas funcionalidades se incorporarán únicamente cuando formen parte del alcance del sistema.

---

# 13. Tecnologías seleccionadas

| Componente           | Tecnología                  |
| -------------------- | --------------------------- |
| Frontend             | React                       |
| Lenguaje Frontend    | JavaScript / JSX            |
| Estilos              | CSS                         |
| Bundler              | Vite                        |
| Navegación           | React Router                |
| Backend              | Spring Boot                 |
| Lenguaje Backend     | Java 21                     |
| Persistencia         | Spring Data JPA / Hibernate |
| Base de datos        | PostgreSQL                  |
| Migraciones          | Flyway                      |
| Mapeo de objetos     | MapStruct                   |
| Control de versiones | Git                         |
| Repositorio          | GitHub                      |

---

# 14. Principios arquitectónicos

El desarrollo seguirá los siguientes principios:

* Separación de responsabilidades.
* Bajo acoplamiento.
* Alta cohesión.
* Mantenibilidad.
* Reutilización de código.
* Seguridad.
* Consistencia de los datos.
* Escalabilidad.
* Documentación continua.

Se priorizarán cambios funcionales necesarios y se evitarán refactorizaciones estructurales que no sean necesarias para el funcionamiento del sistema.

---

# 15. Observaciones

La arquitectura descrita corresponde al estado actual del proyecto Mantas Guajiras.

El sistema se encuentra en desarrollo y puede incorporar nuevos módulos y funcionalidades a medida que avance el proyecto.

Cuando se realicen cambios importantes en la arquitectura, este documento deberá actualizarse para mantener la documentación alineada con la implementación real del sistema.
