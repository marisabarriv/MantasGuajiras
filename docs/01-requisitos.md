# Documento de Requisitos

| Campo                    | Valor                  |
| ------------------------ | ---------------------- |
| **Proyecto**             | Mantas Guajiras        |
| **Documento**            | Requisitos del Sistema |
| **Código**               | DOC-01                 |
| **Versión**              | v0.4.0                 |
| **Estado**               | En desarrollo          |
| **Responsable**          | Equipo de Desarrollo   |
| **Última actualización** | 05/09/2026             |

---

## Control de versiones

| Versión | Fecha      | Descripción                                  | Responsable          |
| ------- | ---------- | -------------------------------------------- | -------------------- |
| v0.1.0  | 29/07/2026 | Creación inicial del documento.              | Equipo de Desarrollo |
| v0.4.0  | 05/09/2026 | Actualización de los requisitos del sistema. | Equipo de Desarrollo |

---

# 1. Introducción

Este documento define los requisitos funcionales y no funcionales del sistema Mantas Guajiras.

Los requisitos sirven como referencia para el diseño, desarrollo, integración y validación del sistema.

---

# 2. Requisitos funcionales

## RF-01. Autenticación

El sistema deberá permitir que los usuarios autenticados accedan a las funcionalidades autorizadas mediante el mecanismo de autenticación implementado.

Actualmente la autenticación utiliza JWT.

---

## RF-02. Gestión de usuarios

El sistema deberá permitir al usuario administrador gestionar los usuarios de acuerdo con los permisos establecidos.

Las operaciones podrán incluir:

* Crear usuarios.
* Modificar usuarios.
* Activar o desactivar usuarios.
* Asignar roles.

---

## RF-03. Gestión de productos

El sistema deberá permitir:

* Crear productos.
* Consultar productos.
* Consultar un producto específico.
* Editar productos.
* Desactivar productos.
* Registrar código interno.
* Registrar código de barras cuando corresponda.
* Asociar una categoría.
* Asociar una unidad.
* Registrar el precio correspondiente.
* Definir el stock mínimo.

---

## RF-04. Gestión de categorías

El sistema deberá permitir administrar y consultar las categorías de productos utilizadas por el sistema.

Actualmente las categorías permiten diferenciar, entre otros, productos de tipo:

* Manta.
* Tela.

---

## RF-05. Gestión de unidades

El sistema deberá utilizar unidades asociadas a los productos.

Las unidades deben corresponder al tipo de producto.

| Tipo de producto | Unidad       |
| ---------------- | ------------ |
| **Manta**        | Unidad (`#`) |
| **Tela**         | Metro        |

Las mantas se manejan mediante cantidades enteras.

Las telas pueden manejar cantidades decimales.

---

## RF-06. Inventario

El sistema deberá mantener el stock real de cada producto.

El inventario deberá permitir registrar movimientos derivados de operaciones como:

* Compras.
* Ventas.
* Producción.
* Ajustes administrativos.

El stock actual no corresponde al valor de `minimumStock`.

---

## RF-07. Ajustes de inventario

El administrador deberá poder realizar ajustes manuales de inventario.

Cada ajuste deberá generar el movimiento de inventario correspondiente.

---

## RF-08. Ventas

El sistema deberá permitir:

* Crear una venta.
* Agregar productos a una venta.
* Definir cantidades.
* Calcular totales.
* Registrar el precio aplicado.
* Descontar las existencias correspondientes.
* Registrar la información de la operación.

---

## RF-09. Precios especiales en ventas

El sistema deberá permitir modificar manualmente el precio de un producto dentro de una venta específica.

El precio especial:

* Se aplicará únicamente a esa venta.
* Podrá utilizarse incluso para una sola unidad.
* No dependerá obligatoriamente de la cantidad.
* No modificará el precio base almacenado del producto.
* Permitirá calcular el descuento correspondiente cuando aplique.

---

## RF-10. Venta de telas

El sistema deberá permitir vender telas utilizando metros como unidad.

La cantidad podrá ser decimal.

La venta deberá descontar del inventario la cantidad correspondiente.

---

## RF-11. Producción

El sistema deberá permitir registrar procesos de producción mediante los cuales se utiliza tela para producir mantas.

La producción deberá registrar:

* Producto de tela utilizado.
* Cantidad de tela utilizada por unidad.
* Producto de manta generado.
* Cantidad de mantas producidas.

Al confirmar la producción:

