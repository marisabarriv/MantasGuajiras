# API Design

**Proyecto:** Mantas Guajiras  
**Versión:** v1.0  
**Última actualización:** 2026-07-29

---

# 1. Objetivo

Este documento define las convenciones utilizadas para el diseño de la API REST del sistema **Mantas Guajiras**.

El propósito es garantizar consistencia entre todos los módulos del backend, facilitar el mantenimiento y ofrecer una interfaz predecible para el frontend.

---

# 2. Arquitectura

La API seguirá el estilo arquitectónico REST.

Cada recurso será representado mediante un endpoint y manipulado utilizando los métodos HTTP estándar.

---

# 3. URL base

Todas las rutas comenzarán con:

```text
/api/v1
```

Ejemplo:

```text
/api/v1/products
```

---

# 4. Convenciones de rutas

Las rutas:

- utilizarán nombres en plural;
- estarán escritas en inglés;
- utilizarán kebab-case únicamente cuando sea necesario.

Ejemplos:

```text
/api/v1/products

/api/v1/custom-orders

/api/v1/customers

/api/v1/sales

/api/v1/purchases

/api/v1/productions

/api/v1/users
```

---

# 5. Métodos HTTP

## GET

Obtiene información.

Ejemplo:

```http
GET /api/v1/products
```

---

## POST

Crea un recurso.

```http
POST /api/v1/products
```

---

## PUT

Actualiza completamente un recurso.

```http
PUT /api/v1/products/{id}
```

---

## PATCH

Actualiza parcialmente un recurso.

```http
PATCH /api/v1/products/{id}
```

---

## DELETE

Realiza eliminación lógica cuando aplique.

```http
DELETE /api/v1/products/{id}
```

---

# 6. Códigos HTTP

| Código | Significado |
|---------|-------------|
|200|OK|
|201|Created|
|204|No Content|
|400|Bad Request|
|401|Unauthorized|
|403|Forbidden|
|404|Not Found|
|409|Conflict|
|422|Unprocessable Entity|
|500|Internal Server Error|

---

# 7. Formato de respuestas

## Respuesta exitosa

```json
{
  "id": "uuid",
  "name": "Manta 21k",
  "unitPrice": 21000
}
```

---

## Error

```json
{
  "timestamp": "2026-07-29T16:30:12",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found.",
  "path": "/api/v1/products/1"
}
```

---

# 8. Paginación

Los listados utilizarán paginación.

Ejemplo:

```text
GET /api/v1/products?page=0&size=20
```

Parámetros:

| Parámetro | Descripción |
|-----------|-------------|
|page|Número de página|
|size|Cantidad de registros|

---

# 9. Ordenamiento

Ejemplo:

```text
GET /api/v1/products?sort=name,asc
```

o

```text
GET /api/v1/products?sort=unitPrice,desc
```

---

# 10. Filtros

Los filtros utilizarán parámetros de consulta.

Ejemplo:

```text
GET /api/v1/products?category=tela
```

```text
GET /api/v1/products?active=true
```

```text
GET /api/v1/products?manufacturable=true
```

Los filtros podrán combinarse.

---

# 11. Identificadores

Todas las entidades transaccionales utilizarán UUID.

Ejemplo:

```text
/api/v1/products/7cb4fd8f-d498-4fd2-baad-ccf1d44c36b8
```

---

# 12. Versionado

La API será versionada desde el inicio.

Versión actual:

```text
v1
```

Esto permitirá mantener compatibilidad en futuras versiones.

---

# 13. Autenticación

La autenticación se realizará mediante JWT.

Las rutas protegidas deberán incluir:

```http
Authorization: Bearer <token>
```

---

# 14. Autorización

Los permisos dependerán del rol del usuario.

Roles iniciales:

- ADMIN
- EMPLOYEE

Las reglas de autorización serán implementadas mediante Spring Security.

---

# 15. Validaciones

Toda validación de negocio se realizará en la capa de servicio.

Las validaciones de formato utilizarán Jakarta Validation.

Ejemplos:

- @NotBlank
- @NotNull
- @Positive
- @Size

---

# 16. Eliminación lógica

Siempre que sea posible se utilizará eliminación lógica mediante el campo:

```text
active
```

No se eliminarán registros que puedan afectar la trazabilidad del sistema.

---

# 17. Convenciones de nombres

## Endpoints

Plural.

```text
/products

/custom-orders

/customers
```

---

## DTO

Request:

```text
CreateProductRequest

UpdateProductRequest
```

Response:

```text
ProductResponse
```

---

## Servicios

```text
ProductService

SaleService
```

---

## Repositorios

```text
ProductRepository

SaleRepository
```

---

## Controladores

```text
ProductController

SaleController
```

---

# 18. Principios

La API deberá cumplir los siguientes principios:

- Consistencia.
- Simplicidad.
- Bajo acoplamiento.
- Alta cohesión.
- Separación entre entidades y DTO.
- Mensajes de error claros.
- Validaciones centralizadas.

---

# 19. Futuras mejoras

La arquitectura permite incorporar posteriormente:

- OpenAPI / Swagger.
- Rate limiting.
- Caché.
- Versionado avanzado.
- API pública.
- WebSockets para sincronización en tiempo real.

---

# 20. Documentos relacionados

- backend-architecture.md
- database-schema.md
- ERD (`../erd_dbml/mantas-guajiras.dbml`)
- README del proyecto