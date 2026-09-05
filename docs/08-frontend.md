# Documento del Frontend

| Campo                    | Valor                |
| ------------------------ | -------------------- |
| **Proyecto**             | Mantas Guajiras      |
| **Documento**            | Frontend             |
| **Código**               | DOC-08               |
| **Versión**              | v0.4.0               |
| **Estado**               | En desarrollo        |
| **Responsable**          | Equipo de Desarrollo |
| **Última actualización** | 05/09/2026           |

---

## 1. Introducción

El frontend de **Mantas Guajiras** corresponde a la interfaz web mediante la cual los usuarios interactúan con las funcionalidades del sistema.

La aplicación está desarrollada como una **aplicación web responsive**, diseñada para funcionar desde:

* Computadores de escritorio.
* Computadores portátiles.
* Tablets.
* Teléfonos celulares.

La aplicación utiliza una única interfaz web adaptable al tamaño de pantalla. No se contempla una aplicación móvil nativa independiente para Android o iOS.

El frontend se comunica con el backend mediante una API REST desarrollada con Spring Boot.

La arquitectura general es:

```text
                 Mantas Guajiras
                        │
             ┌──────────┴──────────┐
             │                     │
        Computador              Celular
             │                     │
             └──────────┬──────────┘
                        │
                 Aplicación Web
                  React + Vite
                        │
                     REST API
                        │
                  Spring Boot
                        │
                   PostgreSQL
```

---

# 2. Tecnologías

El frontend utiliza actualmente las siguientes tecnologías:

| Tecnología       | Uso                                       |
| ---------------- | ----------------------------------------- |
| **React**        | Construcción de la interfaz de usuario    |
| **Vite**         | Herramienta de desarrollo y construcción  |
| **JavaScript**   | Lenguaje principal del frontend           |
| **JSX**          | Definición de componentes React           |
| **CSS**          | Estilos y diseño responsive               |
| **React Router** | Navegación entre páginas                  |
| **Fetch API**    | Comunicación con el backend               |
| **LocalStorage** | Almacenamiento del token de autenticación |

No se utiliza TypeScript actualmente.

No se utiliza Tailwind CSS actualmente.

Los estilos se manejan mediante archivos CSS propios del proyecto.

---

# 3. Estructura del frontend

La estructura actual del frontend se organiza de la siguiente manera:

```text
frontend/
│
├── public/
│
├── src/
│   ├── assets/
│   │
│   ├── components/
│   │
│   ├── pages/
│   │
│   ├── services/
│   │
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

## 3.1 `index.html`

Es el documento HTML principal generado por Vite.

Actúa como punto de entrada de la aplicación web y contiene el elemento raíz sobre el cual React monta la aplicación.

Flujo:

```text
index.html
     │
     ▼
main.jsx
     │
     ▼
App.jsx
```

---

## 3.2 `main.jsx`

Es el punto de entrada de React.

Se encarga de montar el componente principal de la aplicación sobre el elemento raíz definido en `index.html`.

Flujo:

```text
index.html
     │
     ▼
main.jsx
     │
     ▼
<App />
```

---

## 3.3 `App.jsx`

Es el componente principal de la aplicación.

Actualmente se encarga principalmente de configurar el sistema de rutas mediante React Router.

La estructura general es:

```text
App
 │
 ├── /login
 │      └── LoginPage
 │
 ├── /register
 │      └── RegisterPage
 │
 └── MainLayout
        │
        ├── Sidebar
        │
        └── Página actual
```

---

## 3.4 `index.css`

Contiene los estilos CSS globales de la aplicación cuando corresponde.

Su función es establecer reglas generales que pueden afectar a toda la aplicación, como estilos base del documento, tipografía, elementos HTML globales y comportamiento general de los elementos.

Debe mantenerse separado de los estilos específicos de cada página o componente cuando la regla solamente corresponda a una sección concreta.

---

## 3.5 `App.css`

Contiene los estilos principales utilizados por la aplicación y sus diferentes páginas y componentes.

Actualmente incluye estilos relacionados con:

* Layout general.
* Sidebar.
* Contenido principal.
* Página de productos.
* Punto de venta.
* Carrito.
* Modales.
* Formularios.
* Producción.
* Diseño responsive.

---

# 4. Arquitectura de navegación

La navegación se implementa mediante **React Router**.

La aplicación utiliza rutas independientes para las diferentes funcionalidades.

Actualmente se encuentran definidas rutas como:

```text
/login
/register
/sale/new
/products
/production
```

Las páginas de autenticación se encuentran fuera del layout principal.

Las páginas funcionales utilizan `MainLayout`.

La estructura es:

```text
BrowserRouter
     │
     └── Routes
          │
          ├── /login
          │     └── LoginPage
          │
          ├── /register
          │     └── RegisterPage
          │
          └── MainLayout
                 │
                 ├── Sidebar
                 │
                 └── Outlet
                        │
                        ├── NewSalePage
                        ├── ProductsPage
                        └── ProductionPage
