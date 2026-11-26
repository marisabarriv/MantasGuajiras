# Pendientes de la responsable del negocio

Tareas que **solo puede hacer la dueña del proyecto** (en su PC, en GitHub o como decisión de negocio). Todo lo demás de la certificación lo resuelve el desarrollo.

Marca cada casilla cuando la completes y anota la fecha.

---

## A. Antes de traer la versión nueva a tu PC

- [x] **1. Copia de seguridad de tu base actual** (pgAdmin → clic derecho en la base → *Backup*, o `backup-db.ps1` si ya lo configuraste). Al arrancar, el backend aplicará las migraciones V20 y V21.
- [x] **2. Traer los cambios:** en VS Code, en la rama del proyecto, `git pull`.
- [x] **3. Revisar si tienes productos con el mismo nombre** (antes se permitía; ahora no). En pgAdmin → *Query Tool*:

  ```sql
  SELECT lower(name) AS nombre, count(*), string_agg(internal_code, ', ')
  FROM product GROUP BY lower(name) HAVING count(*) > 1;
  ```

  Si aparece alguno, cámbiale el nombre a uno de ellos desde **Productos → Editar**.
- [x] **4. Actualizar `backend/.env`:** `JWT_EXPIRATION=43200000` (sesión de 12 horas, para que dure la jornada y sirva para vender sin conexión).
- [x] **4A. Después del `git pull`, actualizar dependencias:** en VS Code, terminal en `backend`, ejecutar `./mvnw.cmd -DskipTests package` (descarga la librería nueva del estado del sistema). Al arrancar, comprobar en el navegador que `http://localhost:8080/actuator/health` dice `"status":"UP"`. El log queda en `backend\logs\mantasguajiras.log`.

## B. Copias de seguridad en Windows (último bloqueador de la certificación)

Guía completa: `docs/09-copias-de-seguridad.md`.

- [x] **5. Contraseña de PostgreSQL en `%APPDATA%\postgresql\pgpass.conf`** (§1.1).
- [ ] **6. Crear `scripts\backup\backup.config.ps1`** a partir de `backup.config.example.ps1` (§1.2). Configurar `CopyTo` con una carpeta de **OneDrive o Google Drive**.
- [x] **7. Primera copia y primera prueba a mano** (§1.3): `backup-db.ps1` y `restore-test.ps1` deben terminar en *"pasó todas las verificaciones"*.
- [x] **8. Programar las copias automáticas** (§1.4): `register-scheduled-tasks.ps1`. Comprobar en el *Programador de tareas* que aparecen **"MantasGuajiras - Copias del día"** y **"MantasGuajiras - Prueba semanal"**.
- [x] **9. Simulacro en tu PC:** restaurar una copia en una base nueva con `restore-db.ps1`, cronometrar y anotar el resultado en `docs/09` §7.
- [x] **10. Revisar que PostgreSQL protege contra cortes de luz** (§1.5): `SHOW fsync;`, `SHOW synchronous_commit;` y `SHOW full_page_writes;` deben decir `on`.

## C. Decisiones de negocio

- [x] **11. Aprobar los objetivos de recuperación** (`docs/09` §6): perder como máximo **2 horas** de información y volver a operar en **1 hora**. Si prefieres otros valores, indícalos.
- [x] **12. Considerar una UPS ("no break")** para el computador y el módem.
- [x] **12A. Confirmar cuántas personas usan la app a la vez** en el momento de más trabajo (contando vendedores y administradores). Se supuso **hasta 10** y se probó con 40 sin problemas (`docs/12-rendimiento-y-monitoreo.md` §2). Si esperas más de 40, avisa.

## D. GitHub

- [x] **13. Proteger `main`** (`docs/06-despliegue.md` §14.6): exigir pull request, rama actualizada y las 5 comprobaciones de la CI en verde.
- [x] **14. Activar Dependabot:** *Settings → Code security → Dependabot alerts* y *Dependabot security updates*.

## E. Aceptación del negocio (al final)

Todo está preparado en **`docs/15-aceptacion.md`**. Los 24 escenarios ya pasaron el ensayo automático (24/24); falta tu confirmación con los datos y procedimientos reales. El manual con imágenes está en `docs/14-manual-de-usuario.md`.

