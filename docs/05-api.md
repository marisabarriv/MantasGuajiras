# Documento de API

| Campo                    | Valor                    |
| ------------------------ | ------------------------ |
| **Proyecto**             | Mantas Guajiras          |
| **Documento**            | Especificación de la API |
| **Código**               | DOC-05                   |
| **Versión**              | v1.0.0-rc.1             |
| **Estado**               | Versión candidata        |
| **Responsable**          | Equipo de Desarrollo     |
| **Última actualización** | 23/11/2026               |

---

## Control de versiones

| Versión | Fecha      | Descripción                                             | Responsable          |
| ------- | ---------- | ------------------------------------------------------- | -------------------- |
| v0.1.0  | 29/07/2026 | Creación inicial del documento.                         | Equipo de Desarrollo |
| v0.4.0  | 05/09/2026 | Actualización de la API según la implementación actual. | Equipo de Desarrollo |
| v1.0.0-rc.1 | 23/11/2026 | Alineación con la implementación real tras la auditoría técnica. | Equipo de Desarrollo |

---

# 1. Introducción

Este documento describe la API REST utilizada para la comunicación entre el frontend y el backend del sistema Mantas Guajiras.

La API permite que el frontend consulte y modifique la información del sistema mediante solicitudes HTTP.

Los datos se intercambian principalmente en formato JSON.

---

# 2. Características generales

La API utiliza:

* Arquitectura REST.
* Comunicación mediante HTTP/HTTPS.
* Formato JSON.
* Codificación UTF-8.
* Autenticación mediante JWT.
* Autorización basada en roles.
* Validaciones realizadas en el backend.

Los endpoints se incorporan progresivamente conforme se desarrollan los diferentes módulos del sistema.

---

# 3. URL base

Los endpoints actuales utilizan la siguiente base:
/api

No se utiliza actualmente un prefijo `/api/v1`.

---

# 4. Módulos de la API

Endpoints implementados. Columna **Rol**: `Público` (sin sesión), `Todos` (cualquier usuario con sesión), `ADMIN` (solo administrador). Los permisos se aplican en `SecurityConfig`.

## Autenticación — `/auth`

| Método | Ruta              | Rol     | Descripción                                                    |
| ------ | ----------------- | ------- | -------------------------------------------------------------- |
| POST   | `/auth/login`     | Público | Inicia sesión con usuario o teléfono. Devuelve el token JWT. Mensaje único ante usuario o contraseña incorrectos. Tras 5 fallos seguidos, ese usuario queda bloqueado 15 minutos → `429`. |
| POST   | `/auth/register`  | Público | Registro de vendedor. La cuenta queda pendiente de aprobación. |

## Perfil propio — `/api/me`

| Método | Ruta                  | Rol   | Descripción                                          |
| ------ | --------------------- | ----- | ---------------------------------------------------- |
| GET    | `/api/me`             | Todos | Datos del usuario de la sesión.                      |
| PATCH  | `/api/me/password`    | Todos | Cambiar la propia contraseña.                        |
| PUT    | `/api/me/photo`       | Todos | Subir foto (`multipart/form-data`, campo `file`). JPG, PNG, WEBP o GIF, máximo 20 MB; se verifica el contenido real del archivo. |
| GET    | `/api/me/photo`       | Todos | Obtener la foto.                                     |
| DELETE | `/api/me/photo`       | Todos | Quitar la foto.                                      |

## Usuarios — `/users`

| Método | Ruta                          | Rol   | Descripción                                   |
| ------ | ----------------------------- | ----- | --------------------------------------------- |
| GET    | `/users`                      | ADMIN | Listar usuarios.                              |
| GET    | `/users/{id}`                 | ADMIN | Consultar un usuario.                         |
| POST   | `/users`                      | ADMIN | Crear usuario.                                |
| PUT    | `/users/{id}`                 | ADMIN | Editar usuario (datos, rol, activar).         |
| PATCH  | `/users/{id}/reset-password`  | ADMIN | Restablecer contraseña.                       |
| PATCH  | `/users/{id}/promote`         | ADMIN | Convertir en administrador.                   |
| DELETE | `/users/{id}`                 | ADMIN | Desactivar (no se borra). No permite dejar el sistema sin administradores activos. |

## Productos — `/api/products`

