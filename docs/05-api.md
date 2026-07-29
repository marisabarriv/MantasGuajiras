# Documento de API

| Campo | Valor |
|--------|-------|
| **Proyecto** | Mantas Guajiras |
| **Documento** | Especificación de la API |
| **Código** | DOC-05 |
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

Este documento describe la API REST que permitirá la comunicación entre el frontend y el backend del sistema Mantas Guajiras.

La API seguirá una arquitectura REST y utilizará JSON como formato de intercambio de datos.

---

# 2. Características generales

- Arquitectura REST.
- Comunicación mediante HTTP/HTTPS.
- Formato JSON.
- UTF-8.
- Versionado mediante URL.
- Autenticación mediante JWT (implementación futura).

---

# 3. URL base

```
/api/v1
```

---

# 4. Módulos de la API

La API estará organizada por módulos funcionales.

## Autenticación

```
POST    /auth/login
POST    /auth/logout
GET     /auth/me
```

---

## Usuarios

```
GET     /users
GET     /users/{id}
POST    /users
PUT     /users/{id}
PATCH   /users/{id}/status
DELETE  /users/{id}
```

---

## Clientes

```
GET     /clients
GET     /clients/{id}
POST    /clients
PUT     /clients/{id}
DELETE  /clients/{id}
```

---

## Tipos de manta

```
GET     /blanket-types
POST    /blanket-types
PUT     /blanket-types/{id}
DELETE  /blanket-types/{id}
```

---

## Inventario de mantas

```
GET     /blanket-inventory
GET     /blanket-inventory/{id}
PATCH   /blanket-inventory/{id}
```

---

## Tipos de tela

```
GET     /fabric-types
POST    /fabric-types
PUT     /fabric-types/{id}
DELETE  /fabric-types/{id}
```

---

## Inventario de telas

```
GET     /fabric-inventory
GET     /fabric-inventory/{id}
PATCH   /fabric-inventory/{id}
```

---

## Producción

```
GET     /production
POST    /production
GET     /production/{id}
```

---

## Ventas

```
GET     /sales
GET     /sales/{id}
POST    /sales
```

---

## Pedidos

```
GET     /orders
GET     /orders/{id}
POST    /orders
PUT     /orders/{id}
PATCH   /orders/{id}/status
```

---

## Pagos

```
POST    /orders/{id}/payments
GET     /orders/{id}/payments
```

---

## Movimientos de inventario

```
GET     /inventory-movements
GET     /inventory-movements/{id}
```

---

## Reportes

```
GET     /reports/inventory
GET     /reports/sales
GET     /reports/production
GET     /reports/orders
```

---

# 5. Métodos HTTP

| Método | Uso |
|---------|-----|
| GET | Consultar información |
| POST | Crear registros |
| PUT | Actualizar completamente |
| PATCH | Actualizar parcialmente |
| DELETE | Eliminar o desactivar registros |

---

# 6. Códigos de respuesta

| Código | Significado |
|----------|------------|
| 200 | Solicitud exitosa |
| 201 | Recurso creado |
| 204 | Operación exitosa sin contenido |
| 400 | Solicitud inválida |
| 401 | No autenticado |
| 403 | Sin permisos |
| 404 | Recurso no encontrado |
| 409 | Conflicto de información |
| 500 | Error interno del servidor |

---

# 7. Seguridad

El acceso a la API requerirá autenticación.

Cada solicitud incluirá un token JWT en el encabezado Authorization.

La autorización dependerá del rol del usuario.

---

# 8. Validaciones

La API validará:

- Campos obligatorios.
- Tipos de datos.
- Reglas de negocio.
- Permisos del usuario.
- Existencia de recursos.

Toda validación será realizada en el backend.

---

# 9. Formato de respuestas

Todas las respuestas seguirán una estructura consistente.

Respuesta exitosa:

```
{
    "success": true,
    "data": { ... }
}
```

Respuesta con error:

```
{
    "success": false,
    "message": "...",
    "errors": [ ... ]
}
```

---

# 10. Versionado

Todas las rutas estarán agrupadas bajo:

```
/api/v1
```

Cuando existan cambios incompatibles se creará una nueva versión de la API.

Ejemplo:

```
/api/v2
```

---

# 11. Documentación

La documentación técnica de la API será generada automáticamente mediante Swagger/OpenAPI durante el desarrollo del backend.

---

# 12. Observaciones

La especificación descrita en este documento constituye el contrato entre el frontend y el backend.

Cualquier modificación en los endpoints deberá reflejarse en este documento y mantenerse sincronizada con la documentación generada por Swagger.