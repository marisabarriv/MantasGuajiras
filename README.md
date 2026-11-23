# Mantas Guajiras

> Sistema de gestión para el control de inventario, producción y ventas del negocio **Mantas Guajiras**.

> **Versión:** `v1.0.0-rc.1`

> **Estado:** ✅ Funcionalmente completo · 🔧 En preparación para producción (se usa en local)

---

## 📖 Descripción

Mantas Guajiras es un sistema desarrollado para digitalizar la administración del negocio, reemplazando los registros manuales por una plataforma moderna, intuitiva y confiable.

El sistema permite gestionar productos, inventario, ventas y producción mediante una aplicación web conectada a un backend centralizado.

La aplicación está diseñada como una plataforma web responsive, adaptable a computadores, laptops, tablets y celulares.

Todos los módulos están implementados e integrados. El sistema se ejecuta en un computador local; la puesta en producción sigue la lista de `docs/06-despliegue.md` (sección 14.4).

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

## ✅ Implementado

### Módulos

| Módulo               | Qué hace                                                                                  |
| -------------------- | ----------------------------------------------------------------------------------------- |
| Autenticación        | Inicio de sesión con JWT, contraseñas cifradas con BCrypt, aviso de sesión vencida.       |
| Usuarios             | Registro de vendedores con aprobación del administrador, roles, restablecer contraseña, perfil con foto. |
| Productos            | Mantas (`#`) y telas (metros), código interno automático, código de barras, activar/desactivar. |
| Inventario           | Existencias separadas del producto, historial de movimientos, alertas de stock bajo, ajustes por conteo físico. |
| Ventas               | Carrito, escáner de códigos, precio especial por producto, descuento, protección contra doble envío. |
| Compras              | Entradas de inventario con verificación del total en el backend.                          |
| Producción           | Consumo de tela y generación de mantas en una sola transacción.                           |
| Personalizadas       | Mantas por encargo con descuento de tela, abono inicial, pagos posteriores y saldo.       |
| App instalable (PWA) | Instalación en computador y celular, página sin conexión.                                 |
| Copias de seguridad  | Copias automáticas cada 2 horas y al encender el equipo, prueba semanal de restauración (`docs/09-copias-de-seguridad.md`). |

### Garantías del backend

* Los cálculos de precios, totales y descuentos se repiten en el servidor.
* El inventario nunca queda negativo; las mantas solo admiten cantidades enteras.
* Bloqueo de la fila de inventario para que dos ventas simultáneas no vendan la misma unidad.
* Una manta no puede quedar en metros ni una tela en `#`; no se cambia la unidad de un producto con historial.
* Ventas, compras y producciones registradas no se editan ni se borran (trazabilidad).
* Permisos por rol aplicados en el servidor (ver **Roles**).

---

## 📌 Pendiente para producción

* Elegir dónde se instalará (servidor o equipo dedicado) y configurar HTTPS y dominio.
* Recorrido manual de los flujos críticos con datos reales.
* Completar la lista de `docs/06-despliegue.md` → sección 14.4.

La sincronización automática en la nube y el funcionamiento offline podrán evaluarse como mejoras futuras, pero **no forman parte de la implementación actual**.

---

## ⚡ Inicio rápido (local)

Requisitos: Java 21, Node.js 20+, PostgreSQL 16+.

```bash
createdb -U postgres mantas_guajiras_db
```

1. `backend/src/main/resources/application-dev.example.yml` → copiar como `application-dev.yml` y poner la contraseña de PostgreSQL.
2. `backend/.env.example` → copiar como `backend/.env` y completar.
3. `frontend/.env.example` → copiar como `frontend/.env`.

```bash
cd backend && ./mvnw spring-boot:run
```

```bash
cd frontend && npm ci && npm run dev
```

