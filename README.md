# Mantas Guajiras

> Sistema de gestión para el control de inventario, producción y ventas del negocio **Mantas Guajiras**.

> **Versión:** `v0.4.0`

> **Estado:** 🚧 En desarrollo

---

## 📖 Descripción

Mantas Guajiras es un sistema desarrollado para digitalizar la administración del negocio, reemplazando los registros manuales por una plataforma moderna, intuitiva y confiable.

El sistema permite gestionar productos, inventario, ventas y producción mediante una aplicación web conectada a un backend centralizado.

La aplicación está diseñada como una plataforma web responsive, adaptable a computadores, laptops, tablets y celulares.

Actualmente el proyecto se encuentra en desarrollo y en proceso de integración entre el frontend y el backend.

---

## 🎯 Objetivos

* Controlar el inventario de mantas y telas.
* Registrar ventas de mantas y telas.
* Gestionar productos y sus categorías.
* Controlar la producción de mantas.
* Registrar los movimientos de inventario.
* Aplicar precios normales o precios especiales durante una venta.
* Mantener trazabilidad de las operaciones que afectan el inventario.
* Facilitar la administración del negocio mediante una interfaz web sencilla.
* Mantener una arquitectura preparada para futuras ampliaciones.

---

## 🚧 Implementado actualmente

### Backend

* Estructura base del backend con Spring Boot.
* Java 21.
* Gestión de usuarios y autenticación mediante JWT.
* Gestión de productos.
* Gestión de categorías de productos.
* Gestión de unidades.
* Gestión de inventario.
* Registro de movimientos de inventario.
* Registro de compras.
* Registro de producción.
* Registro de ventas.
* Manejo de precios normales y precios especiales en ventas.
* Descuentos personalizados por producto dentro de una venta.
* Validaciones y reglas de negocio.
* Manejo de errores específicos.
* Persistencia mediante JPA/Hibernate.
* Migraciones de base de datos mediante Flyway.
* Base de datos PostgreSQL.

### Frontend

* Aplicación web desarrollada con React.
* Vite.
* JavaScript y JSX.
* CSS.
* React Router.
* Integración con la API REST.
* Autenticación mediante token JWT.
* Página de inicio de sesión.
* Registro de usuarios.
* Gestión de productos.
* Gestión de producción.
* Registro de nuevas ventas.
* Carrito de venta.
* Aplicación de precios especiales por producto durante la venta.
* Interfaz responsive para diferentes tamaños de pantalla.

---

## 📌 Pendiente

* Completar la integración de todos los módulos del frontend con el backend.
* Completar las reglas de negocio que aún se encuentren en desarrollo.
* Realizar pruebas funcionales completas.
* Validar el sistema con datos reales.
* Preparar la implementación local definitiva.
* Definir y preparar el entorno de producción.
* Implementar funcionalidades adicionales conforme evolucionen las necesidades del negocio.

La sincronización automática en la nube y el funcionamiento offline podrán evaluarse como mejoras futuras, pero **no forman parte de la implementación actual**.

---

## 🛠️ Tecnologías

### Frontend

* React
* JavaScript
* JSX
* CSS
* Vite
* React Router

### Backend

* Java 21
* Spring Boot
* Spring Data JPA
* Hibernate
* MapStruct
* Lombok
* Bean Validation
* JWT

### Base de datos

* PostgreSQL
* Flyway

### Herramientas

* Git
* GitHub

---

## 📁 Estructura del proyecto

```text
MantasGuajiras/
│
├── backend/
│
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── assets/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/
│   │   ├── App.jsx
│   │   ├── App.css
│   │   ├── index.css
│   │   └── main.jsx
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
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
│   ├── 06-despliegue.md
│   └── 07-modelo-datos.md
│
└── README.md
```

---

## 📄 Documentación

Toda la documentación técnica y funcional del proyecto se encuentra en la carpeta **`docs/`**.

Incluye:

* Visión del proyecto.
* Requisitos funcionales y no funcionales.
* Reglas de negocio.
* Arquitectura del sistema.
* Modelo de base de datos.
* Documentación de la API.
* Estrategia de despliegue.
* Modelo de datos.
* Diagramas técnicos y funcionales.

---

## 👥 Roles del sistema

El sistema contempla inicialmente dos roles principales:

* **Administrador:** administración general del sistema y acceso a las funcionalidades administrativas.
* **Vendedor:** operaciones relacionadas con ventas y las funcionalidades permitidas por su rol.

Los permisos y restricciones se aplican desde el backend.

---

## 📦 Productos e inventario

El sistema utiliza un modelo general de productos que permite manejar diferentes categorías.

Actualmente se contemplan:

* **Manta:** cantidades expresadas en unidades enteras (`#`).
* **Tela:** cantidades expresadas en metros.

Cada producto mantiene su información de precios y configuración, mientras que la existencia actual se administra mediante su registro de inventario.

