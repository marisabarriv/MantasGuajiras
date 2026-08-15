# Documento de Despliegue

| Campo | Valor |
|--------|-------|
| **Proyecto** | Mantas Guajiras |
| **Documento** | Despliegue del Sistema |
| **Código** | DOC-06 |
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

Este documento describe la estrategia de despliegue del sistema **Mantas Guajiras** para las diferentes etapas del proyecto.

El objetivo es garantizar una implementación ordenada, estable y escalable, permitiendo que el sistema evolucione desde un entorno local hasta una solución con sincronización en la nube.

---

# 2. Objetivos

El despliegue del sistema deberá:

- Permitir un funcionamiento estable en el negocio.
- Facilitar futuras actualizaciones.
- Minimizar el tiempo de inactividad.
- Mantener la integridad de la información.
- Preparar el sistema para la sincronización en la nube.

---

# 3. Entornos

El proyecto contará con los siguientes entornos.

## Desarrollo

Utilizado durante la programación y pruebas.

Características:

- Frontend ejecutándose con Vite.
- Backend ejecutándose con Spring Boot.
- Base de datos PostgreSQL local.
- Datos de prueba.

---

## Producción local

Primera versión utilizada por el negocio.

Características:

- Backend instalado en el computador principal.
- Base de datos PostgreSQL local.
- Frontend accesible desde el navegador del computador.
- Datos reales del negocio.

---

## Producción con sincronización

Versión futura.

Características:

- Base de datos centralizada.
- Sincronización automática entre dispositivos.
- Funcionamiento offline con sincronización periódica.
- Acceso desde diferentes equipos autorizados.

---

# 4. Componentes del sistema

El despliegue estará compuesto por:

- Frontend (React + Vite)
- Backend (Spring Boot)
- Base de datos PostgreSQL

Cada componente podrá actualizarse de manera independiente.

---

# 5. Estrategia de implementación

La implementación se realizará en tres fases.

## Fase 1

Desarrollo del sistema.

- Construcción de funcionalidades.
- Pruebas unitarias.
- Pruebas de integración.

---

## Fase 2

Implementación local.

- Instalación en el negocio.
- Configuración de PostgreSQL.
- Carga del inventario inicial.
- Capacitación básica a los usuarios.
- Validación del funcionamiento.

---

## Fase 3

Implementación de sincronización.

- Configuración de la infraestructura en la nube.
- Conexión de los dispositivos.
- Activación de la sincronización automática.
- Monitoreo del funcionamiento.

---

# 6. Respaldo de la información

El sistema deberá permitir realizar copias de seguridad de la base de datos.

Se recomienda:

- Respaldo diario automático.
- Respaldo manual antes de actualizaciones importantes.
- Conservación de varias versiones recientes del respaldo.

---

# 7. Actualizaciones

Las actualizaciones del sistema deberán realizarse de manera controlada.

Antes de instalar una nueva versión será necesario:

- Realizar un respaldo de la base de datos.
- Verificar la compatibilidad de la nueva versión.
- Confirmar el funcionamiento correcto después de la actualización.

---

# 8. Requisitos mínimos

## Hardware

- Computador con Windows 10 o superior.
- Procesador de dos núcleos o superior.
- 8 GB de memoria RAM.
- 10 GB de espacio libre en disco.

---

## Software

- Java 21.
- PostgreSQL.
- Navegador web moderno.
- Git (solo para desarrollo).

---

# 9. Escalabilidad

La arquitectura permitirá incorporar nuevos equipos sin modificar el funcionamiento principal del sistema.

La incorporación de nuevos módulos deberá realizarse manteniendo la compatibilidad con las versiones anteriores.

---

# 10. Monitoreo

Se recomienda verificar periódicamente:

- Estado del servidor.
- Disponibilidad de la base de datos.
- Espacio disponible en disco.
- Correcto funcionamiento de los respaldos.

---

# 11. Plan de recuperación

Ante una falla crítica se seguirá el siguiente procedimiento:

1. Detener el sistema.
2. Restaurar el respaldo más reciente.
3. Verificar la integridad de la base de datos.
4. Reiniciar los servicios.
5. Validar el funcionamiento antes de reanudar la operación.

---

# 12. Evolución del despliegue

La evolución prevista para el sistema será:

**Versión 0.1.0**
- Documentación y planificación.

**Versión 0.2.0**
- Diseño e implementación de la base de datos.

**Versión 0.3.0**
- Desarrollo del backend.

**Versión 0.4.0**
- Desarrollo del frontend.

**Versión 0.5.0**
- Integración del sistema.

**Versión 0.6.0**
- Pruebas funcionales.

**Versión 0.7.0**
- Implementación local en el negocio.

**Versión 0.8.0**
- Conteo oficial e inventario inicial.

**Versión 0.9.0**
- Sincronización automática y despliegue en la nube.

**Versión 1.0.0**
- Sistema en producción.

---

# 13. Observaciones

Este documento establece la estrategia de despliegue del proyecto y servirá como referencia para futuras implementaciones y actualizaciones del sistema.

Cualquier modificación importante en la infraestructura deberá reflejarse en este documento antes de su implementación.