- [ ] **15. Recorrer los 24 escenarios** de `docs/15` §3 con una vendedora real. Comprueba **en la pantalla** el precio especial ($80.000 → $70.000) y la venta sin conexión con el **modo avión** del celular.
- [ ] **16. Responder los 14 criterios del negocio** (`docs/15` §4) y **firmar el acta** (§6). Con el acta firmada, la versión pasa a `1.0.0`.

## F. Puesta en producción (Fase 7)

El kit de instalación ya está listo y probado. Guía paso a paso: **`docs/13-produccion.md`**.

- [ ] **17. Decidir dónde se instalará.** Recomendado: un **servidor en la nube (VPS)** con Ubuntu 24.04 y al menos **2 GB de RAM** (`docs/13` §2). Un computador en la tienda también sirve, pero es más complicado y depende de la luz y el internet del local.
- [ ] **18. Dominio:** elegir el nombre (por ejemplo `ventas.tudominio.com`) y crear el registro **A** apuntando a la IP del servidor. **Es obligatorio:** sin un dominio con HTTPS real, los celulares no pueden vender sin conexión, instalar la app ni usar la cámara.
- [ ] **19. Instalar** siguiendo `docs/13` §3, pasos 2 a 7 (o pedírselo a quien te ayude con el servidor): firewall, Docker, traer el proyecto, `deploy/.env` con claves **nuevas** y `./deploy.sh`. Debe terminar en *"instalada y verificada"*.
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
| 24/11/2026 | 1, 2 | Copia manual con huella SHA-256 correcta y `git pull` hechos por la responsable. |
| 24/11/2026 | 3 | Hay un nombre repetido: "manta 1" (PROD-00001 inactivo y PROD-00002 activo). **Falta cambiarle el nombre a uno.** |
| 24/11/2026 | 4, 4A | `JWT_EXPIRATION=43200000`; compila; base en V21; `/actuator/health` en `UP`. |
| 24/11/2026 | 5, 7, 8, 10 | Copia y prueba superadas; las dos tareas se ejecutaron desde el Programador con resultado 0; `fsync`, `synchronous_commit` y `full_page_writes` en `on`. |
| 24/11/2026 | 6 | `backup.config.ps1` creado. **Falta `CopyTo`** (OneDrive o Google Drive). |
| 24/11/2026 | 9 | Simulacro superado; ver `docs/09` §7. |
| 24/11/2026 | 13, 14 | Regla "Proteger main" activa (pull request, 4 comprobaciones de la CI, sin push forzado ni borrado; sin excepciones). Alertas y actualizaciones de seguridad de Dependabot activas. |
| 25/11/2026 | 13, 14 | Auditoría externa: ahora son 5 comprobaciones obligatorias y la rama debe estar actualizada. Encendidos CodeQL, secret scanning, push protection y reporte privado de vulnerabilidades. 0 alertas de Dependabot. |
| 25/11/2026 | 6, 17–23 | Copia externa y despliegue: aplazados por decisión de la responsable. |
| 25/11/2026 | 3 | El producto inactivo PROD-00001 pasó a llamarse "manta a" (desde *Productos → Editar*). Ya no hay nombres repetidos. |
| 25/11/2026 | 11 | Aprobado. Si el computador falla, se sigue vendiendo con la app instalada (PWA) en los celulares: las ventas quedan guardadas en cada celular y se envían cuando el sistema vuelve. El inventario y el historial se recuperan de la última copia, que hoy está en el mismo PC: falta la copia externa (tarea 6). |
| 25/11/2026 | 12 | No se comprará una UPS por ahora. La base ya está protegida contra cortes de luz (tarea 10). |
| 25/11/2026 | 12A | Hasta **5 personas** a la vez; se probó con 40 sin problemas. |
| 25/11/2026 | 15, 16 | Recorrido hecho por la responsable en una base de prueba aparte: todo funcionó. Criterios respondidos: 12 de 14 "Sí"; NEG-04 y NEG-05 quedan pendientes del uso real. Acta preparada como **"Aceptado con observaciones"** (`docs/15` §5 y §6); falta la firma. |