```

---

# 5. Layout principal

El layout principal está compuesto por:

```text
MainLayout
│
├── Sidebar
│
└── app-content
      │
      └── Outlet
```

`MainLayout` permite mantener una estructura común para las páginas internas de la aplicación.

El componente `Sidebar` proporciona la navegación principal.

El componente `Outlet` permite mostrar la página correspondiente a la ruta actual.

---

# 6. Sidebar

El `Sidebar` funciona como elemento principal de navegación dentro de la aplicación.

Su objetivo es permitir acceder a las diferentes funcionalidades disponibles para el usuario.

La navegación debe adaptarse a los diferentes tamaños de pantalla.

En dispositivos con pantallas pequeñas, el diseño puede reorganizar los elementos para conservar la funcionalidad y evitar que el contenido quede inutilizable.

---

# 7. Páginas actuales

## 7.1 LoginPage

Ruta:

```text
/login
```

Permite iniciar sesión en el sistema.

La autenticación se realiza contra el backend y utiliza el mecanismo de autenticación basado en JWT.

---

## 7.2 RegisterPage

Ruta:

```text
/register
```

Permite registrar nuevos usuarios mediante el formulario correspondiente.

La validación definitiva de los datos corresponde al backend.

El frontend debe mostrar al usuario los errores específicos devueltos por el backend cuando una operación no puede realizarse.

---

## 7.3 ProductsPage

Ruta:

```text
/products
```

Permite administrar los productos disponibles en el sistema.

Actualmente permite:

* Consultar productos.
* Crear productos.
* Editar productos.
* Mostrar información básica del producto.

La página utiliza `ProductForm` para las operaciones de creación y edición.

La información se obtiene mediante `ProductService`.

---

## 7.4 NewSalePage

Ruta:

```text
/sale/new
```

Corresponde al punto de venta.

Su objetivo es permitir al vendedor seleccionar productos y construir una venta mediante un carrito.

El diseño busca mantener una experiencia sencilla y rápida tanto en computador como en dispositivos móviles.

---

# 8. Punto de venta

El punto de venta está estructurado conceptualmente de la siguiente forma:

```text
NewSalePage
│
├── Encabezado
│
├── Productos
│   ├── Manta
│   └── Tela
│
└── Carrito
    ├── Nombre
    ├── Cantidad
    └── Total
```

Las categorías principales se muestran en el orden:

1. **Manta**
2. **Tela**

Este orden corresponde a la presentación definida para el punto de venta.

---

# 9. Carrito de venta

El carrito permite agregar productos seleccionados para la venta.

La información principal del carrito se presenta mediante tres columnas:

| Columna      | Información              |
| ------------ | ------------------------ |
| **Nombre**   | Producto seleccionado    |
| **Cantidad** | Cantidad del producto    |
| **Total**    | Precio total de la línea |

El total de cada producto se calcula mediante:

```text
Total de línea =
Precio unitario × Cantidad
```

El total general del carrito se actualiza automáticamente cuando cambia la cantidad o el precio de un producto.

---

# 10. Precio especial por producto

El sistema permite establecer un precio personalizado para productos específicos dentro de una venta.

El precio especial:

* Se aplica únicamente a esa venta.
* Puede utilizarse incluso para una sola unidad.
* No depende exclusivamente de la cantidad.
* Permite manejar descuentos o acuerdos particulares.
* No modifica permanentemente el precio base del producto.

Al seleccionar un producto del carrito se puede abrir el popup correspondiente para establecer el precio especial.

La operación debe mantener el precio original del producto como referencia para calcular el descuento cuando corresponda.

---

# 11. Popup de precio especial

El popup de precio especial se utiliza para modificar el precio aplicado al producto dentro de la venta actual.

Características principales:

* Se muestra centrado.
* Utiliza un fondo blanco para facilitar la lectura.
* Mantiene visible el contenido de la página detrás del popup.
* El fondo se presenta con desenfoque.
* Puede cerrarse mediante el botón de cierre.
* Puede cerrarse mediante la tecla `Esc`.

El precio modificado afecta únicamente la venta actual.

---

# 12. Productos y unidades

Los productos manejados por el sistema tienen una categoría y una unidad asociada.

La unidad debe corresponder automáticamente al tipo de producto.

| Tipo de producto | Unidad                |
| ---------------- | --------------------- |
| **Manta**        | Unidad completa (`#`) |
| **Tela**         | Metro                 |

