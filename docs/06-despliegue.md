# Documento de Despliegue

| Campo                    | Valor                  |
| ------------------------ | ---------------------- |
| **Proyecto**             | Mantas Guajiras        |
| **Documento**            | Despliegue del Sistema |
| **Código**               | DOC-06                 |
| **Versión**              | v1.0.0-rc.1           |
| **Estado**               | Versión candidata      |
| **Responsable**          | Equipo de Desarrollo   |
| **Última actualización** | 23/11/2026             |

---

## Control de versiones

| Versión | Fecha      | Descripción                                   | Responsable          |
| ------- | ---------- | --------------------------------------------- | -------------------- |
| v0.1.0  | 29/07/2026 | Creación inicial del documento.               | Equipo de Desarrollo |
| v0.4.0  | 05/09/2026 | Actualización de la estrategia de despliegue. | Equipo de Desarrollo |
| v1.0.0-rc.1 | 23/11/2026 | Alineación con la implementación real tras la auditoría técnica. | Equipo de Desarrollo |

---

# 1. Introducción

Este documento describe la estrategia de despliegue del sistema **Mantas Guajiras** durante sus diferentes etapas de desarrollo.

El objetivo es establecer una forma organizada de ejecutar y mantener los componentes del sistema, considerando el frontend, backend y base de datos.

El sistema está funcionalmente completo (versión candidata `v1.0.0-rc.1`) y por ahora se ejecuta en un computador local. Utiliza una arquitectura web con React, Spring Boot y PostgreSQL.

La sección 14 describe los pasos concretos de instalación, configuración y despliegue.

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

**Implementado:** copias automáticas cada 2 horas de 6:00 a 21:00 y al iniciar sesión, prueba semanal de restauración con verificación de integridad y procedimiento de recuperación. Ver **[09-copias-de-seguridad.md](09-copias-de-seguridad.md)**.

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

**Versión 1.0.0-rc.1 (actual)**

* Módulos funcionales completos: productos, inventario, ventas, compras, producción, personalizadas y pagos, usuarios.
* Permisos por rol aplicados en el backend.
* Pruebas automatizadas de permisos, reglas de producto, ventas concurrentes y migraciones.
* Copias de seguridad automáticas con prueba de restauración.

**Versión 1.0.0**

* Primera versión usada con los datos reales del negocio, después de completar la lista de la sección 14.4.

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

Las ventas se pueden registrar sin conexión y se sincronizan solas al volver la conexión (sección 15.4). No hay bases de datos locales que sincronizar: la base es única.

Cualquier modificación importante relacionada con la infraestructura o el proceso de despliegue deberá reflejarse en este documento.


---

# 14. Guía práctica

## 14.1. Instalación local (desarrollo)

Requisitos: Java 21, Node.js 20 o superior, PostgreSQL 16 o superior, Git.

```bash
git clone https://github.com/marisabarriv/MantasGuajiras.git
```

**Base de datos.** Crear la base vacía (Flyway crea las tablas al arrancar):

```bash
createdb -U postgres mantas_guajiras_db
```

**Backend.**

1. Copiar `backend/src/main/resources/application-dev.example.yml` como `application-dev.yml` y poner la contraseña de PostgreSQL.
2. Copiar `backend/.env.example` como `backend/.env` y completar `JWT_SECRET` y los datos del administrador inicial.
3. Arrancar desde la carpeta `backend`:

   ```bash
   ./mvnw spring-boot:run
   ```

   En Windows: `mvnw.cmd spring-boot:run`.

`backend/.env` se carga automáticamente (`spring.config.import` en `application.yml`) **cuando el backend se arranca desde la carpeta `backend`**. Las variables de entorno reales del sistema tienen prioridad sobre ese archivo. Formato: `CLAVE=valor`, sin comillas.

**Frontend.**

1. Copiar `frontend/.env.example` como `frontend/.env`.
2. Desde la carpeta `frontend`:

   ```bash
   npm ci
   ```

   ```bash
   npm run dev
   ```

**Pruebas del backend.** Usan una base aparte, nunca la de trabajo:

```bash
createdb -U postgres mantas_guajiras_test
```

```bash
./mvnw test
```

