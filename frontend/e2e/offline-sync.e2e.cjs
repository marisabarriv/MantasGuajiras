/*
 * Prueba de punta a punta: ventas sin conexión y sincronización.
 *
 * Usa un navegador real (Playwright + Chromium) contra un backend y una
 * base de datos DE PRUEBA. Nunca la ejecutes contra la base de trabajo:
 * crea productos, usuarios y ventas.
 *
 * Requisitos (ver frontend/e2e/README.md):
 *   - Backend en E2E_API (por defecto http://localhost:8080) con una base vacía.
 *   - Frontend COMPILADO y servido en E2E_APP (por defecto http://localhost:4173):
 *       VITE_API_URL=http://localhost:8080 npm run build && npm run preview
 *   - E2E_ADMIN_USER / E2E_ADMIN_PASSWORD: administrador inicial de esa base.
 *   - Contra el kit de producción (deploy/) con certificado local:
 *       E2E_APP=https://localhost E2E_API=https://localhost E2E_INSECURE_TLS=1
 *
 * Ejecutar:  node e2e/offline-sync.e2e.cjs
 */
const { chromium } = require('playwright');

const APP = process.env.E2E_APP || 'http://localhost:4173';
const API = process.env.E2E_API || 'http://localhost:8080';
const ADMIN_USER = process.env.E2E_ADMIN_USER || 'admine2e';
const ADMIN_PASSWORD = process.env.E2E_ADMIN_PASSWORD || 'Prueba123';
const SHOTS = process.env.E2E_SCREENSHOTS || '';
// Certificado local de Caddy (SITE_ADDRESS=localhost): aceptarlo en la prueba.
const INSECURE_TLS = process.env.E2E_INSECURE_TLS === '1';
if (INSECURE_TLS) {
    process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';
}

const RUN = Date.now().toString(36).slice(-5);
let failures = 0;

const step = (msg) => console.log(`\n▶ ${msg}`);
function check(condition, msg) {
    console.log(`  ${condition ? '✔' : '✘'} ${msg}`);
    if (!condition) failures++;
}

// ---------------------------------------------------------------
// API (preparación de datos y verificación del servidor)
// ---------------------------------------------------------------