Las mantas se manejan mediante cantidades enteras.

Las telas pueden manejar cantidades decimales debido a que se comercializan por metros.

---

# 13. Administración de productos

La administración de productos permite al usuario autorizado crear y modificar la información correspondiente.

Entre los datos manejados actualmente se encuentran:

* Nombre.
* Código interno.
* Código de barras.
* Categoría.
* Unidad.
* Precio de compra.
* Precio unitario.
* Precio mayorista.
* Cantidad mínima para precio mayorista.
* Stock mínimo.
* Estado activo.

Sin embargo, desde la perspectiva actual del frontend, el concepto principal de precio es el precio base del producto.

El precio aplicado específicamente a una venta puede modificarse desde el proceso de venta mediante el mecanismo de precio especial.

---

# 14. Precio de compra y precio unitario

Dentro del modelo funcional actual del proyecto, el precio de compra y el precio unitario representan el mismo concepto de precio base del producto.

El frontend no necesita presentar opciones independientes relacionadas con productos "comprables" o "fabricables".

La clasificación funcional del producto se determina principalmente por su categoría y unidad.

---

# 15. Producción

Ruta:

```text
/production
```

La página de producción permite registrar procesos mediante los cuales una cantidad de tela se utiliza para producir una cantidad de mantas.

La operación contempla:

```text
Tela
  │
  │ consumo
  ▼
Producción
  │
  │ genera
  ▼
Mantas
```

Las cantidades utilizadas dependen del tipo de producto:

| Producto  | Tipo de cantidad    |
| --------- | ------------------- |
| **Tela**  | Decimal, en metros  |
| **Manta** | Entera, en unidades |

La producción utiliza productos existentes previamente registrados en el sistema.

---

# 16. Comunicación con el backend

El frontend se comunica con el backend mediante solicitudes HTTP utilizando `fetch`.

Las operaciones de acceso al backend se concentran en servicios ubicados en:

```text
src/services/
```

Por ejemplo:

```text
src/
└── services/
      └── ProductService.js
```

Los servicios se encargan de:

* Construir las solicitudes.
* Incluir el token de autenticación cuando corresponde.
* Enviar datos al backend.
* Procesar las respuestas.
* Informar errores.

---

# 17. ProductService

`ProductService` centraliza las operaciones relacionadas con productos.

Actualmente contiene operaciones como:

```text
findAll()
findById(id)
create(product)
update(id, product)
delete(id)
findCategories()
findUnits()
```

Estas operaciones utilizan los endpoints actualmente disponibles en el backend.

Ejemplos de rutas utilizadas:

```text
GET    /api/products
GET    /api/products/{id}
POST   /api/products
PUT    /api/products/{id}
DELETE /api/products/{id}

GET    /api/product-categories
GET    /api/unit
```

El proyecto actualmente no utiliza una estructura `/api/v1`.

---

# 18. Autenticación

La autenticación utiliza JWT.

El token obtenido durante el inicio de sesión se almacena actualmente en `localStorage`.

Cuando un servicio necesita realizar una operación autenticada, obtiene el token y lo envía mediante el encabezado:

```text
Authorization: Bearer <token>
```

El frontend no debe asumir que una operación fue exitosa solamente porque se realizó la solicitud.

Debe comprobar la respuesta HTTP y procesar correctamente los errores enviados por el backend.

---

# 19. Manejo de errores

Uno de los principios del frontend es proporcionar mensajes claros al usuario.

Cuando una operación falla, el frontend debe mostrar, siempre que sea posible, la razón específica proporcionada por el backend.

Ejemplos:

```text
"La categoría seleccionada está inactiva."

"Ya existe un producto con ese código de barras."

"Producto no encontrado."

"La unidad seleccionada está inactiva."
```

Se debe evitar presentar únicamente mensajes genéricos como:

```text
"No fue posible realizar la acción."
```

cuando el backend proporciona información más específica.

---

# 20. Formularios

Los formularios del frontend deben:

* Validar los datos básicos antes de enviarlos.
* Evitar enviar información evidentemente incorrecta.
* Mostrar errores de forma clara.
* Permitir cancelar una operación.
* Mostrar estados de carga cuando corresponda.
* Informar correctamente cuando una operación fue exitosa o falló.