| Método | Ruta                  | Rol   | Descripción                                                      |
| ------ | --------------------- | ----- | ---------------------------------------------------------------- |
| GET    | `/api/products`       | Todos | Productos activos. Con `?includeInactive=true`, también los inactivos. |
| GET    | `/api/products/{id}`  | Todos | Consultar un producto.                                           |
| POST   | `/api/products`       | ADMIN | Crear producto. Acepta `initialStock` (genera un movimiento de entrada). |
| PUT    | `/api/products/{id}`  | ADMIN | Editar producto. No modifica el inventario.                      |
| DELETE | `/api/products/{id}`  | ADMIN | Desactivar producto (no se borra).                               |

Validaciones al crear o editar:

* La categoría y la unidad existen, están activas y **corresponden entre sí** (Manta → `#`, Tela → metro).
* `purchasePrice` igual a `unitPrice` (si no se envía, se toma `unitPrice`).
* En mantas, `minimumStock` e `initialStock` son enteros.
* No se cambia la unidad de un producto que ya tiene existencias o movimientos.
* `name` hasta 100 caracteres y **único** (sin distinguir mayúsculas ni espacios sobrantes; incluye productos desactivados) → si se repite, `409`.
* `barcode` hasta 50 y único.

## Categorías — `/api/product-categories`

| Método | Ruta                            | Rol   | Descripción             |
| ------ | ------------------------------- | ----- | ----------------------- |
| GET    | `/api/product-categories`       | Todos | Listar categorías.      |
| GET    | `/api/product-categories/{id}`  | Todos | Consultar una categoría.|
| POST   | `/api/product-categories`       | ADMIN | Crear categoría.        |
| PUT    | `/api/product-categories/{id}`  | ADMIN | Editar categoría.       |
| DELETE | `/api/product-categories/{id}`  | ADMIN | Eliminar categoría.     |

Categorías iniciales (creadas al arrancar): **Tela** y **Manta**.

## Unidades — `/api/unit`

| Método | Ruta              | Rol   | Descripción          |
| ------ | ----------------- | ----- | -------------------- |
| GET    | `/api/unit`       | Todos | Listar unidades.     |
| GET    | `/api/unit/{id}`  | Todos | Consultar una unidad.|
| POST   | `/api/unit`       | ADMIN | Crear unidad.        |
| PUT    | `/api/unit/{id}`  | ADMIN | Editar unidad.       |
| DELETE | `/api/unit/{id}`  | ADMIN | Eliminar unidad.     |

Unidades iniciales: **Metro (`m`)** y **Unidad (`#`)**.

## Inventario — `/api/inventory`

| Método | Ruta                          | Rol   | Descripción                                                     |
| ------ | ----------------------------- | ----- | --------------------------------------------------------------- |
| GET    | `/api/inventory`              | Todos | Existencias de todos los productos activos con estado `OK`, `LOW` u `OUT`. |
| GET    | `/api/inventory/{id}`         | Todos | Existencia de un producto.                                      |
| POST   | `/api/inventory/adjustments`  | Todos | Ajuste por conteo físico: registra la diferencia como entrada o salida, con el usuario que lo hizo. El motivo (`reason`) es obligatorio. |

## Movimientos de inventario — `/api/inventory-movements`

| Método | Ruta                               | Rol   | Descripción                                         |
| ------ | ---------------------------------- | ----- | --------------------------------------------------- |
| GET    | `/api/inventory-movements`         | Todos | Historial, del más reciente al más antiguo. Filtros opcionales y combinables: `productId`, `type` (`IN`/`OUT`), `source` (`SALE`, `PURCHASE`, `PRODUCTION`, `ORDER`, `ADJUSTMENT`), `username`, `from` y `to` (`AAAA-MM-DD`, incluidas). Valores inválidos → 400. |
| GET    | `/api/inventory-movements/{id}`    | Todos | Consultar un movimiento.                            |
| POST   | `/api/inventory-movements`         | ADMIN | Movimiento manual.                                  |

## Ventas — `/api/sales`