async function api(path, { method = 'GET', body, token } = {}) {
    const response = await fetch(API + path, {
        method,
        headers: {
            'Content-Type': 'application/json',
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: body === undefined ? undefined : JSON.stringify(body),
    });
    const text = await response.text();
    const data = text ? JSON.parse(text) : {};
    if (!response.ok) {
        const error = new Error(`${method} ${path} → ${response.status}: ${data.message || text}`);
        error.status = response.status;
        throw error;
    }
    return data;
}

async function login(identifier, password) {
    return (await api('/auth/login', { method: 'POST', body: { identifier, password } })).token;
}

async function stockOf(token, productId) {
    const inventory = await api('/api/inventory', { token });
    return Number(inventory.find((item) => item.productId === productId)?.quantity);
}

async function salesWith(token, clientRequestId) {
    const sales = await api('/api/sales', { token });
    return sales.filter((sale) => sale.clientRequestId === clientRequestId);
}

// ---------------------------------------------------------------
// Navegador
// ---------------------------------------------------------------

async function uiLogin(page, username, password) {
    await page.goto(APP + '/');
    await page.fill('#identifier', username);
    await page.fill('#loginPassword', password);
    await page.getByRole('button', { name: 'Iniciar sesión' }).click();
    await page.waitForURL('**/sale');
}

async function uiLogout(page) {
    // Acepta los avisos de esta salida (ventas pendientes, datos sin guardar)
    // y deja de escuchar al terminar, para no responder diálogos ajenos.
    const accept = (dialog) => dialog.accept().catch(() => {});
    page.on('dialog', accept);
    try {
        await page.getByRole('button', { name: 'Cerrar sesión' }).click();
        await page.waitForURL(APP + '/');
    } finally {
        page.off('dialog', accept);
    }
}

async function sell(page, productName) {
    await page.locator('.product-card', { hasText: productName })
        .getByRole('button', { name: 'Agregar' }).click();
    await page.getByRole('button', { name: 'Finalizar venta' }).click();
    await page.getByRole('button', { name: 'Confirmar venta' }).click();
}

async function closeSaleModal(page) {
    await page.getByRole('button', { name: 'Nueva venta' }).click();
}

const pendingIds = (page) => page.evaluate(() =>
    JSON.parse(localStorage.getItem('mg-offline-sales') || '[]').map((sale) => sale.clientRequestId));

// ---------------------------------------------------------------

(async () => {
    const adminToken = await login(ADMIN_USER, ADMIN_PASSWORD);

    step('Preparación: productos y dos vendedores');
    const categories = await api('/api/product-categories', { token: adminToken });
    const units = await api('/api/unit', { token: adminToken });
    const manta = categories.find((c) => c.name === 'Manta');
    const unidad = units.find((u) => u.abbreviation === '#');

    const newBlanket = (name, stock) => api('/api/products', {
        method: 'POST',
        token: adminToken,
        body: {
            categoryId: manta.id, unitId: unidad.id, name,
            unitPrice: 100000, minimumWholesaleQuantity: 0,
            purchasable: true, manufacturable: false, active: true,
            initialStock: stock,
        },
    });

    const grande = await newBlanket(`E2E Grande ${RUN}`, 10);
    const ultima = await newBlanket(`E2E Ultima ${RUN}`, 1);

    const sellers = ['a', 'b'].map((suffix) => ({ username: `v${suffix}${RUN}`, password: 'Clave123' }));
    for (const [index, seller] of sellers.entries()) {
        await api('/users', {
            method: 'POST',
            token: adminToken,
            body: {
                ...seller, role: 'SELLER', active: true,
                phone: `30099${RUN.length}${index}${Date.now().toString().slice(-5)}`,
            },
        });
    }
    const [sellerA, sellerB] = sellers;
    check(true, `productos ${grande.internalCode}, ${ultima.internalCode}; vendedores ${sellerA.username}, ${sellerB.username}`);

    // Con certificado local, Chromium solo registra el service worker (modo
    // sin conexión) si se le indica aceptar ese certificado al arrancar.
    const browser = await chromium.launch({ args: INSECURE_TLS ? ['--ignore-certificate-errors'] : [] });
    const context = await browser.newContext({ viewport: { width: 1280, height: 860 }, ignoreHTTPSErrors: INSECURE_TLS });
    const page = await context.newPage();

    const cspViolations = [];
    page.on('console', (message) => {
        if (/Content Security Policy/i.test(message.text())) cspViolations.push(message.text());
    });
    page.on('pageerror', (error) => console.log('  [error de página]', error.message));

    // -----------------------------------------------------------
    step('1. Venta sin conexión, app reabierta sin conexión, sincronización');
    await uiLogin(page, sellerA.username, sellerA.password);
    await page.locator('.product-card', { hasText: grande.name }).waitFor();
    await page.evaluate(() => navigator.serviceWorker.ready);
    await page.waitForTimeout(1500);

    await context.setOffline(true);
    await sell(page, grande.name);
    await page.getByText('quedó guardada en este dispositivo').waitFor();
    await closeSaleModal(page);
    const [firstId] = await pendingIds(page);
    check(Boolean(firstId), 'la venta quedó en la cola del dispositivo');

    await page.reload();
    await page.getByText('Vendiendo sin conexión.').waitFor({ timeout: 15000 });
    check(await page.getByText('Ventas guardadas sin conexión (1)').isVisible(), 'la app reabre sin conexión y conserva la venta pendiente');
    if (SHOTS) await page.screenshot({ path: `${SHOTS}/e2e-1-sin-conexion.png` });

    await context.setOffline(false);
    await page.waitForFunction(() => JSON.parse(localStorage.getItem('mg-offline-sales') || '[]').length === 0, null, { timeout: 40000 });
    const synced = await salesWith(adminToken, firstId);
    check(synced.length === 1, 'al volver la conexión llegó UNA sola vez');
    check(Boolean(synced[0]?.offlineCreatedAt), 'conserva la hora real de la venta');
    check(synced[0]?.sellerUsername === sellerA.username, 'queda a nombre del vendedor que la hizo');
    check(await stockOf(adminToken, grande.id) === 9, 'existencias 10 → 9');

    // -----------------------------------------------------------
    step('2. Se corta la conexión DESPUÉS de que el servidor guardó la venta');
    await context.setOffline(true);
    await page.reload().catch(() => {});
    await page.locator('.product-card', { hasText: grande.name }).waitFor({ timeout: 15000 });
    await sell(page, grande.name);
    await page.getByText('quedó guardada en este dispositivo').waitFor();
    await closeSaleModal(page);
    const [lostId] = await pendingIds(page);

    // El servidor recibe y guarda la venta, pero la respuesta nunca llega.
    let deliveredButLost = 0;
    await page.route('**/api/sales', async (route) => {
        if (route.request().method() !== 'POST') return route.continue();
        await route.fetch();
        deliveredButLost++;
        await route.abort('connectionreset');
    });
    await context.setOffline(false);
    for (let i = 0; i < 80 && deliveredButLost === 0; i++) {
        await page.waitForTimeout(500);
    }
    await page.waitForTimeout(500);
    check(deliveredButLost >= 1, 'el servidor recibió la venta pero el dispositivo no recibió respuesta');
    check((await pendingIds(page)).includes(lostId), 'el dispositivo la mantiene pendiente (no sabe si llegó)');

    await page.unroute('**/api/sales');
    // Equivale a "vuelve la conexión": dispara el reintento sin esperar 30 s.
    await page.evaluate(() => window.dispatchEvent(new Event('online')));
    await page.waitForFunction(() => JSON.parse(localStorage.getItem('mg-offline-sales') || '[]').length === 0, null, { timeout: 40000 });
    check((await salesWith(adminToken, lostId)).length === 1, 'al reintentar, el servidor la reconoce: NO se duplica');
    check(await stockOf(adminToken, grande.id) === 8, 'existencias 9 → 8 (descontada una sola vez)');

    // -----------------------------------------------------------
    step('3. Conflicto: otro vendedor vende la última unidad mientras tanto');
    await context.setOffline(true);
    await page.reload().catch(() => {});
    await page.locator('.product-card', { hasText: ultima.name }).waitFor({ timeout: 15000 });
    await sell(page, ultima.name);
    await page.getByText('quedó guardada en este dispositivo').waitFor();
    await closeSaleModal(page);
    const [conflictId] = await pendingIds(page);

    await api('/api/sales', {
        method: 'POST', token: adminToken,
        body: { items: [{ productId: ultima.id, quantity: 1, specialPrice: false }] },
    });

    await context.setOffline(false);
    await page.getByText('No se registró:').waitFor({ timeout: 40000 });
    check(await stockOf(adminToken, ultima.id) === 0, 'el inventario no queda negativo');
    check((await salesWith(adminToken, conflictId)).length === 0, 'la venta en conflicto no se registró');
    if (SHOTS) await page.screenshot({ path: `${SHOTS}/e2e-3-conflicto.png`, fullPage: true });

    // Llegó mercancía: se ajusta el inventario y se reintenta.
    await api('/api/inventory/adjustments', {
        method: 'POST', token: adminToken,
        body: { productId: ultima.id, countedQuantity: 1, reason: 'E2E: llegó una unidad' },
    });
    await page.getByRole('button', { name: 'Reintentar' }).click();
    await page.waitForFunction(() => JSON.parse(localStorage.getItem('mg-offline-sales') || '[]').length === 0, null, { timeout: 40000 });
    check((await salesWith(adminToken, conflictId)).length === 1, 'tras el ajuste, "Reintentar" la registra');
    check(await stockOf(adminToken, ultima.id) === 0, 'existencias 1 → 0');

    // -----------------------------------------------------------
    step('4. Dispositivo compartido: otro vendedor inicia sesión con ventas pendientes');
    await context.setOffline(true);
    await page.reload().catch(() => {});
    await page.locator('.product-card', { hasText: grande.name }).waitFor({ timeout: 15000 });
    await sell(page, grande.name);
    await page.getByText('quedó guardada en este dispositivo').waitFor();
    await closeSaleModal(page);
    const [sharedId] = await pendingIds(page);

    let logoutWarning = '';
    page.once('dialog', (dialog) => { logoutWarning = dialog.message(); dialog.accept(); });
    await page.getByRole('button', { name: 'Cerrar sesión' }).click();
    await page.waitForURL(APP + '/');
    check(logoutWarning.includes('sin conexión'), 'al cerrar sesión avisa que hay ventas sin enviar');

    await context.setOffline(false);
    await uiLogin(page, sellerB.username, sellerB.password);
    await page.getByText('otro usuario').waitFor({ timeout: 10000 });
    await page.waitForTimeout(2000);
    check((await salesWith(adminToken, sharedId)).length === 0, 'la sesión de otro vendedor NO envía ventas ajenas');
    check((await pendingIds(page)).includes(sharedId), 'la venta sigue guardada en el dispositivo');

    await uiLogout(page);
    await uiLogin(page, sellerA.username, sellerA.password);
    await page.waitForFunction(() => JSON.parse(localStorage.getItem('mg-offline-sales') || '[]').length === 0, null, { timeout: 40000 });
    const shared = await salesWith(adminToken, sharedId);
    check(shared.length === 1 && shared[0].sellerUsername === sellerA.username, 'al volver el vendedor original, se envía a su nombre');
    check(await stockOf(adminToken, grande.id) === 7, 'existencias 8 → 7');

    // -----------------------------------------------------------
    step('5. Compra: se corta la conexión al guardar y se pulsa otra vez');
    await uiLogout(page);
    await uiLogin(page, ADMIN_USER, ADMIN_PASSWORD);
    const tela = await api('/api/products', {
        method: 'POST', token: adminToken,
        body: {
            categoryId: categories.find((c) => c.name === 'Tela').id,
            unitId: units.find((u) => u.abbreviation === 'm').id,
            name: `E2E Tela ${RUN}`, unitPrice: 20000, minimumWholesaleQuantity: 0,
            purchasable: true, manufacturable: false, active: true, initialStock: 5,
        },
    });
    const purchasesBefore = (await api('/api/purchases', { token: adminToken })).length;

    await page.goto(APP + '/purchases');
    await page.getByRole('button', { name: '+ Registrar compra' }).click();
    await page.locator('select[id^="product-"]').first().selectOption(tela.id);
    await page.locator('input[id^="qty-"]').first().fill('3');
    await page.locator('input[id^="cost-"]').first().fill('20000');

    let purchaseLost = 0;
    await page.route('**/api/purchases', async (route) => {
        if (route.request().method() !== 'POST') return route.continue();
        await route.fetch();          // el servidor la guarda...
        purchaseLost++;
        await route.abort('connectionreset'); // ...pero la respuesta no llega
    });
    await page.getByRole('button', { name: 'Registrar compra', exact: true }).click();
    await page.getByText('Puedes volver a pulsar Guardar').waitFor({ timeout: 30000 });
    check(purchaseLost === 1, 'el servidor recibió la compra y la pantalla muestra el error de conexión');

    await page.unroute('**/api/purchases');
    await page.getByRole('button', { name: 'Registrar compra', exact: true }).click();
    await page.locator('.result.success, [role="status"]').first().waitFor({ timeout: 15000 });
    const purchasesAfter = (await api('/api/purchases', { token: adminToken })).length;
    check(purchasesAfter === purchasesBefore + 1, 'al pulsar otra vez se registra UNA sola compra');
    check(await stockOf(adminToken, tela.id) === 8, 'la tela sube una sola vez (5 + 3)');

    // -----------------------------------------------------------
    step('6. Ajuste con motivo obligatorio y filtros del historial');
    await page.goto(APP + '/inventory');
    const telaRow = page.locator('.inv-row', { hasText: tela.name });
    await telaRow.getByRole('button', { name: 'Ajustar' }).click();
    await page.fill('#countedQuantity', '7');
    const saveAdjust = page.locator('form button[type="submit"]').last();
    check(await saveAdjust.isDisabled(), 'sin motivo no se puede guardar el ajuste');
    await page.fill('#adjustReason', 'E2E: conteo con motivo');
    check(await saveAdjust.isEnabled(), 'con motivo se habilita');
    await saveAdjust.click();
    await page.waitForFunction(() => !document.querySelector('#adjustReason'), null, { timeout: 10000 });
    check(await stockOf(adminToken, tela.id) === 7, 'existencias 8 → 7');

    await page.getByRole('tab', { name: /Historial/ }).click();
    await page.locator('.history-filters select').nth(1).selectOption('ADJUSTMENT');
    await page.locator('.history-filters input[type="search"]').fill(ADMIN_USER);
    await page.waitForTimeout(1500);
    const kinds = await page.locator('.inv-movement-kind').allInnerTexts();
    check(kinds.length > 0 && kinds.every((kind) => /ajuste/i.test(kind)), `el filtro "Ajuste" solo muestra ajustes (${kinds.length})`);
    const owners = await page.locator('.inv-movement-meta').allInnerTexts();
    check(owners.every((meta) => meta.includes(ADMIN_USER)), 'el filtro de usuario solo muestra los del administrador');
    if (SHOTS) await page.screenshot({ path: `${SHOTS}/e2e-6-filtros.png`, fullPage: true });

    // -----------------------------------------------------------
    step('7. Dos pestañas abiertas sincronizan a la vez la misma venta');
    await page.goto(APP + '/sale');
    await page.locator('.product-card', { hasText: grande.name }).waitFor();
    const secondTab = await context.newPage();
    await secondTab.goto(APP + '/sale');
    await secondTab.locator('.product-card', { hasText: grande.name }).waitFor();
    const stockBeforeTabs = await stockOf(adminToken, grande.id);

    await context.setOffline(true);
    await sell(page, grande.name);
    await page.getByText('quedó guardada en este dispositivo').waitFor();
    await closeSaleModal(page);
    const [tabsId] = await pendingIds(page);
    await secondTab.waitForFunction((id) =>
        JSON.parse(localStorage.getItem('mg-offline-sales') || '[]').some((sale) => sale.clientRequestId === id),
    tabsId, { timeout: 10000 });
    check(true, 'la otra pestaña ve la venta pendiente');

    await context.setOffline(false); // las dos pestañas intentan enviarla
    for (const tab of [page, secondTab]) {
        await tab.waitForFunction(() => JSON.parse(localStorage.getItem('mg-offline-sales') || '[]').length === 0, null, { timeout: 40000 });
    }
    check((await salesWith(adminToken, tabsId)).length === 1, 'se registró UNA sola vez');
    check(await stockOf(adminToken, grande.id) === stockBeforeTabs - 1, 'el inventario bajó una sola vez');
    await secondTab.close();

    // -----------------------------------------------------------
    step('8. La sesión vence sin conexión: se sigue vendiendo y se envía al volver a entrar');
    await uiLogout(page);
    await uiLogin(page, sellerA.username, sellerA.password);
    await page.locator('.product-card', { hasText: grande.name }).waitFor();

    await context.setOffline(true);
    await page.evaluate(() => {
        // Simula que el token venció mientras no había conexión.
        const [header, payload, signature] = localStorage.getItem('token').split('.');
        const data = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
        data.exp = Math.floor(Date.now() / 1000) - 60;
        const encoded = btoa(JSON.stringify(data)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
        localStorage.setItem('token', [header, encoded, signature].join('.'));
    });
    await page.reload().catch(() => {});
    await page.getByText('Vendiendo sin conexión.').waitFor({ timeout: 15000 });
    check(page.url().endsWith('/sale'), 'con la sesión vencida y sin conexión sigue en Nueva venta');

    await sell(page, grande.name);
    await page.getByText('quedó guardada en este dispositivo').waitFor();
    await closeSaleModal(page);
    const [expiredId] = await pendingIds(page);

    await context.setOffline(false);
    await page.waitForURL((url) => url.pathname === '/', { timeout: 70000 });
    check((await pendingIds(page)).includes(expiredId), 'al volver la conexión pide iniciar sesión y la venta sigue guardada');
    check((await salesWith(adminToken, expiredId)).length === 0, 'no se envía sin una sesión válida');

    await uiLogin(page, sellerA.username, sellerA.password);
    await page.waitForFunction(() => JSON.parse(localStorage.getItem('mg-offline-sales') || '[]').length === 0, null, { timeout: 40000 });
    const expiredSale = await salesWith(adminToken, expiredId);
    check(expiredSale.length === 1 && expiredSale[0].sellerUsername === sellerA.username,
        'tras iniciar sesión se envía una vez, a nombre del vendedor');

    // -----------------------------------------------------------
    step('9. Sin espacio en el dispositivo: la venta NO se da por guardada');
    await page.locator('.product-card', { hasText: grande.name }).waitFor();
    await context.setOffline(true);
    await page.evaluate(() => {
        const original = Storage.prototype.setItem;
        window.__restoreSetItem = () => { Storage.prototype.setItem = original; };
        Storage.prototype.setItem = function (key, value) {
            if (key === 'mg-offline-sales') {
                throw new DOMException('Sin espacio', 'QuotaExceededError');
            }
            return original.call(this, key, value);
        };
    });
    await sell(page, grande.name);
    await page.getByText('anótala en papel').waitFor({ timeout: 15000 });
    check(true, 'avisa que la venta NO se registró');
    check((await pendingIds(page)).length === 0, 'no queda una venta fantasma en la cola');
    check(await page.locator('.pos-cart-row', { hasText: grande.name }).count() === 1, 'el carrito se conserva para reintentar');

    await page.evaluate(() => window.__restoreSetItem());
    await page.getByRole('button', { name: 'Confirmar venta' }).click();
    await page.getByText('quedó guardada en este dispositivo').waitFor();
    await closeSaleModal(page);
    await context.setOffline(false);
    await page.waitForFunction(() => JSON.parse(localStorage.getItem('mg-offline-sales') || '[]').length === 0, null, { timeout: 40000 });
    check(true, 'con espacio disponible se guarda y luego se envía');

    // -----------------------------------------------------------
    step('10. Recorrido por todas las páginas con cada rol');
    const pages = [
        ['/sale', 'Nueva venta'], ['/custom-orders', 'Mantas personalizadas'], ['/products', 'Productos'],
        ['/inventory', 'Inventario'], ['/production', 'Producción'], ['/profile', 'Mi perfil'],
        ['/sales', 'Ventas'], ['/purchases', 'Compras'], ['/admin', 'Administrar usuarios'],
    ];
    const adminOnly = ['/sales', '/purchases', '/admin'];
    const pageErrors = [];
    const onPageError = (error) => pageErrors.push(`${page.url()}: ${error.message}`);
    page.on('pageerror', onPageError);

    for (const [who, user, password] of [['administrador', ADMIN_USER, ADMIN_PASSWORD], ['vendedor', sellerA.username, sellerA.password]]) {
        await uiLogout(page).catch(() => {});
        await uiLogin(page, user, password);
        const isAdmin = who === 'administrador';
        let loaded = 0;
        for (const [path, title] of pages) {
            await page.goto(APP + path);
            const restricted = !isAdmin && adminOnly.includes(path);
            const heading = restricted ? 'Sección restringida' : title;
            const ok = await page.getByRole('heading', { level: 1, name: heading }).waitFor({ timeout: 10000 })
                .then(() => true, () => false);
            if (ok) loaded++;
            else console.log(`    no cargó ${path} como ${who}`);
        }
        check(loaded === pages.length, `${who}: las ${pages.length} páginas cargan (las de administrador muestran "Sección restringida" al vendedor)`);

        const menu = await page.locator('.sidebar-nav').innerText();
        check(isAdmin === /Ventas/.test(menu) && isAdmin === /Compras/.test(menu) && isAdmin === /Administrar usuarios/.test(menu),
            `${who}: el menú muestra solo lo que le corresponde`);

        await page.goto(APP + '/products');
        await page.getByRole('heading', { level: 1, name: 'Productos' }).waitFor();
        // Esperar a que termine de cargar (con muchos productos tarda un poco más).
        await page.getByText('Cargando productos').waitFor({ state: 'detached', timeout: 15000 }).catch(() => {});
        if (isAdmin) {
            await page.getByRole('button', { name: '+ Crear producto' }).waitFor({ timeout: 10000 }).catch(() => {});
        }
        const canCreate = await page.getByRole('button', { name: '+ Crear producto' }).count();
        check(isAdmin ? canCreate === 1 : canCreate === 0, `${who}: ${isAdmin ? 'puede' : 'no puede'} crear productos`);

        await page.goto(APP + '/inventory');
        await page.locator('.inv-row').first().waitFor();
        check(await page.getByRole('button', { name: 'Ajustar' }).count() > 0, `${who}: puede ajustar inventario por conteo`);
    }
    page.off('pageerror', onPageError);
    check(pageErrors.length === 0, `sin errores de JavaScript en ninguna página${pageErrors.length ? `: ${pageErrors[0]}` : ''}`);

    // -----------------------------------------------------------
    step('11. Actualización automática y seguridad de contenido');
    await page.goto(APP + '/inventory');
    await page.locator('.inv-row', { hasText: grande.name }).waitFor();
    // Otro usuario cuenta y deja 2 unidades menos (el valor depende de los pasos anteriores).
    const counted = (await stockOf(adminToken, grande.id)) - 2;
    await api('/api/inventory/adjustments', {
        method: 'POST', token: adminToken,
        body: { productId: grande.id, countedQuantity: counted, reason: 'E2E: conteo' },
    });
    await page.evaluate(() => document.dispatchEvent(new Event('visibilitychange')));
    await page.waitForFunction(({ name, expected }) => {
        const row = [...document.querySelectorAll('.inv-row')].find((r) => r.innerText.includes(name));
        return row && new RegExp(`\\b${expected} unidad(es)?\\b`).test(row.innerText);
    }, { name: grande.name, expected: counted }, { timeout: 10000 }).then(() => true, () => false)
        .then((ok) => check(ok, 'el inventario se actualiza solo al volver a la app'));
    check(cspViolations.length === 0, `sin violaciones de la política de seguridad (CSP)${cspViolations.length ? `: ${cspViolations[0]}` : ''}`);

    // -----------------------------------------------------------
    step('12. El servidor falla al guardar (p. ej. la base de datos se cae)');
    await api('/api/inventory/adjustments', {
        method: 'POST', token: adminToken,
        body: { productId: grande.id, countedQuantity: 5, reason: 'E2E: base caída' },
    });
    await page.goto(APP + '/sale');
    await page.locator('.product-card', { hasText: grande.name }).waitFor();
    let failedOnce = false;
    await page.route('**/api/sales', async (route) => {
        if (route.request().method() !== 'POST' || failedOnce) return route.continue();
        failedOnce = true;
        await route.fulfill({ status: 500, contentType: 'application/json',
            body: JSON.stringify({ status: 500, message: 'Ha ocurrido un error interno. Código de referencia: e2e00000.' }) });
    });
    await sell(page, grande.name);
    const keptOnDevice = await page.getByText('quedó guardada en este dispositivo').waitFor({ timeout: 15000 })
        .then(() => true, () => false);
    check(keptOnDevice, 'la venta NO se pierde: queda guardada en el dispositivo');
    await closeSaleModal(page).catch(() => {});
    const [serverDownId] = await pendingIds(page);
    await page.unroute('**/api/sales');
    await page.evaluate(() => window.dispatchEvent(new Event('online')));
    await page.waitForFunction(() => JSON.parse(localStorage.getItem('mg-offline-sales') || '[]').length === 0, null, { timeout: 40000 })
        .catch(() => {});
    check(serverDownId && (await salesWith(adminToken, serverDownId)).length === 1, 'cuando el servidor se recupera, se envía sola y una sola vez');
    check(await stockOf(adminToken, grande.id) === 4, 'existencias 5 → 4');

    // -----------------------------------------------------------
    step('13. Aviso al salir con datos sin guardar');
    await page.goto(APP + '/sale');
    await page.locator('.product-card', { hasText: grande.name }).getByRole('button', { name: 'Agregar' }).click();
    let asked = '';
    page.once('dialog', (dialog) => { asked = dialog.message(); dialog.dismiss(); });
    await page.locator('.sidebar-nav').getByRole('link', { name: 'Inventario' }).click();
    await page.waitForTimeout(300);
    check(/sin guardar/.test(asked) && page.url().endsWith('/sale'), 'con productos en el carrito, pregunta antes de cambiar de pantalla y se puede quedar');
    check(await page.locator('.pos-cart-row', { hasText: grande.name }).count() === 1, 'el carrito sigue intacto');
    page.once('dialog', (dialog) => dialog.accept());
    await page.locator('.sidebar-nav').getByRole('link', { name: 'Inventario' }).click();
    check(await page.waitForURL('**/inventory', { timeout: 5000 }).then(() => true, () => false), 'si confirma, cambia de pantalla');
    await page.locator('.inv-row', { hasText: grande.name }).getByRole('button', { name: 'Ajustar' }).click();
    await page.fill('#adjustReason', 'a medio escribir');
    asked = '';
    page.once('dialog', (dialog) => { asked = dialog.message(); dialog.dismiss(); });
    await page.keyboard.press('Escape');
    await page.waitForTimeout(300);
    check(Boolean(asked) && await page.locator('#adjustReason').isVisible(), 'formulario a medio llenar: Escape pregunta y no lo cierra');
    page.once('dialog', (dialog) => dialog.accept());
    await page.keyboard.press('Escape');
    check(await page.locator('#adjustReason').waitFor({ state: 'detached', timeout: 5000 }).then(() => true, () => false), 'si confirma, se cierra');

    await browser.close();

    console.log(failures ? `\nRESULTADO: ${failures} FALLA(S)` : '\nRESULTADO: TODO CORRECTO');
    process.exit(failures ? 1 : 0);
})().catch((error) => {
    console.error('\nERROR:', error);
    process.exit(1);
});
