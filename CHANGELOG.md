# Historial de versiones

Cambios importantes de Mantas Guajiras, del más reciente al más antiguo. Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/). La versión instalada se ve en *Mi perfil* y en `/actuator/info`.

## [1.0.0] — pendiente

Se publicará cuando la responsable del negocio firme el acta de aceptación (`docs/15-aceptacion.md`) y el sistema empiece a usarse con los datos reales.

## [1.0.0-rc.1] — certificación (noviembre de 2026)

Versión candidata. Se llevó por las 8 fases de la certificación (`docs/10-certificacion.md`).

### Auditoría externa (25/11/2026)

- **Inicio de sesión:** los bloqueos se guardan en la base (V22) y sobreviven a un reinicio. Límites por cuenta y conexión, por conexión y por cuenta: nadie puede bloquear a otra persona a propósito.
- **Registro público:** límite de cuentas nuevas por conexión y en total.
- **Corregido:** con muchos registros simultáneos se podía pasar el límite (30 a la vez lograban 18 con un límite de 5). Ahora la decisión sale del contador ya sumado, en una sola instrucción de la base.
- **Corregido:** lo mismo en el inicio de sesión: 30 intentos simultáneos con contraseñas distintas se revisaban todos, aunque el límite era menor. Ahora cada intento se cuenta antes de revisar la contraseña y se devuelve si era correcta o si la cuenta estaba inactiva.
- **Corregido:** restablecer la contraseña no quitaba el bloqueo, aunque el aviso lo prometía. Ahora la persona entra de inmediato.
- `main` exige las 5 comprobaciones de la CI y la rama actualizada. CI con permisos mínimos y acciones fijadas a un commit.
- CodeQL, secret scanning, push protection y reporte privado de vulnerabilidades (`SECURITY.md`).
- Spring Boot 4.2.0, React 19.3.1 y las demás actualizaciones de Dependabot.

### Fase 8 — Aceptación

- Ensayo automático de los 24 escenarios de aceptación por la interfaz, como vendedor en celular y como administrador (`frontend/e2e/acceptance.e2e.cjs`). Corre en cada cambio.
- Manual de usuario con capturas reales (`docs/14`) y guía y acta de aceptación (`docs/15`).
- **Nuevo:** aviso antes de salir con productos en el carrito o con un formulario a medio llenar.
- Las copias de seguridad quedan legibles solo por su dueño. Guía para la copia cifrada en la nube.

### Fase 7 — Producción

- Kit de instalación `deploy/`:
  - Docker con HTTPS automático (Caddy) en el mismo dominio para la app y la API;
  - PostgreSQL 17 cerrado al exterior;
  - copias programadas;
  - reinicio automático de todos los servicios.
- `deploy.sh`: copia previa, verificación (`verify.sh`, 18 comprobaciones) y **vuelta atrás automática** si la versión nueva falla.
- `import-data.sh` para traer los datos del PC al servidor.
- Versión visible en *Mi perfil* y en `/actuator/info`.
- **Corregido:** con la base de datos caída, el servidor respondía "sesión vencida". Ahora responde 503 y la venta queda guardada en el dispositivo hasta que el servidor vuelve.
- Simulacros de caída de la base, corte de luz y versión rota, sin perder ninguna venta confirmada.

### Fase 6 — Rendimiento y monitoreo

- **Corregido:** con 3 años de datos, el historial completo agotaba la memoria del servidor (123 errores con 15 usuarios).
- Historiales de ventas y movimientos por periodo: 30 días por defecto, hasta 3 meses. En pantalla, de a 100 con "Ver más".
- Sin consultas repetidas por cada venta (N+1).
- Estado del sistema (`/actuator/health`), log a archivo con rotación y una línea por operación con usuario y motivo de los rechazos.
- Prueba de carga: 40 usuarios simultáneos sin errores.

### Fase 5 — Calidad

- Cobertura de pruebas medida (92 %) con umbral en el build; pruebas en orden aleatorio.
- Prueba de punta a punta por todas las pantallas con cada rol. Código sin uso eliminado.

### Fase 4 — Recuperación

- Actualización de la base con datos reales sin pérdidas; simulacro de pérdida total recuperado en 7,5 s.
- Procedimiento de vuelta atrás y objetivos de recuperación (RPO 2 h, RTO 1 h, por aprobar).

### Fase 3 — Seguridad

- Bloqueo de 15 minutos tras 5 contraseñas erradas.
- Spring Boot 4.1.3 para corregir vulnerabilidades críticas.
- Escaneo de dependencias en cada cambio; Dependabot.

### Fase 2 — Sin conexión

- Casos límite:
  - dos pestañas a la vez;
  - sesión vencida sin conexión;
  - dispositivo sin espacio;
  - celular compartido entre vendedores.
- Decisiones de negocio: **nombres de producto únicos**, sin estado "cancelada" en personalizadas.

### Fase 1 — Reglas del negocio

- Ninguna operación se duplica al reenviarse: ventas, compras, producción, personalizadas y pagos.
- Motivo obligatorio en los ajustes de inventario y filtros del historial.
- Errores claros (400) en lugar de errores internos con datos mal escritos.

### Antes de la certificación (noviembre de 2026)

- Ventas sin conexión con sincronización automática y pantallas que se actualizan solas.
- Los vendedores pueden ajustar el inventario por conteo físico.
- Copias de seguridad automáticas cada 2 horas de 6:00 a 21:00, con prueba de restauración.
- App instalable en el celular (PWA), escáner de códigos con la cámara y mantas personalizadas con abonos.

## Versiones iniciales (agosto–septiembre de 2026)

- Productos, inventario, ventas, compras, producción y pedidos.
- Autenticación con roles (administrador y vendedor) y administración de usuarios.