| Método | Ruta               | Rol   | Descripción                                                              |
| ------ | ------------------ | ----- | ------------------------------------------------------------------------ |
| POST   | `/api/sales`       | Todos | Registrar venta. El backend recalcula precios y total, y descuenta el inventario. Campos opcionales: `clientRequestId` (UUID; si ya existe una venta con ese identificador se devuelve la existente, sin duplicar) y `offlineCreatedAt` (hora real de una venta hecha sin conexión, con zona horaria). |
| GET    | `/api/sales`       | ADMIN | Historial de ventas con montos.                                          |
| GET    | `/api/sales/{id}`  | ADMIN | Detalle de una venta.                                                    |
| PUT    | `/api/sales/{id}`  | ADMIN | Rechazado: las ventas no se modifican después de registrarse.           |
| DELETE | `/api/sales/{id}`  | ADMIN | Rechazado: las ventas no se eliminan.                                    |

## Compras — `/api/purchases`

| Método | Ruta                   | Rol   | Descripción                                                          |
| ------ | ---------------------- | ----- | -------------------------------------------------------------------- |
| POST   | `/api/purchases`       | ADMIN | Registrar compra. El backend recalcula el total y lo compara con el recibido. Acepta `clientRequestId` (no duplica reintentos). |
| GET    | `/api/purchases`       | ADMIN | Historial de compras.                                                |
| GET    | `/api/purchases/{id}`  | ADMIN | Detalle de una compra.                                               |
| PUT / DELETE | `/api/purchases/{id}` | ADMIN | Rechazados: las compras no se modifican ni eliminan.          |

## Producción — `/api/productions`

| Método | Ruta                     | Rol   | Descripción                                                          |
| ------ | ------------------------ | ----- | -------------------------------------------------------------------- |
| POST   | `/api/productions`       | Todos | Registrar producción: descuenta tela y suma mantas en una sola transacción. Acepta `clientRequestId`. |
| GET    | `/api/productions`       | Todos | Historial de producción.                                             |
| GET    | `/api/productions/{id}`  | Todos | Detalle.                                                             |
| PUT / DELETE | `/api/productions/{id}` | Todos | Rechazados: la producción no se modifica ni elimina.         |

## Mantas personalizadas — `/api/orders`

| Método | Ruta                          | Rol   | Descripción                                                         |
| ------ | ----------------------------- | ----- | ------------------------------------------------------------------- |
| GET    | `/api/orders`                 | Todos | Listar. Filtro opcional `?status=PAID` (pagadas) o `?status=PENDING` (con saldo).          |
| GET    | `/api/orders/{id}`            | Todos | Detalle con pagos.                                                  |
| POST   | `/api/orders`                 | Todos | Registrar: nombre, cliente, valor, abono inicial y tela usada (se descuenta del inventario). Acepta `clientRequestId`. |
| POST   | `/api/orders/{id}/payments`   | Todos | Registrar un pago. No puede superar el saldo pendiente. Acepta `clientRequestId` (un pago reenviado no se cobra dos veces). |
| PUT / DELETE | `/api/orders/{id}`      | Todos | Rechazados: las personalizadas no se modifican ni eliminan.         |

La manta personalizada se entrega al cliente: **no** aumenta el inventario de mantas terminadas.

---

# 5. Métodos HTTP

| Método | Uso                                               |
| ------ | ------------------------------------------------- |
| GET    | Consultar información                             |
| POST   | Crear registros                                   |
| PUT    | Actualizar registros                              |
| DELETE | Desactivar o eliminar registros según corresponda |

Los métodos disponibles dependen de cada recurso.

---

# 6. Códigos de respuesta

La API utiliza códigos HTTP para indicar el resultado de las operaciones.

| Código | Significado                     |
| ------ | ------------------------------- |
| 200    | Solicitud exitosa               |
| 201    | Recurso creado                  |
| 204    | Operación exitosa sin contenido |
| 400    | Solicitud inválida              |
| 401    | No autenticado                  |
| 403    | Sin permisos                    |
| 404    | Recurso no encontrado           |
| 405    | Método no permitido             |
| 409    | Conflicto de información (duplicado o choque con un registro existente; se puede reintentar) |
| 413    | Archivo demasiado grande (foto) |
| 429    | Demasiados intentos de inicio de sesión |
| 500    | Error interno del servidor      |

Un JSON mal formado, un tipo de dato incorrecto o un identificador o fecha mal escritos en la dirección (`/api/products/abc`, `?from=ayer`) responden `400` con un mensaje claro; una ruta que no existe, `404`; un método no permitido, `405`. Nunca `500`.