La validación del frontend complementa, pero no reemplaza, la validación realizada por el backend.

---

# 21. Modales

Los modales se utilizan para operaciones que no requieren abandonar la página actual.

Actualmente se utilizan para operaciones como:

* Crear producto.
* Editar producto.
* Configurar un precio especial en una venta.

El diseño de los modales busca mantener el contexto de la página original.

Características visuales:

```text
Página principal
       │
       ▼
Fondo visible + desenfoque
       │
       ▼
Modal blanco
       │
       ▼
Formulario / contenido
```

---

# 22. Diseño responsive

El frontend debe funcionar correctamente en diferentes tamaños de pantalla.

El diseño debe considerar:

* Computadores de escritorio.
* Portátiles.
* Tablets.
* Teléfonos celulares.

No existe una versión independiente del frontend para dispositivos móviles.

La misma aplicación web debe adaptar:

* Navegación.
* Distribución de contenido.
* Formularios.
* Carrito.
* Modales.
* Listados.
* Botones.
* Espaciado.

según el tamaño disponible.

---

# 23. Diseño visual

La interfaz busca mantener una estética:

* Cálida.
* Limpia.
* Sencilla.
* Fácil de utilizar.
* Adecuada para operaciones frecuentes.

La aplicación utiliza una paleta visual basada principalmente en tonos cálidos y neutros.

El diseño evita elementos visuales innecesarios que puedan dificultar el uso del sistema.

No se utilizan emojis como parte de la interfaz funcional.

---

# 24. Punto de venta y dispositivos móviles

El punto de venta debe ser usable tanto en computador como en celular.

En computador se aprovecha un espacio de pantalla mayor para mostrar simultáneamente productos y carrito.

En dispositivos pequeños, los elementos deben reorganizarse para mantener:

* Lectura clara.
* Controles accesibles.
* Botones utilizables.
* Carrito visible.
* Modales correctamente centrados.
* Campos de formulario accesibles.

El objetivo es que el usuario pueda realizar una venta desde cualquiera de los dispositivos compatibles sin necesitar una aplicación diferente.

---

# 25. Variables de entorno

La dirección base de la API se obtiene mediante una variable de entorno de Vite:

```text
VITE_API_URL
```

Los servicios utilizan esta variable para construir las solicitudes al backend.

Ejemplo conceptual:

```text
VITE_API_URL
      │
      ▼
ProductService
      │
      ▼
${API_URL}/api/products
```

La variable debe configurarse de acuerdo con el entorno donde se ejecute el frontend.

---

# 26. Inventario

El inventario representa las existencias reales de los productos.

El frontend no debe interpretar `minimumStock` como la cantidad disponible.

La diferencia conceptual es:

| Concepto         | Función                                                   |
| ---------------- | --------------------------------------------------------- |
| **Stock actual** | Cantidad existente del producto                           |
| **Stock mínimo** | Umbral utilizado para identificar necesidad de reposición |

El stock actual pertenece al módulo de inventario.

Las operaciones que modifican existencias deben quedar registradas mediante movimientos de inventario.

---

# 27. Integración con inventario

Las operaciones que pueden afectar el inventario incluyen:

```text
Compra
   │
   └── aumenta stock

Venta
   │
   └── disminuye stock

Producción - entrada
   │
   └── aumenta stock de mantas

Producción - consumo
   │
   └── disminuye stock de tela

Ajuste
   │
   └── modifica manualmente el stock
```

La lógica definitiva de modificación del inventario corresponde al backend.

El frontend debe enviar las operaciones correspondientes y mostrar al usuario el resultado.

---

# 28. Pruebas del frontend

Las funcionalidades del sistema se validan principalmente mediante el uso real de la aplicación frontend.

No se utiliza Swagger como mecanismo principal para probar los endpoints.

El flujo esperado es:

```text
Usuario
   │
   ▼
Frontend
   │
   ▼
REST API
   │
   ▼
Backend
   │
   ▼
Base de datos
```

Las pruebas deben verificar tanto el funcionamiento técnico como el comportamiento visible para el usuario.

Se deben comprobar especialmente:

* Formularios.
* Validaciones.
* Mensajes de error.
* Navegación.
* Creación y edición de productos.
* Ventas.
* Carrito.
* Precios especiales.
* Producción.
* Comportamiento responsive.

---

# 29. Principios de desarrollo del frontend

El desarrollo del frontend sigue los siguientes principios:

### 29.1 Mantener la arquitectura existente

No se realizan refactorizaciones innecesarias cuando la estructura actual funciona correctamente.

