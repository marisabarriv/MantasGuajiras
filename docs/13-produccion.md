# Puesta en producción

Fase 7 de la certificación (`docs/10-certificacion.md`, sección 17). Probado el 24/11/2026.

---

## 1. Resumen

La carpeta `deploy/` es un **kit de instalación** que deja la app funcionando en un servidor con un solo comando (`./deploy.sh`):

| Pieza | Qué hace |
| --- | --- |
| **web** (Caddy) | Sirve la app y obtiene y renueva solo el certificado **HTTPS** (Let's Encrypt, gratuito). Es lo **único** con puertos abiertos (80 y 443). Redirige `http://` a `https://`, agrega cabeceras de seguridad y reenvía `/api`, `/auth` y `/users` al backend. App y API quedan en el mismo dominio, así que no hace falta configurar CORS. |
| **backend** | Spring Boot con el perfil `prod`, sin privilegios de administrador, memoria limitada a 512 MB y reinicio automático si algo falla. |
| **db** | PostgreSQL 17, **sin puertos publicados**. La app usa su propio usuario de base de datos, sin permisos de administrador. |
| **backup** | Copia al arrancar, cada 2 horas de 6:00 a 21:00 y prueba de restauración los domingos. Cada copia lleva su huella SHA-256 y una segunda copia en otra carpeta. |

Todos los servicios se reinician solos: si se caen, si se agota la memoria o si el servidor se apaga y vuelve a encender.

Scripts:

| Script | Para qué |
| --- | --- |
| `deploy/deploy.sh` | Instalar o actualizar: **copia previa → compilar → arrancar → verificar**. Si la verificación falla, **vuelve sola a la versión anterior**. |
| `deploy/verify.sh` | 18 comprobaciones tras instalar: estado, versión, app, modo sin conexión, API, Swagger y Actuator cerrados, HTTPS, cabeceras de seguridad y redirección. |
| `deploy/import-data.sh` | Traer los datos de tu PC al servidor, con su huella comprobada y una copia previa. |

**Lo que decide la dueña:** dónde instalarlo y el dominio (§2). Todo lo demás está listo y probado.

## 2. Dónde instalarlo

### Por qué hace falta un dominio con HTTPS real

El navegador del celular solo permite tres cosas con HTTPS y un certificado válido:

- **Vender sin conexión.** Comprobado: con un certificado no válido, Chromium no registra el *service worker* y la app no abre sin internet.
- **Instalar la app.**
- **Usar la cámara** para leer códigos de barras.

Por eso una dirección como `http://192.168.1.10` o un certificado "casero" **no sirven** para los celulares de la tienda. Se necesita un dominio, por ejemplo `ventas.mantasguajiras.com`. Con él, Caddy consigue el certificado gratis y lo renueva solo.

### Opciones

| | **A. Servidor en la nube (VPS)** — recomendado | B. Computador en la tienda |
| --- | --- | --- |
| Acceso | Desde cualquier lugar, incluido el administrador fuera del local | Solo si se expone a internet: IP pública y puertos en el módem, o un túnel |
| Cortes de luz o de internet en la tienda | El servidor sigue arriba. Los vendedores sin internet venden sin conexión y todo se envía al volver | El servidor se apaga con la tienda (hace falta UPS) |
| HTTPS / dominio | Automático (§3) | Igual de necesario, y más difícil sin IP pública (muchos proveedores la comparten, CGNAT) |
| Copias fuera del equipo | Hay que configurarlas (§6) | Hay que configurarlas |
| Costo | Mensualidad del VPS + dominio anual | Equipo encendido 24 h + dominio + posible túnel |

**Recomendación: opción A.**

- **Servidor:** Linux Ubuntu 24.04 LTS, con al menos **2 GB de RAM** (4 GB holgado; ver `docs/12` §2.5), 1–2 núcleos y 40 GB de disco.
- **Proveedor:** cualquiera que ofrezca VPS con Ubuntu.
- **Dominio:** el que prefieras. Basta un subdominio de uno que ya tengas.

## 3. Instalación paso a paso (servidor Ubuntu)

> Antes de empezar, en **cada celular y computador** de la tienda: que no quede ninguna venta en *"Ventas guardadas sin conexión"*. Esas ventas viven en el navegador y están atadas a la dirección actual de la app. Al cambiar de dirección no se envían.

1. **Dominio.** En el panel del dominio, crear un registro **A** que apunte el nombre elegido (por ejemplo `ventas`) a la IP pública del servidor. Puede tardar unos minutos en propagarse.

2. **Entrar al servidor** desde tu PC: `ssh root@IP_DEL_SERVIDOR`.

3. **Sistema, firewall y Docker:**

   ```bash
   apt update && apt upgrade -y && apt install -y git ufw unattended-upgrades
   ```
   ```bash
   ufw allow OpenSSH && ufw allow 80/tcp && ufw allow 443/tcp && ufw allow 443/udp && ufw --force enable
   ```
   ```bash
   curl -fsSL https://get.docker.com | sh
   ```

   - El firewall deja entrar solo SSH, HTTP y HTTPS. PostgreSQL ni siquiera publica su puerto.
   - Si el proveedor tiene su propio firewall en el panel, abre allí también **solo** 22, 80 y 443.
   - `unattended-upgrades` instala solas las actualizaciones de seguridad del sistema.

4. **Traer el proyecto.** El repositorio es privado: crea una *deploy key* de solo lectura.

   ```bash
   ssh-keygen -t ed25519 -N "" -f ~/.ssh/id_ed25519 && cat ~/.ssh/id_ed25519.pub
   ```

   Copia la línea que aparece en GitHub → el repositorio → *Settings → Deploy keys → Add deploy key*, sin marcar "Allow write access". Luego:

   ```bash
   git clone git@github.com:marisabarriv/MantasGuajiras.git /opt/mantasguajiras
   ```

5. **Configuración:**

   ```bash
   cd /opt/mantasguajiras/deploy && cp .env.example .env && nano .env
   ```

   - `SITE_ADDRESS`: tu dominio, sin `https://`.
   - Claves: genera cada una con `openssl rand -hex 24` (`POSTGRES_PASSWORD` y `DB_PASSWORD`) y `openssl rand -hex 32` (`JWT_SECRET`). Distintas entre sí y de las de tu PC.
   - Administrador inicial: usuario, teléfono y contraseña. Se cambia en el primer ingreso.
   - `BACKUP_COPY_TO`: carpeta de la segunda copia (§6).
   - `deploy/.env` **nunca** se sube a git.

6. **Instalar:**

   ```bash
   ./deploy.sh
   ```

   - Termina en `== Listo: versión … instalada y verificada.`
   - La primera vez tarda unos minutos, porque compila todo.

7. **Entrar** a `https://tu-dominio` con el administrador inicial y **cambiar su contraseña** en *Mi perfil*.

8. **Traer tus datos desde el PC** (si ya los tienes allí):
   1. En el PC, haz una copia con `backup-db.ps1`. Deja un `.dump` y su `.sha256` en tu carpeta de copias.
   2. Cópialos al servidor desde PowerShell:

      ```powershell
      $f = "$env:USERPROFILE\MantasGuajiras-backups\mantas_AAAA-MM-DD_HHMMSS.dump"
      scp $f "$f.sha256" root@IP_DEL_SERVIDOR:/opt/mantasguajiras/deploy/backups/
      ```

   3. En el servidor:

      ```bash
      ./import-data.sh backups/mantas_AAAA-MM-DD_HHMMSS.dump
      ```

      Pide escribir `REEMPLAZAR`. Después verifica la integridad y la app. Tus usuarios y contraseñas siguen siendo los mismos.

   4. Desde ese momento **se vende solo en el servidor**. Hay una sola base de datos (`docs/06` §15.1): la del PC queda como archivo histórico.

9. **En cada celular:** abrir `https://tu-dominio`, iniciar sesión y pulsar **Instalar app**.

## 4. Actualizar y volver atrás

**Actualizar.** Fuera del horario de atención:

```bash
cd /opt/mantasguajiras && git pull && cd deploy && ./deploy.sh
```

1. Copia la base.
2. Compila la versión nueva.
3. La arranca.
4. Verifica.

Durante la actualización la app queda **unos 9 segundos sin servidor**: muestra "sin conexión" y las ventas se guardan en el dispositivo.

**Si la versión nueva falla la verificación**, `deploy.sh` vuelve solo a la anterior y la verifica (probado, §8).

**Volver a una versión concreta a mano:**

```bash
APP_COMMIT=abc1234 DEPLOY_SKIP_BUILD=1 ./deploy.sh
```

- `abc1234` es la versión que muestra *Mi perfil* o `https://tu-dominio/actuator/info`.
- Si la versión nueva había cambiado la base de datos, primero restaura la copia que `deploy.sh` hizo antes de actualizar: `import-data.sh` con ese archivo de `backups/`.

## 5. Verificar en cualquier momento

```bash
cd /opt/mantasguajiras/deploy && ./verify.sh https://tu-dominio
```

Otros comandos útiles:

- Estado de los servicios: `docker compose ps`.
- Versión instalada: `https://tu-dominio/actuator/info`, o *Mi perfil* en la app.
- Log del backend: `docker compose logs --tail 100 backend`. Qué buscar: `docs/12` §3 y §6.
- Log de las copias: `docker compose logs backup`, o `backups/backup.log`.

## 6. Copias en el servidor

El servicio `backup` guarda las copias en `deploy/backups/` (`BACKUP_DIR`) y una segunda copia en `BACKUP_COPY_TO`.

**La segunda copia debe estar fuera del servidor.** Si el servidor se pierde, se pierden las dos. Opciones:

- **Carpeta en la nube (recomendado):** instalar `rclone`, conectarlo a Google Drive u OneDrive (`rclone config`) y programar con `crontab -e`:

  ```text
  15 6-22/2 * * * rclone copy /opt/mantasguajiras/deploy/backups gdrive:MantasGuajiras-copias --include "mantas_*"
  ```

- **Tu PC:** una tarea programada de Windows que traiga las copias con `scp`.

**Restaurar en una emergencia:** `./import-data.sh backups/ARCHIVO.dump`. Toma la copia más reciente con su `.sha256`.

Prueba de restauración a mano:

```bash
docker compose exec -T backup /scripts/restore-test.sh
```

## 7. Seguridad del servidor

- **Puertos:** solo 22 (SSH), 80 y 443 (`ufw` y el firewall del proveedor). PostgreSQL no publica su puerto: ni con el firewall apagado se puede conectar alguien desde fuera.
  - Docker abre por su cuenta los puertos que publica, sin pasar por `ufw`. Aquí solo publica 80 y 443, a propósito.
- **SSH:** cuando entres con llave, desactiva la contraseña: `PasswordAuthentication no` en `/etc/ssh/sshd_config` y `systemctl restart ssh`.
- **Claves:** solo en `deploy/.env`. `deploy.sh` le deja permisos solo para el dueño (`chmod 600`).
- **Cabeceras:** HSTS, `X-Content-Type-Options`, `X-Frame-Options` y `Referrer-Policy`, más la CSP de la app. Las comprueba `verify.sh`.
- **Actuator:** solo `health` e `info` (sin datos sensibles). Swagger está apagado.

## 8. Pruebas realizadas (24/11/2026)

Kit completo en un servidor Linux con Docker, HTTPS con certificado local y `SITE_ADDRESS=localhost`:

| Prueba | Resultado |
| --- | --- |
| Instalación desde cero | 22 s, **18/18 comprobaciones** |
| Prueba de punta a punta **por HTTPS, mismo dominio** (12 escenarios, 53 comprobaciones; incluye vender sin conexión, reabrir sin conexión y sincronizar) | **Todo correcto** |
| Puertos | Solo `web` publica 80/443. PostgreSQL inaccesible desde fuera |
| Usuario de la base | `mantas_app`, sin permisos de administrador, dueño de todas las tablas |
| Actualización 4f5cd7e → 4a8d9a1 → 66f595f con copia previa | 13–22 s, verificada; **≈9 s sin servicio** |
| Versión rota a propósito | Falla 7 comprobaciones → **vuelve sola a la anterior** y la verifica (15 s) |
| **PostgreSQL se cae de golpe** (`kill -9`) con 5 vendedores vendiendo | Se reinicia solo; sistema UP en **1,2 s**; **0 de 4.557** ventas confirmadas perdidas; integridad correcta |
| **Corte de luz simulado** (todos los procesos muertos de golpe, incluido Docker) con ventas en curso | Todo arranca solo; UP **14 s** después del corte; PostgreSQL se recupera de su registro (WAL) en 0,04 s; **0 de 8.602** ventas confirmadas perdidas; integridad correcta |
| El backend muere de golpe (como al agotar la memoria) | Vuelve solo en 9 s |
| Importar datos del PC (41.259 ventas, PostgreSQL 16 → 17) | 15 s; datos idénticos; integridad correcta; los vendedores entran con su contraseña de siempre |
| Prueba de restauración dentro del kit | Correcta |

**Hallazgo corregido.** Con la base caída, las ventas recibían "sesión vencida" (401) y la app habría mandado a los vendedores a iniciar sesión. Ahora:

- El servidor responde **503 "La base de datos no responde…"**.
- La app guarda la venta en el dispositivo y la envía sola al recuperarse, una sola vez (escenario 12 de la prueba de punta a punta).

La CI de GitHub repite en cada cambio estos pasos con las imágenes reales (trabajo *Kit de producción*):

- instalación y verificación;
- puertos;
- prueba de punta a punta por HTTPS;
- copia y prueba de restauración.