En Windows el backend se arranca con `mvnw.cmd spring-boot:run`. Guía completa: `docs/06-despliegue.md`, sección 14.

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
├── backend/                         Spring Boot (Java 21)
│   ├── src/main/java/com/mantasguajiras/backend/
│   │   ├── common/                  DTO, excepciones y utilidades compartidas
│   │   ├── config/                  CORS, datos iniciales, Jackson, OpenAPI
│   │   ├── security/                JWT, filtro y permisos por rol
│   │   ├── product/  productcategory/  unit/
│   │   ├── inventory/  inventorymovement/  movementtype/  sourcetype/
│   │   ├── sale/  purchase/  production/  order/
│   │   └── user/
│   ├── src/main/resources/
│   │   ├── db/migration/            Migraciones Flyway (V1 … V19)
│   │   ├── application.yml
│   │   ├── application-dev.example.yml
│   │   └── application-prod.yml
│   ├── src/test/                    Pruebas automatizadas
│   ├── .env.example
│   └── pom.xml
│
├── frontend/                        React + Vite
│   ├── public/                      Íconos, manifiesto PWA, página sin conexión
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/                Llamadas a la API
│   │   ├── styles/
│   │   ├── utils/
│   │   ├── App.jsx
│   │   └── main.jsx
│   ├── .env.example
│   └── package.json
│
├── database/
│   └── exports/                     Exportación del esquema (referencia)
│
├── scripts/
│   └── backup/                      Copias de seguridad y prueba de restauración
│
├── docs/
│   ├── diagrams/
│   │   ├── architecture/
│   │   ├── erd/
│   │   └── exports/
│   ├── 00-vision.md
│   ├── 01-requisitos.md
│   ├── 02-reglas-negocio.md
│   ├── 03-arquitectura.md
│   ├── 04-base-de-datos.md
│   ├── 05-api.md
│   ├── 06-despliegue.md
│   ├── 07-modelo-de-datos.md
│   ├── 08-frontend.md
│   └── 09-copias-de-seguridad.md
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
* Estrategia de despliegue e instalación paso a paso.
* Modelo de datos.
* Frontend.
* Copias de seguridad y recuperación.
* Diagramas técnicos y funcionales.

---

## 👥 Roles del sistema

* **Administrador:** todo el sistema, incluidos productos, categorías, unidades, ajustes de inventario, compras, montos de ventas y usuarios.
* **Vendedor:** registra ventas, consulta productos e inventario, y usa Producción y Personalizadas. No modifica datos maestros, no ajusta inventario ni ve montos de ventas y compras.

Los permisos se aplican en el backend (`SecurityConfig`); la tabla completa está en `docs/02-reglas-negocio.md` (RN-24A).

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

**Automáticas (backend)** — `./mvnw test`, contra una base de pruebas separada (`mantas_guajiras_test`):

| Prueba                     | Qué comprueba                                                                 |
| -------------------------- | ----------------------------------------------------------------------------- |
| `BackendApplicationTests`  | Las 19 migraciones se aplican desde cero y el esquema coincide con las entidades. |
| `RolePermissionsTest`      | Qué puede y qué no puede hacer cada rol.                                      |
| `ConcurrentSaleTest`       | Dos ventas simultáneas de la última unidad: solo una se registra.             |
| `ProductRulesTest`         | Categoría ↔ unidad, enteros en mantas, precio de compra = precio unitario.     |
| `ImageSignatureTest`       | La foto de perfil se valida por su contenido real.                            |

**Frontend:** `npm run lint` y `npm run build`.

**Manuales:** recorrido de los flujos críticos desde la interfaz (lista en `docs/06-despliegue.md`, sección 14.4).

---

## 🚀 Estado del proyecto

**Versión actual:** `v1.0.0-rc.1` (versión candidata)

**Estado:** funcionalmente completo, en uso local, en preparación para producción.

La versión `v1.0.0` corresponderá a la primera puesta en uso con los datos reales del negocio.

---

## 📈 Evolución del proyecto

El proyecto utiliza versionado semántico para identificar los principales hitos de desarrollo.

| Versión                   | Objetivo                                                                                                                      |
| ------------------------- | ----------------------------------------------------------------------------------------------------------------------------- |
| **v0.1.0**                | Estructura inicial del proyecto, repositorio y documentación.                                                                 |
| **v0.2.0**                | Diseño e implementación inicial de la base de datos.                                                                          |
| **v0.3.0**                | Inicio del desarrollo del backend y establecimiento de la arquitectura y lógica base.                                         |
| **v0.4.0**                | Consolidación del backend e inicio del desarrollo e integración del frontend.                                                 |
| **v1.0.0-rc.1**           | Todos los módulos integrados, permisos por rol, pruebas automatizadas y copias de seguridad. Versión candidata.             |
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

Este proyecto es de uso privado.