### 29.2 Cambios controlados

Las funcionalidades se incorporan progresivamente para facilitar las pruebas y detectar errores.

### 29.3 Reutilización

Los componentes y servicios reutilizables deben mantenerse centralizados cuando sea conveniente.

### 29.4 Separación de responsabilidades

Las páginas, componentes y servicios deben mantener responsabilidades diferenciadas.

```text
pages
  │
  └── composición de pantallas

components
  │
  └── elementos reutilizables

services
  │
  └── comunicación con backend
```

### 29.5 Mensajes claros

Los errores deben explicar al usuario qué ocurrió y, cuando sea posible, por qué ocurrió.

### 29.6 Responsive desde el diseño

La compatibilidad con computadores y dispositivos móviles forma parte del diseño actual del sistema y no corresponde a una funcionalidad futura.

---

# 30. Estado actual del frontend

Actualmente el frontend se encuentra en desarrollo y cuenta con una base funcional para:

* Autenticación.
* Registro.
* Layout principal.
* Navegación mediante React Router.
* Administración de productos.
* Creación de productos.
* Edición de productos.
* Consulta de productos.
* Punto de venta.
* Carrito de venta.
* Configuración de precios especiales por producto.
* Producción.

La implementación continúa ampliándose conforme se integran las funcionalidades restantes del backend y frontend.

---

# 31. Funcionalidades pendientes

Las funcionalidades que todavía requieran implementación o integración deberán incorporarse progresivamente.

Entre ellas pueden encontrarse:

* Integración completa de inventario.
* Ajustes manuales de inventario.
* Visualización detallada del stock.
* Integración completa entre movimientos de inventario y las operaciones correspondientes.
* Funcionalidades administrativas adicionales.
* Mejoras adicionales de responsive y experiencia de usuario.
* Pruebas funcionales completas de todos los módulos.

Las funcionalidades pendientes no deben documentarse como implementadas hasta que hayan sido desarrolladas y verificadas.

---

# 32. Relación entre frontend y backend

La aplicación completa sigue la siguiente arquitectura:

```text
                    USUARIO
                       │
                       ▼
              Aplicación Web
             React + Vite + CSS
                       │
                       ▼
                 React Router
                       │
                       ▼
                   Services
                       │
                       ▼
                  REST API
                       │
                       ▼
               Spring Boot
                       │
             ┌─────────┴─────────┐
             │                   │
          Servicios            JPA
             │                   │
             └─────────┬─────────┘
                       │
                       ▼
                   PostgreSQL
```

El frontend se encarga principalmente de la interacción con el usuario y la presentación de la información.

El backend se encarga de:

* Reglas de negocio.
* Validaciones definitivas.
* Autenticación.
* Persistencia.
* Gestión del inventario.
* Operaciones transaccionales.

---

# 33. Consideraciones de seguridad

El frontend no debe considerarse responsable de garantizar por sí mismo la seguridad de las operaciones.

Las restricciones importantes deben ser verificadas nuevamente por el backend.

El frontend debe:

* Enviar el token cuando sea necesario.
* Manejar respuestas de autenticación.
* No confiar únicamente en validaciones del cliente.
* Mostrar correctamente los errores de autorización o autenticación.

---

# 34. Compatibilidad

El frontend está diseñado para ejecutarse desde navegadores web modernos.

La aplicación debe adaptarse a diferentes resoluciones y tamaños de pantalla.

La compatibilidad esperada incluye:

```text
┌─────────────────────────────────────┐
│        Mantas Guajiras Web          │
├─────────────────────────────────────┤
│                                     │
│  💻 PC        💻 Portátil           │
│                                     │
│  📱 Celular   📱 Tablet             │
│                                     │
└─────────────────────────────────────┘
```

La representación anterior describe los dispositivos objetivo; los emojis no forman parte de la interfaz real de la aplicación.

---

# 35. Conclusión

El frontend de **Mantas Guajiras** constituye una aplicación web desarrollada con React y Vite, conectada al backend mediante una API REST.

Su arquitectura está organizada alrededor de páginas, componentes reutilizables y servicios de comunicación con el backend.

El sistema está diseñado desde el inicio para funcionar tanto en computadores como en dispositivos móviles mediante una única interfaz responsive.

El desarrollo continuará de manera progresiva, priorizando:

1. Funcionalidad.
2. Claridad de uso.
3. Integración correcta con el backend.
4. Mensajes de error específicos.
5. Diseño responsive.
6. Mantenimiento de la arquitectura existente.
7. Cambios controlados y verificables.

