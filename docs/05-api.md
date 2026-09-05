# Documento de API

| Campo                    | Valor                    |
| ------------------------ | ------------------------ |
| **Proyecto**             | Mantas Guajiras          |
| **Documento**            | Especificación de la API |
| **Código**               | DOC-05                   |
| **Versión**              | v0.4.0                   |
| **Estado**               | En desarrollo            |
| **Responsable**          | Equipo de Desarrollo     |
| **Última actualización** | 05/09/2026               |

---

## Control de versiones

| Versión | Fecha      | Descripción                                             | Responsable          |
| ------- | ---------- | ------------------------------------------------------- | -------------------- |
| v0.1.0  | 29/07/2026 | Creación inicial del documento.                         | Equipo de Desarrollo |
| v0.4.0  | 05/09/2026 | Actualización de la API según la implementación actual. | Equipo de Desarrollo |

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

## Productos

Los endpoints actualmente implementados para productos son:

GET     /api/products
GET     /api/products/{id}
POST    /api/products
PUT     /api/products/{id}
DELETE  /api/products/{id}

Estas operaciones permiten:

* Consultar productos activos.
* Consultar un producto específico.
* Crear productos.
* Actualizar productos.
* Desactivar productos.

El código interno del producto es generado por el backend.

El código de barras se valida para evitar duplicados.

Las categorías y unidades utilizadas por los productos se consultan mediante endpoints independientes.

---

## Categorías de productos

Endpoint actualmente implementado:


GET     /api/product-categories

Las categorías permiten clasificar los productos.

Actualmente se utilizan categorías correspondientes a:

* Manta.
* Tela.

---

## Unidades

Endpoint actualmente implementado:

GET     /api/unit

Las unidades permiten determinar cómo se maneja la cantidad de un producto.

Actualmente:

* Las mantas utilizan unidades enteras (`#`).
* Las telas utilizan metros.

---

## Inventario

La gestión del inventario se encuentra relacionada con los productos y los movimientos de inventario.

La existencia actual corresponde al valor almacenado en `Inventory.quantity`.

Los movimientos de inventario permiten registrar entradas y salidas producidas por diferentes operaciones del sistema.

---

## Movimientos de inventario

El sistema cuenta con un módulo para gestionar los movimientos de inventario.

Las operaciones relacionadas con movimientos se realizan desde el backend y permiten registrar información como:

* Producto.
* Tipo de movimiento.
* Tipo de origen.
* Identificador del origen.
* Cantidad.
* Observaciones.

Los tipos principales de movimiento son:

IN
OUT

Los movimientos pueden estar relacionados con operaciones como:

* Compra.
* Venta.
* Producción.
* Ajuste.

El acceso directo del vendedor a operaciones genéricas de modificación de inventario no forma parte del flujo normal de ventas.

---

## Producción

La producción utiliza los productos existentes para registrar el consumo de materia prima y la generación de productos terminados.

Una producción puede:

* Consumir tela.
* Generar mantas.
* Registrar los movimientos correspondientes.
* Actualizar el inventario.

Las operaciones de producción se realizan de manera transaccional.

---

## Ventas

El módulo de ventas permite registrar productos, cantidades y precios aplicados a una venta.

El precio utilizado en una venta puede corresponder al precio normal del producto o a un precio especial establecido específicamente para esa compra.

El precio especial se aplica únicamente a la venta correspondiente y no modifica el precio base del producto.

Al completar una venta se genera el movimiento de inventario correspondiente.

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
| 409    | Conflicto de información        |
| 500    | Error interno del servidor      |

---

# 7. Seguridad

Las operaciones protegidas de la API requieren autenticación.

El frontend envía el token JWT mediante el encabezado:

Authorization: Bearer <token>

La autorización depende del rol asignado al usuario.

Actualmente se contemplan principalmente los roles:

* Administrador.
* Vendedor.

Las restricciones de acceso son controladas por el backend.

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
