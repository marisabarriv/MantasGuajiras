# Backend Architecture

**Proyecto:** Mantas Guajiras  
**Versión:** v1.0  
**Última actualización:** 2026-07-29

---

# 1. Objetivo

Este documento describe la arquitectura del backend del sistema **Mantas Guajiras**.

El objetivo es definir una estructura clara, mantenible y escalable para el desarrollo de la aplicación, siguiendo las buenas prácticas de Spring Boot.

---

# 2. Tecnologías

El backend será desarrollado utilizando las siguientes tecnologías:

| Tecnología | Versión |
|------------|----------|
| Java | 21 |
| Spring Boot | 3.x |
| Maven | 3.x |
| PostgreSQL | 17 |
| Spring Data JPA | - |
| Flyway | - |
| Spring Security | - |
| JWT | - |
| Lombok | - |

---

# 3. Arquitectura

El proyecto seguirá una arquitectura en capas (*Layered Architecture*).

```text
                Client (React)

                      │

               REST Controllers

                      │

                  Services

                      │

               Repositories

                      │

                PostgreSQL
```

Cada capa tiene una única responsabilidad y solo puede comunicarse con la capa inmediatamente inferior.

---

# 4. Estructura del proyecto

El backend tendrá la siguiente organización.

```text
backend
└── src
    └── main
        ├── java
        │   └── com
        │       └── mantasguajiras
        │           ├── config
        │           ├── controller
        │           ├── dto
        │           ├── entity
        │           ├── exception
        │           ├── mapper
        │           ├── repository
        │           ├── security
        │           ├── service
        │           └── util
        │
        └── resources
            ├── db
            │   └── migration
            ├── application.yml
            └── application-dev.yml
```

---

# 5. Responsabilidad de cada paquete

## config

Configuraciones generales del proyecto.

Ejemplos:

- Beans.
- Configuración de CORS.
- Configuración de Jackson.
- Configuración de OpenAPI.

---

## controller

Expone los endpoints REST.

Responsabilidades:

- recibir solicitudes;
- validar la entrada;
- devolver respuestas HTTP.

No contiene lógica de negocio.

---

## dto

Objetos utilizados para intercambiar información entre cliente y servidor.

Se utilizan para:

- Request DTO
- Response DTO

Las entidades nunca se exponen directamente.

---

## entity

Representa las tablas de la base de datos mediante JPA.

Cada entidad corresponde a una tabla del modelo definido en el ERD.

---

## exception

Contiene las excepciones personalizadas y el manejo global de errores.

Ejemplos:

- ResourceNotFoundException
- BusinessException
- GlobalExceptionHandler

---

## mapper

Convierte entidades en DTO y viceversa.

Esto desacopla la base de datos de la API.

---

## repository

Acceso a datos mediante Spring Data JPA.

Su única responsabilidad es interactuar con la base de datos.

---

## security

Configuración de autenticación y autorización.

Incluye:

- JWT
- filtros
- configuración de Spring Security

---

## service

Implementa toda la lógica del negocio.

Aquí se gestionan:

- ventas;
- compras;
- producción;
- pedidos;
- inventario.

Los servicios coordinan repositorios y validaciones.

---

## util

Clases auxiliares reutilizables por todo el proyecto.

---

# 6. Flujo de una solicitud

Una petición seguirá el siguiente flujo.

```text
Cliente

↓

Controller

↓

Service

↓

Repository

↓

PostgreSQL
```

La respuesta seguirá el camino inverso.

---

# 7. Persistencia

Toda la persistencia será gestionada mediante:

- Spring Data JPA.
- Hibernate.

No se utilizarán consultas SQL embebidas salvo que exista una justificación de rendimiento.

---

# 8. Migraciones

Toda modificación del esquema de base de datos se realizará mediante Flyway.

Cada cambio estructural deberá incluir una nueva migración.

Ejemplo:

```text
V1__create_catalog_tables.sql

V2__create_product_tables.sql

V3__create_inventory_tables.sql
```

Las migraciones nunca deberán modificarse después de haber sido ejecutadas en un entorno compartido.

---

# 9. Convenciones

## Clases

- PascalCase

Ejemplo:

```text
ProductService
```

---

## Métodos

- camelCase

Ejemplo:

```text
findById()

createProduct()

updateInventory()
```

---

## Variables

- camelCase

---

## Paquetes

- minúsculas

Ejemplo:

```text
repository
```

---

## Tablas

- snake_case

Ejemplo:

```text
sale_item
```

---

# 10. Principios de desarrollo

Durante el proyecto se seguirán los siguientes principios:

- Responsabilidad única.
- Separación de responsabilidades.
- Código limpio.
- Evitar duplicación.
- Programación orientada a interfaces cuando sea apropiado.
- Validación en la capa de servicio.
- Uso de DTO para toda comunicación externa.

---

# 11. Escalabilidad

La arquitectura fue diseñada para facilitar la incorporación futura de nuevas funcionalidades, entre ellas:

- Bill of Materials (BOM).
- Reportes.
- Dashboard.
- Sincronización en la nube.
- Aplicación móvil.

---

# 12. Documentos relacionados

- `database-schema.md`
- ERD (`../erd_dbml/mantas-guajiras.dbml`)
- README del proyecto