Si tu usuario o contraseña de PostgreSQL no son `postgres`/`postgres`, usa las variables `TEST_DB_USERNAME` y `TEST_DB_PASSWORD` (o `TEST_DB_URL`).

## 14.2. Variables de entorno

| Variable                  | Perfil    | Descripción                                                        |
| ------------------------- | --------- | ------------------------------------------------------------------ |
| `SPRING_PROFILES_ACTIVE`  | todos     | `dev` (por defecto) o `prod`.                                       |
| `JWT_SECRET`              | todos     | Clave de firma de sesiones. Mínimo 32 caracteres. Distinta por entorno. |
| `JWT_EXPIRATION`          | todos     | Duración de la sesión en milisegundos.                              |
| `INITIAL_ADMIN_USERNAME`  | todos     | Administrador que se crea si no existe ninguno.                     |
| `INITIAL_ADMIN_PHONE`     | todos     | Teléfono de ese administrador.                                      |
| `INITIAL_ADMIN_PASSWORD`  | todos     | Contraseña inicial (4 a 12 caracteres). Cambiarla al primer ingreso. |
| `DB_URL`                  | prod      | `jdbc:postgresql://servidor:5432/mantas_guajiras_db`                |
| `DB_USERNAME`             | prod      | Usuario de la aplicación en PostgreSQL (no `postgres`).             |
| `DB_PASSWORD`             | prod      | Contraseña de ese usuario.                                          |
| `CORS_ALLOWED_ORIGINS`    | prod      | Dominio(s) del frontend separados por comas. Por defecto: `localhost`. |
| `VITE_API_URL`            | frontend  | Dirección del backend al compilar el frontend.                      |

## 14.3. Producción

**Backend.**

```bash
./mvnw -DskipTests package
```

```bash
SPRING_PROFILES_ACTIVE=prod java -jar target/backend-1.0.0-rc.1.jar
```

El perfil `prod` toma la conexión, las claves y el administrador inicial **solo** de variables de entorno y desactiva Swagger.

**Frontend.** Con `VITE_API_URL` apuntando al backend real:

```bash
npm run build
```

El resultado queda en `frontend/dist` y se publica en cualquier servidor web (Nginx, Caddy, IIS, un hosting estático...). Como es una aplicación de una sola página, el servidor debe responder `index.html` para cualquier ruta desconocida.

**CORS.** Si frontend y backend están en dominios distintos (`app.ejemplo.com` y `api.ejemplo.com`), definir `CORS_ALLOWED_ORIGINS=https://app.ejemplo.com`. Si se sirven desde el mismo dominio mediante un proxy inverso (recomendado), CORS no interviene.

**PostgreSQL.**

* Crear un usuario propio para la aplicación, dueño solo de su base:

  ```sql
  CREATE USER mantas_app WITH PASSWORD 'una_contraseña_larga';
  CREATE DATABASE mantas_guajiras_db OWNER mantas_app;
  ```

* No exponer el puerto 5432 a Internet; solo el backend debe conectarse.

## 14.4. Lista mínima antes de usar datos reales

- [ ] Configuración de producción separada de la de desarrollo (`SPRING_PROFILES_ACTIVE=prod`).
- [ ] `JWT_SECRET` nuevo, largo y distinto del de desarrollo.
- [ ] Contraseña del administrador inicial cambiada después del primer ingreso.
- [ ] PostgreSQL con usuario propio y sin acceso desde Internet.
- [ ] HTTPS en el dominio del frontend y del backend.
- [ ] `CORS_ALLOWED_ORIGINS` con el dominio real (o mismo origen mediante proxy).
- [ ] Copias automáticas programadas **y** una prueba de restauración exitosa (`docs/09-copias-de-seguridad.md`).
- [ ] Segunda copia fuera del equipo o servidor (`CopyTo`).
- [ ] `./mvnw test` en verde.
- [ ] Recorrido manual de los flujos críticos: venta normal, venta con precio especial, compra, producción, ajuste de inventario, personalizada con abono y pago final, inicio de sesión como vendedor para confirmar que no puede editar productos.

## 14.5. Compartir el proyecto sin claves

Nunca comprimir la carpeta del proyecto a mano: incluiría `.env`, `application-dev.yml` y `node_modules`. Para entregar una copia del código, usar:

```bash
git archive --format=zip -o MantasGuajiras.zip HEAD
```

Ese ZIP contiene únicamente los archivos versionados en git: sin claves, sin dependencias y sin copias de la base de datos.

---

# 15. Varios usuarios y dispositivos (nube)

## 15.1. Una sola base de datos, no sincronización

En la nube **no hace falta sincronizar bases de datos**. Hay **una sola** base PostgreSQL y un solo backend; el administrador, los vendedores y cualquier celular o computador se conectan al mismo backend por Internet:

```text
Administrador (PC) ──┐
Vendedor (celular) ──┼──HTTPS──► Backend (Spring Boot) ──► PostgreSQL (única)
Vendedor 2 (tablet) ─┘
```

Cuando alguien registra una venta, queda guardada en esa única base y cualquier otra persona la ve al consultar. Las reglas que ya existen protegen el trabajo simultáneo: el bloqueo de inventario impide vender dos veces la última unidad (prueba `ConcurrentSaleTest`).

Si se cae Internet en el local, las **ventas** se pueden seguir registrando sin conexión (sección 15.4).

**Hoy, en local:** el vendedor puede usar la misma base si está en la misma red Wi-Fi que el computador donde corre el sistema (`npm run dev:celular`), siempre que ese computador esté encendido.

## 15.2. Ver los cambios de otros usuarios (implementado)

Las pantallas se actualizan solas, sin recargar:

* al volver a la pestaña o a la app (por ejemplo, al desbloquear el celular),
* cuando vuelve la conexión,
* cada minuto mientras la pantalla está visible.

Si una actualización automática falla (sin conexión), se conservan los datos que ya estaban en pantalla. Además, el backend siempre valida con los datos actuales: una pantalla con unos segundos de atraso no produce errores en los datos.

## 15.3. Copias en la nube

Los servicios de PostgreSQL administrado (por ejemplo, los que ofrecen Railway, Render, Supabase, Neon o los grandes proveedores) incluyen copias automáticas y restauración a un momento exacto (*point-in-time recovery*). Los apagones en el local dejan de afectar a los datos, porque la base ya no está en ese computador. Aun así, conviene seguir haciendo una copia propia periódica con `backup-db.sh` o `backup-db.ps1` apuntando a la base de la nube, para no depender de un solo proveedor.

## 15.4. Ventas sin conexión (implementado)

Cuando un dispositivo no puede comunicarse con el servidor (sin Internet, Wi-Fi caído o servidor apagado):

1. Aparece arriba el aviso **"Sin conexión con el servidor"**.
2. En **Nueva venta** se usan los productos, precios y existencias guardados la última vez que hubo conexión; la pantalla dice de qué hora son.
3. Al confirmar, la venta queda **guardada en el dispositivo** y aparece en la lista "Ventas guardadas sin conexión". Lo vendido se resta de las existencias que ve ese dispositivo.
4. Al volver la conexión (o al abrir la app, o cada 30 segundos) las ventas se envían solas.
5. Si el servidor rechaza una (por ejemplo, otro vendedor vendió la última unidad), queda marcada en rojo con el motivo y los botones **Reintentar** y **Descartar**.

Condiciones:

* El dispositivo debe haber abierto **Nueva venta** al menos una vez con conexión (así guarda el catálogo).
* Para **reabrir** la app sin conexión se necesita la versión compilada (`npm run build`, servida en producción o con `npm run preview`). Con `npm run dev` se puede seguir vendiendo si la app ya estaba abierta cuando se fue la conexión, pero no reabrirla.
* La sesión dura lo que indique `JWT_EXPIRATION`. Sin conexión se puede seguir vendiendo aunque venza; las ventas se envían cuando la persona vuelve a iniciar sesión con conexión. Se recomienda una duración que cubra una jornada (por ejemplo, 12 horas = `43200000`).
* No borrar los datos del navegador ni desinstalar la app mientras haya ventas en la lista de pendientes.
* Si un producto cambió de precio mientras no había conexión, la venta se registra con el precio vigente al recibirla.

Verificado con una prueba de navegador de punta a punta: venta sin conexión, app reabierta sin conexión, conflicto con otra venta, sincronización automática, reenvío sin duplicar y actualización automática del inventario.
