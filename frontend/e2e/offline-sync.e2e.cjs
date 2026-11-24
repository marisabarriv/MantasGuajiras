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
 *
 * Ejecutar:  node e2e/offline-sync.e2e.cjs
 */
const { chromium } = require('playwright');

const APP = process.env.E2E_APP || 'http://localhost:4173';
const API = process.env.E2E_API || 'http://localhost:8080';
const ADMIN_USER = process.env.E2E_ADMIN_USER || 'admine2e';
const ADMIN_PASSWORD = process.env.E2E_ADMIN_PASSWORD || 'Prueba123';
const SHOTS = process.env.E2E_SCREENSHOTS || '';

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
    page.once('dialog', (dialog) => dialog.accept());
    await page.getByRole('button', { name: 'Cerrar sesión' }).click();
    await page.waitForURL(APP + '/');
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

    const browser = await chromium.launch();
    const context = await browser.newContext({ viewport: { width: 1280, height: 860 } });
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
    step('7. Actualización automática y seguridad de contenido');
    await page.goto(APP + '/inventory');
    await page.locator('.inv-row', { hasText: grande.name }).waitFor();
    await api('/api/inventory/adjustments', {
        method: 'POST', token: adminToken,
        body: { productId: grande.id, countedQuantity: 4, reason: 'E2E: conteo' },
    });
    await page.evaluate(() => document.dispatchEvent(new Event('visibilitychange')));
    await page.waitForFunction((name) => {
        const row = [...document.querySelectorAll('.inv-row')].find((r) => r.innerText.includes(name));
        return row && /\b4 unidades\b/.test(row.innerText);
    }, grande.name, { timeout: 10000 }).then(() => true, () => false)
        .then((ok) => check(ok, 'el inventario se actualiza solo al volver a la app'));
    check(cspViolations.length === 0, `sin violaciones de la política de seguridad (CSP)${cspViolations.length ? `: ${cspViolations[0]}` : ''}`);

    await browser.close();

    console.log(failures ? `\nRESULTADO: ${failures} FALLA(S)` : '\nRESULTADO: TODO CORRECTO');
    process.exit(failures ? 1 : 0);
})().catch((error) => {
    console.error('\nERROR:', error);
    process.exit(1);
});
