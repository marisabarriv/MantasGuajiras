# Documento de Despliegue

| Campo                    | Valor                  |
| ------------------------ | ---------------------- |
| **Proyecto**             | Mantas Guajiras        |
| **Documento**            | Despliegue del Sistema |
| **Código**               | DOC-06                 |
| **Versión**              | v0.4.0                 |
| **Estado**               | En desarrollo          |
| **Responsable**          | Equipo de Desarrollo   |
| **Última actualización** | 05/09/2026             |

---

## Control de versiones

| Versión | Fecha      | Descripción                                   | Responsable          |
| ------- | ---------- | --------------------------------------------- | -------------------- |
| v0.1.0  | 29/07/2026 | Creación inicial del documento.               | Equipo de Desarrollo |
| v0.4.0  | 05/09/2026 | Actualización de la estrategia de despliegue. | Equipo de Desarrollo |

---

# 1. Introducción

Este documento describe la estrategia de despliegue del sistema **Mantas Guajiras** durante sus diferentes etapas de desarrollo.

El objetivo es establecer una forma organizada de ejecutar y mantener los componentes del sistema, considerando el frontend, backend y base de datos.

Actualmente el sistema se encuentra en desarrollo y utiliza una arquitectura web con React, Spring Boot y PostgreSQL.

---

# 2. Objetivos

El despliegue del sistema deberá:

* Permitir ejecutar correctamente todos los componentes.
* Mantener la integridad de la información.
* Facilitar las actualizaciones del sistema.
* Separar los entornos de desarrollo y producción cuando corresponda.
* Permitir realizar respaldos de la información.
* Facilitar futuras ampliaciones de la infraestructura.

---

# 3. Entornos

## Desarrollo

Es el entorno utilizado durante la construcción y validación del sistema.

Características principales:

* Frontend ejecutado con Vite.
* Backend ejecutado con Spring Boot.
* Base de datos PostgreSQL.
* Datos utilizados para desarrollo y pruebas.
* Ejecución local de los componentes.

---

## Producción

La implementación de producción se realizará cuando el sistema se encuentre preparado para su utilización por parte del negocio.

El entorno de producción deberá contar con:

* Frontend accesible mediante navegador web.
* Backend ejecutándose de forma estable.
* Base de datos PostgreSQL.
* Datos reales del negocio.
* Mecanismos de respaldo de la información.

La configuración definitiva de infraestructura se establecerá cuando se realice el despliegue del sistema en el entorno de producción.

---

# 4. Componentes del sistema

El sistema está compuesto por:

* Frontend web.
* Backend.
* Base de datos PostgreSQL.

Las tecnologías principales son:

* React + Vite para el frontend.
* Java 21 + Spring Boot para el backend.
* PostgreSQL para la base de datos.

Los componentes se comunican mediante la API REST del backend.

---

# 5. Estrategia de implementación

La implementación se realizará de manera progresiva.

## Desarrollo

Durante esta etapa se realizan:

* Desarrollo de funcionalidades.
* Corrección de errores.
* Pruebas de los diferentes módulos.
* Integración entre frontend y backend.
* Validación de las operaciones de la base de datos.

---

## Preparación para producción

Antes de utilizar el sistema con información real se deberá:

* Verificar el funcionamiento del frontend.
* Verificar el funcionamiento del backend.
* Configurar la base de datos de producción.
* Realizar las migraciones correspondientes.
* Configurar los usuarios y permisos necesarios.
* Cargar o registrar la información inicial del negocio.
* Realizar pruebas funcionales.

---

## Puesta en producción

Una vez validado el sistema:

* Se desplegarán los componentes necesarios.
* Se verificará la conexión entre frontend y backend.
* Se comprobará la conexión con PostgreSQL.
* Se validarán las funcionalidades principales.
* Se realizará una revisión final antes de comenzar la operación con datos reales.

---

# 6. Respaldo de la información

