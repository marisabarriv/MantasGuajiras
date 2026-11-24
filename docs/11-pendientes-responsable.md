# Pendientes de la responsable del negocio

Tareas que **solo puede hacer la dueña del proyecto** (en su PC, en GitHub o como decisión de negocio). Todo lo demás de la certificación lo resuelve el desarrollo.

Marca cada casilla cuando la completes y anota la fecha.

---

## A. Antes de traer la versión nueva a tu PC

- [ ] **1. Copia de seguridad de tu base actual** (pgAdmin → clic derecho en la base → *Backup*, o `backup-db.ps1` si ya lo configuraste). Al arrancar, el backend aplicará las migraciones V20 y V21.
- [ ] **2. Traer los cambios:** en VS Code, en la rama del proyecto, `git pull`.
- [ ] **3. Revisar si tienes productos con el mismo nombre** (antes se permitía; ahora no). En pgAdmin → *Query Tool*:

  ```sql
  SELECT lower(name) AS nombre, count(*), string_agg(internal_code, ', ')
  FROM product GROUP BY lower(name) HAVING count(*) > 1;
  ```

  Si aparece alguno, cámbiale el nombre a uno de ellos desde **Productos → Editar**.
- [ ] **4. Actualizar `backend/.env`:** `JWT_EXPIRATION=43200000` (sesión de 12 horas, para que dure la jornada y sirva para vender sin conexión).
- [ ] **4A. Después del `git pull`, actualizar dependencias:** en VS Code, terminal en `backend`, ejecutar `./mvnw.cmd -DskipTests package` (descarga la librería nueva del estado del sistema). Al arrancar, comprobar en el navegador que `http://localhost:8080/actuator/health` dice `"status":"UP"`. El log queda en `backend\logs\mantasguajiras.log`.

## B. Copias de seguridad en Windows (último bloqueador de la certificación)

Guía completa: `docs/09-copias-de-seguridad.md`.

- [ ] **5. Contraseña de PostgreSQL en `%APPDATA%\postgresql\pgpass.conf`** (§1.1).
- [ ] **6. Crear `scripts\backup\backup.config.ps1`** a partir de `backup.config.example.ps1` (§1.2). Configurar `CopyTo` con una carpeta de **OneDrive o Google Drive**.
- [ ] **7. Primera copia y primera prueba a mano** (§1.3): `backup-db.ps1` y `restore-test.ps1` deben terminar en *"pasó todas las verificaciones"*.
- [ ] **8. Programar las copias automáticas** (§1.4): `register-scheduled-tasks.ps1`. Comprobar en el *Programador de tareas* que aparecen **"MantasGuajiras - Copias del día"** y **"MantasGuajiras - Prueba semanal"**.
- [ ] **9. Simulacro en tu PC:** restaurar una copia en una base nueva con `restore-db.ps1`, cronometrar y anotar el resultado en `docs/09` §7.
- [ ] **10. Revisar que PostgreSQL protege contra cortes de luz** (§1.5): `SHOW fsync;`, `SHOW synchronous_commit;` y `SHOW full_page_writes;` deben decir `on`.

## C. Decisiones de negocio

- [ ] **11. Aprobar los objetivos de recuperación** (`docs/09` §6): perder como máximo **2 horas** de información y volver a operar en **1 hora**. Si prefieres otros valores, indícalos.
- [ ] **12. Considerar una UPS ("no break")** para el computador y el módem.
- [ ] **12A. Confirmar cuántas personas usan la app a la vez** en el momento de más trabajo (contando vendedores y administradores). Se supuso **hasta 10** y se probó con 40 sin problemas (`docs/12-rendimiento-y-monitoreo.md` §2). Si esperas más de 40, avisa.

## D. GitHub

- [ ] **13. Proteger `main`** (`docs/06-despliegue.md` §14.6): exigir pull request y que la CI esté en verde.
- [ ] **14. Activar Dependabot:** *Settings → Code security → Dependabot alerts* y *Dependabot security updates*.

## E. Aceptación del negocio (al final)

- [ ] **15. Recorrer los 24 escenarios de aceptación** de la auditoría con usuarios reales (productos, inventario, ventas, compras y producción, usuarios y seguridad, sin conexión). En particular, comprobar **en la pantalla** el precio especial ($80.000 → $70.000 en el carrito, la confirmación y el historial).
- [ ] **16. Firmar el acta de aceptación** cuando todo esté conforme (sección 20 de `docs/10-certificacion.md`).

## F. Puesta en producción (Fase 7)

El kit de instalación ya está listo y probado. Guía paso a paso: **`docs/13-produccion.md`**.

- [ ] **17. Decidir dónde se instalará.** Recomendado: un **servidor en la nube (VPS)** con Ubuntu 24.04 y al menos **2 GB de RAM** (`docs/13` §2). Un computador en la tienda también sirve, pero es más complicado y depende de la luz y el internet del local.
- [ ] **18. Dominio:** elegir el nombre (por ejemplo `ventas.tudominio.com`) y crear el registro **A** apuntando a la IP del servidor. **Es obligatorio:** sin un dominio con HTTPS real, los celulares no pueden vender sin conexión, instalar la app ni usar la cámara.
- [ ] **19. Instalar** siguiendo `docs/13` §3, pasos 2 a 7 (o pedírselo a quien te ayude con el servidor): firewall, Docker, *deploy key* de GitHub, `deploy/.env` con claves nuevas y `./deploy.sh`. Debe terminar en *"instalada y verificada"*.
- [ ] **20. Cambiar a producción:**
  - [ ] Primero, **en cada dispositivo, que no queden ventas pendientes sin conexión**.
  - [ ] Después, copia en tu PC e importarla en el servidor con `import-data.sh` (`docs/13` §3, paso 8).
  - [ ] Desde ese día se vende **solo** en el servidor.
- [ ] **21. Segunda copia fuera del servidor** (Google Drive u OneDrive con `rclone`, `docs/13` §6).
- [ ] **22. Aviso automático si el sistema se cae:** registrar `https://tu-dominio/actuator/health` en un servicio de monitoreo externo (los hay gratuitos, por ejemplo UptimeRobot) con aviso a tu correo o celular.
- [ ] **23. Seguridad del servidor:** entrar por SSH con llave y desactivar la contraseña (`docs/13` §7).

---

| Fecha | Tarea | Nota |
| ----- | ----- | ---- |
|       |       |      |
