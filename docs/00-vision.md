# Documento de Visión

| Campo | Valor |
|--------|-------|
| **Proyecto** | Mantas Guajiras |
| **Documento** | Visión del Proyecto |
| **Código** | DOC-00 |
| **Versión** | v0.3.0 |
| **Estado** | En desarrollo |
| **Responsable** | Equipo de Desarrollo |
| **Última actualización** | 14/08/2026 |

---

## Control de versiones

| Versión | Fecha | Descripción | Responsable |
|----------|--------|-------------|-------------|
| v0.1.0 | 29/07/2026 | Creación inicial del documento. | Equipo de Desarrollo |

---

# 1. Introducción

Mantas Guajiras es un sistema de gestión desarrollado para apoyar las operaciones diarias del negocio mediante la digitalización de sus procesos administrativos y operativos.

El sistema busca reemplazar los registros manuales por una plataforma centralizada que permita administrar el inventario, registrar ventas, controlar la producción de mantas y gestionar pedidos personalizados de forma sencilla, organizada y confiable.

Desde su diseño, el sistema prioriza la facilidad de uso para personas con poca experiencia en herramientas tecnológicas, ofreciendo una interfaz intuitiva y procesos claros que faciliten el trabajo diario.

---

# 2. Problema

Actualmente la administración del negocio depende principalmente de registros manuales, lo que dificulta mantener un control preciso de la información y aumenta la posibilidad de errores.

Entre las principales dificultades se encuentran:

- Desconocimiento del inventario real de mantas y telas.
- Dificultad para controlar el consumo de tela durante la producción.
- Riesgo de pérdida o duplicidad de información.
- Seguimiento limitado de pedidos personalizados y sus pagos.
- Ausencia de un historial organizado de ventas.
- Dependencia de registros físicos para la consulta de información.

---

# 3. Solución propuesta

Mantas Guajiras centralizará la información del negocio en una única plataforma, permitiendo administrar los procesos principales desde un solo sistema.

La primera versión incluirá funcionalidades para:

- Administración del inventario de mantas.
- Administración del inventario de telas.
- Registro de ventas de mantas.
- Registro de ventas de telas por metros.
- Registro del consumo de tela para la fabricación de mantas.
- Gestión de pedidos personalizados.
- Registro de abonos y pagos pendientes.
- Generación de reportes administrativos básicos.

Posteriormente se implementará la sincronización con la nube para permitir el trabajo desde múltiples dispositivos, incluso en escenarios donde la conexión a Internet sea intermitente.

---

# 4. Objetivos

## 4.1 Objetivo general

Desarrollar un sistema de gestión que permita administrar de forma eficiente el inventario, las ventas, la producción y los pedidos del negocio Mantas Guajiras.

### 4.2 Objetivos específicos

- Mantener un inventario actualizado.
- Disminuir errores en el registro de información.
- Facilitar el control del proceso de producción.
- Registrar todas las ventas realizadas.
- Gestionar pedidos personalizados y sus pagos.
- Facilitar la consulta de información para la toma de decisiones.
- Preparar la plataforma para futuras ampliaciones.

---

# 5. Alcance

La primera versión del sistema incluirá:

- Gestión de usuarios.
- Gestión de clientes.
- Inventario de mantas.
- Inventario de telas.
- Producción de mantas.
- Ventas.
- Pedidos personalizados.
- Registro de pagos.
- Reportes básicos.

No se incluyen en esta etapa:

- Sincronización en la nube.
- Facturación electrónica.
- Integración con WhatsApp.
- Lectores de código de barras.
- Aplicación móvil nativa.

Estas funcionalidades podrán incorporarse en versiones futuras.

---

# 6. Usuarios del sistema

El sistema contará inicialmente con dos perfiles.

## Administrador

Tendrá acceso completo al sistema y podrá administrar:

- Usuarios.
- Inventario.
- Producción.
- Ventas.
- Pedidos.
- Reportes.
- Configuración del sistema.

## Vendedor

Podrá:

- Registrar ventas.
- Registrar pedidos personalizados.
- Registrar el consumo de tela utilizado para producción.
- Consultar inventarios.
- Consultar pedidos.

No podrá modificar configuraciones críticas ni administrar usuarios.

---

# 7. Principios del proyecto

Durante el desarrollo del sistema se adoptarán los siguientes principios:

- La tecnología debe adaptarse al negocio.
- La interfaz debe ser sencilla e intuitiva.
- La información debe ser consistente y confiable.
- Todo movimiento importante debe quedar registrado.
- El sistema debe ser escalable.
- La documentación hará parte del desarrollo del proyecto.

---

# 8. Visión a futuro

Una vez implementada la primera versión del sistema, se evaluará la incorporación de nuevas funcionalidades como:

- Sincronización automática con la nube.
- Acceso desde múltiples dispositivos.
- Reportes avanzados.
- Estadísticas de ventas.
- Panel administrativo con indicadores.
- Nuevos módulos según las necesidades del negocio.

---

# 9. Criterios de éxito

Se considerará que el proyecto ha cumplido sus objetivos cuando:

- El inventario refleje la existencia real de mantas y telas.
- Las ventas se registren completamente desde el sistema.
- La producción actualice automáticamente el inventario.
- Los pedidos personalizados puedan consultarse en cualquier momento.
- Los usuarios puedan utilizar el sistema con facilidad después de una capacitación básica.
- El negocio pueda reemplazar completamente los registros manuales por el sistema.

---

# 10. Observaciones

Este documento constituye la base conceptual del proyecto Mantas Guajiras y servirá como referencia para el desarrollo de la arquitectura, la base de datos, la implementación del software y las futuras ampliaciones del sistema.