La cantidad actual de inventario se encuentra en `Inventory.quantity`.

Los cambios de existencia se registran mediante movimientos de inventario, permitiendo identificar operaciones como:

* Compra.
* Venta.
* Producción.
* Ajuste.

El campo `minimumStock` representa un nivel mínimo de referencia y no la existencia actual.

---

## 💰 Ventas

Las ventas permiten trabajar con el precio base del producto y, cuando sea necesario, aplicar un precio especial o personalizado para un producto específico dentro de la venta.

El precio especial:

* Se aplica únicamente a esa venta.
* No modifica permanentemente el precio base del producto.
* Puede utilizarse incluso para una sola unidad.
* Permite calcular el descuento correspondiente respecto al precio original.

El precio finalmente aplicado se conserva en el detalle de la venta.

---

## 🏭 Producción

El módulo de producción permite registrar la transformación de materia prima en productos terminados.

En el caso de producción de mantas:

* Se selecciona el producto de tela utilizado.
* Se indica la cantidad de tela requerida por manta.
* Se selecciona el producto de manta generado.
* Se indica la cantidad de mantas producidas.
* Se descuenta la tela utilizada del inventario.
* Se incrementa el inventario de mantas producidas.
* Se registran los movimientos correspondientes.

Estas operaciones se ejecutan de forma transaccional para mantener la consistencia del inventario.

---

## 🔐 Seguridad

El sistema utiliza autenticación mediante JWT.

Las solicitudes protegidas utilizan el token de autorización correspondiente.

El backend es responsable de:

* Autenticación.
* Autorización.
* Validación de datos.
* Aplicación de reglas de negocio.
* Control de acceso según el rol.
* Protección de las operaciones sensibles.

---

## 🧪 Pruebas

Las funcionalidades del sistema se validan principalmente mediante la interacción del frontend con la API.

Los endpoints **no se prueban mediante Swagger como parte del flujo establecido del proyecto**.

Las pruebas funcionales se realizan sobre las funcionalidades reales de la aplicación y su integración con el backend.

---

## 🚀 Estado del proyecto

**Versión actual:** `v0.4.0`

**Estado:** 🚧 En desarrollo

La versión actual representa una etapa de desarrollo en la que el backend cuenta con una base funcional consolidada y el frontend se encuentra en desarrollo e integración con la API.

---

## 📈 Evolución del proyecto

El proyecto utiliza versionado semántico para identificar los principales hitos de desarrollo.

| Versión                   | Objetivo                                                                                                                      |
| ------------------------- | ----------------------------------------------------------------------------------------------------------------------------- |
| **v0.1.0**                | Estructura inicial del proyecto, repositorio y documentación.                                                                 |
| **v0.2.0**                | Diseño e implementación inicial de la base de datos.                                                                          |
| **v0.3.0**                | Inicio del desarrollo del backend y establecimiento de la arquitectura y lógica base.                                         |
| **v0.4.0**                | Consolidación del backend e inicio del desarrollo e integración del frontend.                                                 |
| **Versiones posteriores** | Integración completa, pruebas funcionales, validación con datos reales, preparación para producción y nuevas funcionalidades. |
| **v1.0.0**                | Primera versión estable destinada al uso oficial del negocio.                                                                 |

> Las versiones posteriores se definirán de acuerdo con el avance real del proyecto y no como un cronograma rígido.

---

## 📅 Plan de desarrollo

### Sprint 0

Análisis y diseño del sistema.

### Sprint 1

Diseño e implementación inicial de la base de datos.

### Sprint 2

Desarrollo del backend.

* Arquitectura base.
* Entidades y persistencia.
* CRUD de módulos principales.
* Reglas de negocio.
* Gestión de inventario.
* Compras.
* Producción.
* Ventas.
* Seguridad y autenticación.
* Documentación de API.

### Sprint 3

Desarrollo e integración del frontend.

* Estructura de la aplicación web.
* Autenticación.
* Gestión de productos.
* Producción.
* Ventas.
* Integración con la API.

### Sprint 4

Integración completa del sistema y pruebas funcionales.

### Sprint 5

Validación con datos reales y corrección de errores.

### Sprint 6

Preparación e implementación local.

### Sprint 7

Evaluación de futuras necesidades de despliegue y funcionalidades adicionales.

---

## 📌 Filosofía del proyecto

Este software está siendo desarrollado para un negocio real.

Cada decisión de diseño prioriza:

* Simplicidad para el usuario.
* Confiabilidad de la información.
* Mantenibilidad del código.
* Seguridad.
* Consistencia de los datos.
* Escalabilidad para futuras versiones.
* Documentación clara y actualizada.

La tecnología se adapta al negocio, no el negocio a la tecnología.

---

## 📜 Licencia

Actualmente este proyecto es de uso privado y se encuentra en desarrollo.