La base de datos deberá contar con mecanismos de respaldo para proteger la información del negocio.

Se recomienda:

* Realizar respaldos periódicos.
* Realizar un respaldo antes de actualizaciones importantes.
* Conservar varias copias recientes.
* Verificar periódicamente que los respaldos puedan restaurarse correctamente.

La frecuencia definitiva de los respaldos se establecerá de acuerdo con las necesidades del negocio y el entorno de producción.

---

# 7. Actualizaciones

Las actualizaciones deberán realizarse de manera controlada.

Antes de instalar una nueva versión se deberá:

* Realizar un respaldo de la base de datos.
* Verificar los cambios incluidos.
* Ejecutar las migraciones necesarias.
* Comprobar la compatibilidad entre frontend y backend.
* Validar el funcionamiento después de la actualización.

Las modificaciones estructurales de la base de datos deberán gestionarse mediante nuevas migraciones de Flyway.

---

# 8. Requisitos mínimos

## Hardware

Los requisitos definitivos dependerán del entorno de producción seleccionado.

Para desarrollo se requiere un equipo capaz de ejecutar:

* Java 21.
* Node.js y Vite.
* PostgreSQL.
* Un navegador web moderno.

---

## Software

* Java 21.
* PostgreSQL.
* Node.js.
* Navegador web moderno.
* Git para desarrollo y control de versiones.

---

# 9. Escalabilidad

La arquitectura permite ampliar progresivamente los recursos utilizados por el sistema.

La aplicación web puede ser utilizada desde diferentes tipos de dispositivos mediante un diseño responsivo.

La incorporación de nuevos módulos o funcionalidades deberá realizarse manteniendo la separación entre frontend, backend y base de datos.

La infraestructura podrá ampliarse posteriormente de acuerdo con las necesidades reales del negocio.

---

# 10. Monitoreo

Durante la operación del sistema se deberá verificar periódicamente:

* Disponibilidad del backend.
* Disponibilidad de PostgreSQL.
* Funcionamiento del frontend.
* Espacio disponible en disco.
* Estado de los respaldos.
* Errores relevantes de la aplicación.

Los mecanismos específicos de monitoreo se definirán cuando se establezca el entorno de producción definitivo.

---

# 11. Plan de recuperación

Ante una falla que afecte la información o el funcionamiento del sistema se seguirá un procedimiento acorde con el tipo de incidente.

De manera general:

1. Identificar la causa de la falla.
2. Detener las operaciones afectadas cuando sea necesario.
3. Verificar el estado de la base de datos.
4. Restaurar un respaldo si existe pérdida o corrupción de información.
5. Verificar la integridad de los datos.
6. Reiniciar los servicios afectados.
7. Validar el funcionamiento del sistema.
8. Reanudar la operación.

---

# 12. Evolución del despliegue

La evolución del sistema se realizará de forma progresiva.

**Versión 0.1.0**

* Documentación y planificación inicial.

**Versión 0.2.0**

* Diseño e implementación inicial de la base de datos.

**Versión 0.3.0**

* Desarrollo inicial del backend.

**Versión 0.4.0**

* Desarrollo e integración progresiva del frontend.

**Versiones posteriores**

* Integración completa de funcionalidades.
* Pruebas funcionales.
* Preparación del entorno de producción.
* Implementación del sistema en el negocio.
* Ajustes y mejoras posteriores.

Las versiones futuras se definirán de acuerdo con el avance real del proyecto y no representan funcionalidades implementadas actualmente.

---

# 13. Observaciones

Este documento establece las consideraciones generales para el despliegue del sistema Mantas Guajiras.

La estrategia podrá adaptarse cuando se defina la infraestructura definitiva del entorno de producción.

Actualmente no se considera implementado un sistema de funcionamiento offline ni una sincronización automática entre bases de datos locales y una base de datos centralizada en la nube.

Cualquier modificación importante relacionada con la infraestructura o el proceso de despliegue deberá reflejarse en este documento.
