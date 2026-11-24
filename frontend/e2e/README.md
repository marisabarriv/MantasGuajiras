# Pruebas de punta a punta (E2E)

`offline-sync.e2e.cjs` abre la app en un navegador real (Chromium) y comprueba las ventas sin conexión:

1. Venta sin conexión, app reabierta sin conexión y sincronización al volver la conexión.
2. Se corta la conexión **después** de que el servidor guardó la venta: al reintentar no se duplica.
3. Conflicto: otro vendedor vende la última unidad. La venta se rechaza sin dejar el inventario negativo, y "Reintentar" la registra después de un ajuste.
4. Dispositivo compartido: aviso al cerrar sesión con ventas pendientes, otro vendedor no envía ventas ajenas y la venta queda a nombre de quien la hizo.
5. Actualización automática del inventario y ausencia de violaciones de la política de seguridad (CSP).

Se ejecuta sola en GitHub Actions (`.github/workflows/ci.yml`) en cada push.

## Ejecutarla en tu PC

> **Nunca contra tu base de trabajo.** La prueba crea productos, vendedores y ventas. Usa una base vacía.

1. Crea una base vacía:

   ```bash
   createdb -U postgres mantas_guajiras_e2e
   ```

2. Arranca el backend contra esa base (perfil `prod`, todo por variables de entorno). En PowerShell, desde `backend`:

   ```powershell
   ./mvnw.cmd -DskipTests package
   $env:SPRING_PROFILES_ACTIVE="prod"; $env:DB_URL="jdbc:postgresql://localhost:5432/mantas_guajiras_e2e"
   $env:DB_USERNAME="postgres"; $env:DB_PASSWORD="TU_CONTRASEÑA"
   $env:JWT_SECRET="clave-solo-para-pruebas-e2e-de-al-menos-32-caracteres"; $env:JWT_EXPIRATION="3600000"
   $env:INITIAL_ADMIN_USERNAME="admine2e"; $env:INITIAL_ADMIN_PHONE="3001112233"; $env:INITIAL_ADMIN_PASSWORD="Prueba123"
   java -jar target/backend-1.0.0-rc.1.jar
   ```

3. En otra terminal, desde `frontend`, compila y sirve la app (la prueba necesita la versión compilada):

   ```powershell
   $env:VITE_API_URL="http://localhost:8080"; npm run build; npm run preview
   ```

4. En otra terminal, desde `frontend`, instala Playwright (solo la primera vez) y ejecuta:

   ```bash
   npm install --no-save playwright
   ```
   ```bash
   npx playwright install chromium
   ```
   ```bash
   node e2e/offline-sync.e2e.cjs
   ```

Termina con `RESULTADO: TODO CORRECTO`, o indica qué comprobación falló.

Variables opcionales: `E2E_APP` (por defecto `http://localhost:4173`), `E2E_API` (`http://localhost:8080`), `E2E_ADMIN_USER`, `E2E_ADMIN_PASSWORD` y `E2E_SCREENSHOTS` (carpeta donde guardar capturas).