* Se disminuirá el inventario de tela.
* Se aumentará el inventario de mantas.
* Se registrarán los movimientos correspondientes.

---

## RF-12. Compras

El sistema deberá permitir registrar compras de productos cuando corresponda.

Una compra deberá generar el movimiento de entrada de inventario correspondiente.

---

## RF-13. Pedidos

El sistema deberá permitir gestionar pedidos de acuerdo con las funcionalidades definidas para el módulo.

Los pedidos podrán incorporar información como:

* Cliente.
* Productos.
* Cantidades.
* Precio acordado.
* Estado.
* Observaciones.
* Información relacionada con pagos.

Las funcionalidades específicas deberán mantenerse alineadas con la implementación real del módulo.

---

## RF-14. Pagos

Cuando una funcionalidad de pedido requiera pagos, el sistema deberá permitir registrar los valores correspondientes y mantener el saldo pendiente de acuerdo con las reglas de negocio implementadas.

---

## RF-15. Historial de inventario

El sistema deberá conservar los movimientos de inventario para mantener trazabilidad sobre las operaciones que modifican las existencias.

---

## RF-16. Consulta de información

El sistema deberá permitir consultar la información necesaria para la operación diaria de acuerdo con los permisos del usuario.

---

# 3. Requisitos no funcionales

## RNF-01. Usabilidad

La interfaz deberá ser sencilla, clara e intuitiva.

---

## RNF-02. Responsive

La aplicación deberá funcionar correctamente en:

* Computadores de escritorio.
* Portátiles.
* Tablets.
* Teléfonos celulares.

La aplicación será una única aplicación web responsive.

---

## RNF-03. Rendimiento

Las operaciones comunes deberán ejecutarse en tiempos adecuados para la operación diaria del negocio.

---

## RNF-04. Seguridad

El acceso a las funcionalidades deberá estar protegido mediante autenticación y autorización.

Los permisos definitivos serán controlados por el backend.

---

## RNF-05. Integridad

El sistema no deberá permitir operaciones que generen inconsistencias en la información.

No se permitirá generar inventarios negativos.

---

## RNF-06. Trazabilidad

Las operaciones que modifiquen el inventario deberán quedar registradas mediante movimientos de inventario.

---

## RNF-07. Mantenibilidad

El código deberá mantenerse organizado y dividido según las responsabilidades de cada componente.

No se realizarán refactorizaciones estructurales innecesarias sobre componentes existentes que funcionen correctamente.

---

## RNF-08. Escalabilidad

La arquitectura deberá permitir incorporar nuevas funcionalidades sin modificar innecesariamente los módulos existentes.

---

## RNF-09. Compatibilidad

La aplicación web deberá poder utilizarse mediante navegadores web modernos en los dispositivos objetivo.

---

# 4. Tecnologías

| Componente           | Tecnología      |
| -------------------- | --------------- |
| Frontend             | React           |
| Lenguaje frontend    | JavaScript      |
| Interfaz             | JSX             |
| Estilos              | CSS             |
| Build tool           | Vite            |
| Navegación           | React Router    |
| Comunicación         | Fetch API       |
| Backend              | Java 21         |
| Framework backend    | Spring Boot     |
| Persistencia         | JPA / Hibernate |
| Base de datos        | PostgreSQL      |
| Migraciones          | Flyway          |
| Control de versiones | Git             |
| Repositorio          | GitHub          |

---

# 5. Restricciones

Actualmente:

* No se utiliza TypeScript.
* No se utiliza Tailwind CSS.
* No se utiliza una aplicación móvil nativa.
* Los endpoints se prueban mediante la aplicación frontend y no mediante Swagger.
* La sincronización en la nube no forma parte de la implementación actual.
* El funcionamiento offline con sincronización posterior es una funcionalidad futura.

---

# 6. Criterios de aceptación

El sistema deberá considerarse funcional cuando:

* Los productos puedan administrarse correctamente.
* El inventario refleje las existencias reales.
* Las operaciones de inventario generen movimientos.
* Las ventas puedan registrarse correctamente.
* Los precios especiales se apliquen únicamente a la venta correspondiente.
* La producción modifique correctamente las existencias.
* Los permisos de los usuarios sean respetados.
* La interfaz funcione correctamente en computadores y dispositivos móviles.

---

# 7. Observaciones

Los requisitos podrán ampliarse conforme avance el proyecto.

Todo requisito deberá reflejar el comportamiento real del sistema antes de considerarse implementado.