**Operaciones sin duplicados.** Ventas, compras, producción, personalizadas y pagos aceptan `clientRequestId` (UUID que genera el dispositivo). Si llega una operación con un identificador ya registrado, el backend devuelve la existente en lugar de repetirla. La interfaz conserva el mismo identificador mientras el formulario está abierto, así que pulsar "Guardar" otra vez tras un corte de conexión es seguro.

---

# 7. Seguridad

Las operaciones protegidas de la API requieren autenticación.

El frontend envía el token JWT mediante el encabezado:

Authorization: Bearer <token>

La autorización depende del rol asignado al usuario.

Roles: `ADMIN` (administrador) y `SELLER` (vendedor).

* Sin token o con token vencido → `401`: el frontend vuelve al inicio de sesión.
* Con token pero sin permiso → `403`.

La columna **Rol** de la sección 4 resume los permisos; la matriz completa está en `02-reglas-negocio.md` (RN-24A) y está verificada por la prueba `RolePermissionsTest`.

**CORS:** los orígenes permitidos se configuran con `CORS_ALLOWED_ORIGINS` (por defecto, cualquier puerto de `localhost`).

**Swagger** (`/swagger-ui`) está disponible en desarrollo y desactivado en el perfil `prod`.

---

# 8. Validaciones

Las validaciones se realizan principalmente en el backend.

Entre las validaciones se encuentran:

* Campos obligatorios.
* Tipos de datos.
* Existencia de recursos relacionados.
* Estado activo de los recursos.
* Valores válidos.
* Reglas de negocio.
* Permisos del usuario.
* Restricciones de duplicidad.

Por ejemplo, al crear un producto se valida que la categoría y la unidad existan y se encuentren activas.

También se valida que el código de barras no esté registrado en otro producto.

---

# 9. Manejo de errores

Cuando una operación no puede realizarse, el backend proporciona una razón específica del fallo.

El frontend debe utilizar esta información para mostrar al usuario un mensaje claro sobre lo ocurrido.

No se debe reemplazar innecesariamente una razón específica del backend por mensajes genéricos como:

"No fue posible realizar la acción."

El objetivo es que el usuario pueda conocer la causa real del problema y actuar en consecuencia.

---

# 10. Formato de datos

Las solicitudes y respuestas de la API utilizan JSON cuando corresponde.

Ejemplo de información de un producto:

```json
{
  "id": "uuid",
  "categoryId": "uuid",
  "internalCode": "PROD-00001",
  "barcode": "123456789",
  "unitId": "uuid",
  "name": "Manta",
  "purchasePrice": 50000,
  "unitPrice": 50000,
  "wholesalePrice": 45000,
  "minimumWholesaleQuantity": 10,
  "minimumStock": 5,
  "active": true
}
```

La estructura exacta de cada respuesta depende del recurso consultado.

---

# 11. Pruebas de la API

Los endpoints se validan desde el frontend mediante las operaciones correspondientes de cada funcionalidad.

Por decisión del proyecto, las pruebas funcionales de los endpoints no se realizan mediante Swagger como herramienta principal.

El frontend permite comprobar el comportamiento real de la API dentro del flujo de uso de la aplicación.

Además, el backend tiene pruebas automatizadas (`./mvnw test`) que llaman a la API con cada rol y verifican los permisos (`RolePermissionsTest`), y pruebas de servicio para ventas concurrentes y reglas de producto. Ver la sección **Pruebas** del README.

---

# 12. Versionado

Actualmente los endpoints utilizan el prefijo:

/api

No se utiliza actualmente `/api/v1`.

Si en el futuro se requiere versionar formalmente la API debido a cambios incompatibles, se podrá establecer una nueva versión de los endpoints.

Cualquier cambio de este tipo deberá reflejarse en este documento.

---

# 13. Documentación de endpoints

Este documento se actualizará conforme se incorporen nuevos endpoints y módulos.

Las rutas descritas deben corresponder a endpoints realmente implementados en el backend.

No se deben documentar rutas futuras como si ya estuvieran disponibles.

---

# 14. Observaciones

La API constituye el punto de comunicación entre el frontend y el backend de Mantas Guajiras.

El backend mantiene la responsabilidad sobre las reglas de negocio, validaciones, seguridad y operaciones relacionadas con los datos.

La documentación deberá mantenerse sincronizada con la implementación real del sistema para evitar diferencias entre los endpoints documentados y los disponibles.
