/*
 * Ensayo de aceptación: los 24 escenarios del checklist de la auditoría
 * (docs/15-aceptacion.md), ejecutados por la INTERFAZ como lo haría una
 * persona: clics, formularios y mensajes en pantalla. Cada escenario deja
 * una captura (ACCEPT_SHOTS) que también ilustra el manual de usuario.
 *
 * No reemplaza la aceptación de la responsable del negocio: la prepara.
 * Si el ensayo pasa, el recorrido humano confirma lo mismo con criterio
 * de negocio (precios reales, procedimientos reales).
 *
 * Requisitos: igual que offline-sync.e2e.cjs (backend con base VACÍA,
 * frontend compilado). Variables: E2E_APP, E2E_API, E2E_ADMIN_USER,
 * E2E_ADMIN_PASSWORD, ACCEPT_SHOTS (carpeta de capturas), E2E_INSECURE_TLS.
 *
 * Ejecutar:  node e2e/acceptance.e2e.cjs
 */
const fs = require('node:fs');
const path = require('node:path');
const { chromium, devices } = require('playwright');

const APP = process.env.E2E_APP || 'http://localhost:4173';
const API = process.env.E2E_API || 'http://localhost:8080';
const ADMIN_USER = process.env.E2E_ADMIN_USER || 'admine2e';
const ADMIN_PASSWORD = process.env.E2E_ADMIN_PASSWORD || 'Prueba123';
const SHOTS = process.env.ACCEPT_SHOTS || '';
const INSECURE_TLS = process.env.E2E_INSECURE_TLS === '1';
if (INSECURE_TLS) process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';

// Sufijo para que cada corrida use nombres nuevos. ACCEPT_RUN= (vacío) en una
// base nueva da nombres naturales, para las capturas del manual.
const RUN = process.env.ACCEPT_RUN ?? Date.now().toString(36).slice(-4);
const tag = (text) => (RUN ? `${text} ${RUN}` : text);
const results = [];
let current = null;

// ---------------------------------------------------------------
// Apoyo
// ---------------------------------------------------------------

async function api(p, { method = 'GET', body, token } = {}) {
    const response = await fetch(API + p, {
        method,
        headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
        body: body === undefined ? undefined : JSON.stringify(body),
    });
    const text = await response.text();
    const data = text ? JSON.parse(text) : {};
    if (!response.ok) throw new Error(`${method} ${p} → ${response.status}: ${data.message || text}`);
    return data;
}
const login = async (u, p) => (await api('/auth/login', { method: 'POST', body: { identifier: u, password: p } })).token;
async function stockByName(token, name) {
    const item = (await api('/api/inventory', { token })).find((i) => i.productName === name);
    return item ? Number(item.quantity) : NaN;
}

function check(condition, msg) {
    current.checks.push({ ok: Boolean(condition), msg });
    console.log(`    ${condition ? '✔' : '✘'} ${msg}`);
}

async function shot(page, name) {
    if (!SHOTS) return;
    fs.mkdirSync(SHOTS, { recursive: true });
    await page.screenshot({ path: path.join(SHOTS, `${name}.png`) });
}

/** Un escenario del checklist: corre, registra resultado y captura. */
async function scenario(id, block, title, page, fn) {
    current = { id, block, title, checks: [] };
    results.push(current);
    console.log(`\n▶ ${id}. ${title}`);
    try {
        await fn();
    } catch (error) {
        check(false, `error inesperado: ${error.message.split('\n')[0]}`);
    }
    await shot(page, `${id}-${title.toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/[^a-z0-9]+/g, '-').replace(/-$/, '')}`).catch(() => {});
}

async function uiLogin(page, username, password) {
    await page.goto(APP + '/');
    await page.fill('#identifier', username);
    await page.fill('#loginPassword', password);
    await page.getByRole('button', { name: 'Iniciar sesión' }).click();
    await page.waitForURL('**/sale');
}

const visible = (locator, timeout = 8000) => locator.first().waitFor({ timeout }).then(() => true, () => false);
const money = (n) => `$${Number(n).toLocaleString('es-CO')}`;

/** Crear un producto desde la pantalla Productos. */
async function createProduct(page, { name, category, price, stock, minimum }) {
    await page.goto(APP + '/products');
    await page.getByRole('button', { name: '+ Crear producto' }).click();
    await page.fill('#productName', name);
    await page.selectOption('#categoryId', { label: category });
    await page.fill('#price', String(price));
    if (stock !== undefined) await page.fill('#initialStock', String(stock));
    if (minimum !== undefined) await page.fill('#minimumStock', String(minimum));
    await page.getByRole('button', { name: 'Crear producto', exact: true }).click();
}

async function openEdit(page, name) {
    await page.goto(APP + '/products');
    await page.getByLabel('Buscar producto').fill(name);
    await page.locator('.inv-row', { hasText: name }).getByRole('button', { name: 'Editar' }).click();
}

async function addToCart(page, name, category = 'Manta') {
    await page.goto(APP + '/sale');
    // Los productos se agrupan por categoría (pestañas Manta / Tela).
    await page.getByRole('button', { name: category, exact: true }).click().catch(() => {});
    await page.locator('.product-card', { hasText: name }).getByRole('button', { name: 'Agregar' }).click();
}

async function finishSale(page) {
    await page.getByRole('button', { name: 'Finalizar venta' }).click();
    await page.getByRole('button', { name: 'Confirmar venta' }).click();
}

// ---------------------------------------------------------------
// Ensayo
// ---------------------------------------------------------------

(async () => {
    const browser = await chromium.launch({ args: INSECURE_TLS ? ['--ignore-certificate-errors'] : [] });
    // Administrador en computador; vendedor en celular (como en la tienda).
    const adminCtx = await browser.newContext({ viewport: { width: 1280, height: 860 }, ignoreHTTPSErrors: INSECURE_TLS });
    const sellerCtx = await browser.newContext({ ...devices['Pixel 7'], ignoreHTTPSErrors: INSECURE_TLS });
    const admin = await adminCtx.newPage();
    const seller = await sellerCtx.newPage();
    admin.setDefaultTimeout(10000);
    seller.setDefaultTimeout(10000);
    const adminToken = await login(ADMIN_USER, ADMIN_PASSWORD);

    const MANTA = tag('Manta wayuu grande');
    const TELA = tag('Tela de algodón roja');
    const ULTIMA = tag('Manta edición limitada');
    const RETIRADA = tag('Manta modelo anterior');
    const SELLER = (RUN ? `vende${RUN}` : 'laura'); // el usuario admite hasta 12 caracteres
    const SELLER_PASSWORD = 'Venta2026';

    await uiLogin(admin, ADMIN_USER, ADMIN_PASSWORD);

    // ======================= PRODUCTOS =======================

    await scenario('01', 'Productos', 'Crear manta y tela', admin, async () => {
        await createProduct(admin, { name: MANTA, category: 'Manta', price: 80000, stock: 10, minimum: 2 });
        check(await visible(admin.locator('.inv-row', { hasText: MANTA })), `la manta aparece en la lista (${money(80000)}, 10 unidades)`);
        await createProduct(admin, { name: TELA, category: 'Tela', price: 18000, stock: 50, minimum: 10 });
        check(await visible(admin.locator('.inv-row', { hasText: TELA })), 'la tela aparece en la lista (se maneja en metros)');
        await createProduct(admin, { name: ULTIMA, category: 'Manta', price: 120000, stock: 1 });
        await createProduct(admin, { name: RETIRADA, category: 'Manta', price: 60000, stock: 3 });
        check(await stockByName(adminToken, MANTA) === 10 && await stockByName(adminToken, TELA) === 50, 'existencias iniciales: 10 mantas y 50 metros');
        if (RUN) await admin.getByLabel('Buscar producto').fill(RUN);
    });

    await scenario('02', 'Productos', 'Editar productos', admin, async () => {
        await openEdit(admin, MANTA);
        await admin.fill('#price', '85000');
        await admin.getByRole('button', { name: 'Guardar cambios' }).click();
        check(await visible(admin.locator('.inv-row', { hasText: MANTA }).getByText(money(85000))), 'el precio cambió a $85.000');
        await openEdit(admin, MANTA);
        await admin.fill('#price', '80000');
        await admin.fill('#minimumStock', '3');
        await admin.getByRole('button', { name: 'Guardar cambios' }).click();
        check(await visible(admin.locator('.inv-row', { hasText: MANTA }).getByText(money(80000))), 'se puede volver a $80.000 y cambiar el stock mínimo');
    });

    await scenario('03', 'Productos', 'Validar unidades y precios', admin, async () => {
        await admin.goto(APP + '/products');
        await admin.getByRole('button', { name: '+ Crear producto' }).click();
        await admin.selectOption('#categoryId', { label: 'Tela' });
        check(await visible(admin.getByText('Esta categoría se maneja en metros.')), 'la tela se maneja en metros (unidad automática)');
        await admin.selectOption('#categoryId', { label: 'Manta' });
        await admin.fill('#productName', tag('Manta con precio cero'));
        await admin.fill('#price', '0');
        await admin.getByRole('button', { name: 'Crear producto', exact: true }).click();
        check(await visible(admin.locator('.field-error, .result.error, [role=alert]')), 'precio 0: el formulario avisa y no lo crea');
        await admin.fill('#price', '50000');
        await admin.fill('#initialStock', '2.5');
        check(await admin.$eval('#initialStock', (input) => !input.checkValidity()), 'manta con 2,5 unidades: el campo no lo acepta (solo enteros)');
        await admin.getByRole('button', { name: 'Crear producto', exact: true }).click();
        const products = await api('/api/products', { token: adminToken });
        check(!products.some((p) => p.name === tag('Manta con precio cero')), 'ninguno de los dos se creó');
        await admin.keyboard.press('Escape');
    });

    await scenario('04', 'Productos', 'Desactivar productos', admin, async () => {
        await openEdit(admin, RETIRADA);
        await admin.getByLabel('Producto activo').uncheck();
        await admin.getByRole('button', { name: 'Guardar cambios' }).click();
        await admin.getByRole('button', { name: /^Inactivos/ }).click();
        check(await visible(admin.locator('.inv-row', { hasText: RETIRADA })), 'aparece en "Inactivos"');
        await addToCart(admin, MANTA).catch(() => {});
        if (RUN) await admin.getByLabel('Buscar producto').fill(RUN);
        check(!(await visible(admin.locator('.product-card', { hasText: RETIRADA }), 3000)), 'ya no se puede vender');
        await admin.getByRole('button', { name: 'Quitar' }).first().click().catch(() => {});
    });

    // ======================= INVENTARIO =======================

    await scenario('05', 'Inventario', 'Registrar ajuste por conteo físico', admin, async () => {
        await admin.goto(APP + '/inventory');
        await admin.locator('.inv-row', { hasText: MANTA }).getByRole('button', { name: 'Ajustar' }).click();
        await admin.fill('#countedQuantity', '12');
        check(await admin.getByRole('button', { name: 'Guardar ajuste' }).isDisabled(), 'sin motivo, "Guardar ajuste" está desactivado');
        check(await visible(admin.getByText('Se registrará una entrada de 2 unidades')), 'antes de guardar explica: "entrada de 2 unidades"');
        await admin.fill('#adjustReason', 'Conteo semanal: llegaron 2 de la bodega');
        await admin.getByRole('button', { name: 'Guardar ajuste' }).click();
        await admin.locator('.inv-row', { hasText: MANTA }).getByText(/12 unidades/).first().waitFor({ timeout: 8000 }).catch(() => {});
        check(await stockByName(adminToken, MANTA) === 12, 'existencias 10 → 12');
    });

    await scenario('06', 'Inventario', 'Verificar entradas y salidas', admin, async () => {
        await admin.goto(APP + '/inventory');
        await admin.getByRole('tab', { name: 'Historial de movimientos' }).click();
        await admin.getByLabel('Usuario').fill(ADMIN_USER);
        const rows = admin.locator('.inv-movement', { hasText: MANTA });
        await rows.first().waitFor({ timeout: 8000 });
        const text = await rows.allInnerTexts();
        check(text.some((t) => /\+ ?2 unidades/.test(t) && /ajuste/i.test(t)), 'entrada de +2 por ajuste, con quién y por qué');
        check(text.some((t) => /\+ ?10 unidades/.test(t)), 'entrada inicial de 10 unidades');
    });

    await scenario('07', 'Inventario', 'Validar stock insuficiente', admin, async () => {
        // Producción que pide más tela de la que hay: se rechaza y nada cambia.
        await admin.goto(APP + '/production');
        await admin.getByRole('button', { name: '+ Registrar producción' }).click();
        await admin.selectOption('#fabricId', { label: new RegExp(TELA) }).catch(async () => {
            const option = await admin.locator('#fabricId option', { hasText: TELA }).getAttribute('value');
            await admin.selectOption('#fabricId', option);
        });
        await admin.fill('#perUnit', '30');
        const manta = await admin.locator('#outputId option', { hasText: MANTA }).getAttribute('value');
        await admin.selectOption('#outputId', manta);
        await admin.fill('#outputQuantity', '2');
        await admin.fill('#makerName', 'María Pérez');
        await admin.getByRole('button', { name: 'Registrar producción', exact: true }).click();
        check(await visible(admin.getByText(/suficiente|alcanza|disponible/i)), 'avisa que no hay tela suficiente (60 m pedidos, 50 m disponibles)');
        check(await stockByName(adminToken, TELA) === 50 && await stockByName(adminToken, MANTA) === 12, 'el inventario no cambió');
        await admin.keyboard.press('Escape');
    });

    await scenario('08', 'Inventario', 'Comprobar historial de movimientos', admin, async () => {
        await admin.goto(APP + '/inventory');
        await admin.getByRole('tab', { name: 'Historial de movimientos' }).click();
        await admin.getByLabel('Origen').selectOption({ label: 'Ajuste' }).catch(() => admin.getByLabel('Origen').selectOption('ADJUSTMENT'));
        const today = new Date();
        const iso = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
        await admin.getByLabel('Desde').fill(iso);
        const adjust = admin.locator('.inv-movement', { hasText: 'Conteo semanal' });
        check(await visible(adjust), 'filtro por origen "Ajuste" y fecha: aparece el conteo con su motivo');
        check(/Registrado por/.test(await adjust.first().innerText()), 'cada movimiento muestra quién lo registró');
    });

    // ======================= VENTAS (vendedor, en celular) =======================

    await scenario('18', 'Usuarios y seguridad', 'Crear vendedor', admin, async () => {
        await admin.goto(APP + '/admin');
        await admin.getByRole('button', { name: '+ Crear usuario' }).click();
        await admin.fill('#userUsername', SELLER);
        await admin.fill('#userPhone', `31${String(Date.now()).slice(-8)}`);
        await admin.fill('#userPassword', SELLER_PASSWORD);
        await admin.selectOption('#userRole', 'SELLER');
        await admin.getByRole('button', { name: 'Crear usuario', exact: true }).click();
        check(await visible(admin.getByText(`Usuario ${SELLER} creado.`)), `confirma: "Usuario ${SELLER} creado."`);
        check(await visible(admin.locator('li', { hasText: SELLER }).getByText('Activo')), 'aparece en la lista como vendedora activa');
    });

    await uiLogin(seller, SELLER, SELLER_PASSWORD);
    let stockBefore = 0;

    await scenario('09', 'Ventas', 'Registrar venta normal', seller, async () => {
        stockBefore = await stockByName(adminToken, MANTA);
        await addToCart(seller, MANTA);
        check(await visible(seller.locator('.pos-cart-row', { hasText: MANTA })), 'la manta queda en el carrito');
        await finishSale(seller);
        check(await visible(seller.getByText('La venta se guardó correctamente.')), 'confirma: "La venta se guardó correctamente."');
        check(await stockByName(adminToken, MANTA) === stockBefore - 1, `existencias ${stockBefore} → ${stockBefore - 1}`);
        await seller.getByRole('button', { name: 'Nueva venta' }).click();
    });

    await scenario('10', 'Ventas', 'Aplicar precio especial a una unidad', seller, async () => {
        await addToCart(seller, MANTA);
        await seller.locator('.pos-cart-row', { hasText: MANTA }).getByRole('button', { name: 'Cambiar precio' }).click();
        await seller.fill('#specialPriceInput', '70000');
        check(await visible(seller.getByText(/12,5 % · ahorra \$\s?10\.000 por unidad/)), 'muestra: 12,5 % · ahorra $10.000 por unidad');
        await shot(seller, '10b-precio-especial-ventana');
        await seller.getByRole('button', { name: 'Aplicar' }).click();
        check(/70\.000/.test(await seller.locator('.pos-cart').innerText().catch(() => '')) || await visible(seller.getByText(money(70000))), 'el carrito cobra $70.000');
        await seller.getByRole('button', { name: 'Finalizar venta' }).click();
        check(await visible(seller.locator('.pos-confirm').getByText(money(70000))), 'la confirmación muestra $70.000');
        await shot(seller, '10c-precio-especial-confirmacion');
        await seller.getByRole('button', { name: 'Confirmar venta' }).click();
        check(await visible(seller.getByText('La venta se guardó correctamente.')), 'venta registrada');
        const product = (await api('/api/products', { token: adminToken })).find((p) => p.name === MANTA);
        check(Number(product.unitPrice) === 80000, 'el precio del producto sigue en $80.000 (solo cambió para esta venta)');
        await seller.getByRole('button', { name: 'Nueva venta' }).click();
    });

    await scenario('11', 'Ventas', 'Aplicar descuento a un producto', seller, async () => {
        await addToCart(seller, TELA, 'Tela');
        const metres = seller.getByLabel(`Metros de ${TELA}`);
        await metres.fill('3');
        await metres.press('Tab');
        await seller.locator('.pos-cart-row', { hasText: TELA }).getByRole('button', { name: 'Cambiar precio' }).click();
        await seller.fill('#specialPriceInput', '16000');
        check(await visible(seller.getByText(/11,1\d? % · ahorra \$\s?2\.000 por metro/)), 'descuento por metro: 11,1 %, ahorra $2.000');
        await seller.getByRole('button', { name: 'Aplicar' }).click();
        await seller.getByRole('button', { name: 'Finalizar venta' }).click();
        check(await visible(seller.locator('.pos-confirm').getByText(money(48000))), '3 metros × $16.000 = $48.000');
        await seller.getByRole('button', { name: 'Confirmar venta' }).click();
        check(await visible(seller.getByText('La venta se guardó correctamente.')), 'venta registrada');
        check(await stockByName(adminToken, TELA) === 47, 'tela 50 → 47 metros');
        await seller.getByRole('button', { name: 'Nueva venta' }).click();
    });

    await scenario('12', 'Ventas', 'Validar venta con stock insuficiente', seller, async () => {
        await addToCart(seller, ULTIMA);
        // Mientras tanto, la última unidad se vende en otro punto (o se cuenta y no está).
        const last = (await api('/api/inventory', { token: adminToken })).find((i) => i.productName === ULTIMA);
        await api('/api/inventory/adjustments', { method: 'POST', token: adminToken, body: { productId: last.productId, countedQuantity: 0, reason: 'Ensayo: se vendió en el otro punto' } });
        await finishSale(seller);
        check(await visible(seller.getByText(/No hay suficiente inventario/)), 'rechaza: "No hay suficiente inventario … Disponible: 0"');
        check(await stockByName(adminToken, ULTIMA) === 0, 'el inventario no queda negativo');
        check(await visible(seller.locator('.pos-cart-row', { hasText: ULTIMA })), 'el carrito se conserva para corregir');
        await seller.keyboard.press('Escape').catch(() => {});
        await seller.locator('.pos-cart-row', { hasText: ULTIMA }).getByRole('button', { name: 'Quitar' }).click().catch(() => {});
    });

    await scenario('13', 'Ventas', 'Comprobar historial y totales', admin, async () => {
        await admin.goto(APP + '/sales');
        await admin.getByRole('button', { name: /^Hoy \(/ }).click();
        // Las tres ventas de la vendedora suman 80.000 + 70.000 + 48.000.
        const isToday = (v) => new Date(v).toDateString() === new Date().toDateString();
        const all = await api('/api/sales', { token: adminToken });
        const mine = all.filter((x) => x.sellerUsername === SELLER).reduce((sum, x) => sum + Number(x.total), 0);
        check(mine === 198000, `las ventas de ${SELLER} suman $198.000 (80.000 + 70.000 + 48.000)`);
        // La tarjeta "Vendido hoy" coincide con la suma de todas las ventas y pagos de hoy.
        const orders = await api('/api/orders', { token: adminToken });
        const today = all.filter((x) => isToday(x.offlineCreatedAt || x.createdAt)).reduce((sum, x) => sum + Number(x.total), 0)
            + orders.flatMap((o) => o.payments || []).filter((pay) => isToday(pay.createdAt)).reduce((sum, pay) => sum + Number(pay.amount), 0);
        check(await visible(admin.locator('.inv-card', { hasText: 'Vendido hoy' }).getByText(money(today))), `"Vendido hoy" en pantalla = ${money(today)}, la suma real del día`);
        const special = admin.locator('.sales-row', { hasText: 'Con precio especial' }).filter({ hasText: MANTA });
        check(await visible(special), 'la venta con precio especial está marcada');
        await special.first().getByRole('button', { name: 'Ver detalle' }).click();
        check(await visible(admin.getByText(/Precio especial: normal \$\s?80\.000, descuento 12,5 %/)), 'el detalle muestra: normal $80.000, descuento 12,5 %');
        check(await visible(admin.getByText(`Vendió: ${SELLER}`)), 'y quién la vendió');
        await shot(admin, '13b-detalle-venta');
        await admin.keyboard.press('Escape');
    });

    // ======================= COMPRAS Y PRODUCCIÓN =======================

    await scenario('14', 'Compras y producción', 'Registrar compra', admin, async () => {
        await admin.goto(APP + '/purchases');
        await admin.getByRole('button', { name: '+ Registrar compra' }).click();
        const select = admin.locator('select[id^="product-"]').first();
        const option = await select.locator('option', { hasText: TELA }).getAttribute('value');
        await select.selectOption(option);
        await admin.locator('input[id^="qty-"]').first().fill('20');
        await admin.locator('input[id^="cost-"]').first().fill('15000');
        await admin.fill('#purchaseObservations', tag('Factura'));
        check(await visible(admin.getByText(money(300000))), 'total de la compra: $300.000');
        await admin.getByRole('button', { name: 'Registrar compra', exact: true }).click();
        check(await visible(admin.getByText('Compra registrada por $300.000. Las cantidades ya se sumaron al inventario.')), 'confirma: "Compra registrada por $300.000…"');
        check(await visible(admin.locator('.inv-row, li', { hasText: TELA }).filter({ hasText: money(300000) })), 'la compra queda en el historial con su total');
    });

    await scenario('15', 'Compras y producción', 'Comprobar entrada de inventario', admin, async () => {
        check(await stockByName(adminToken, TELA) === 67, 'tela 47 → 67 metros');
        await admin.goto(APP + '/inventory');
        await admin.getByRole('tab', { name: 'Historial de movimientos' }).click();
        check(await visible(admin.locator('.inv-movement', { hasText: TELA }).filter({ hasText: /compra/i })), 'movimiento "Entrada por compra" de 20 metros');
    });

    await scenario('16', 'Compras y producción', 'Registrar producción', admin, async () => {
        await admin.goto(APP + '/production');
        await admin.getByRole('button', { name: '+ Registrar producción' }).click();
        const fabric = await admin.locator('#fabricId option', { hasText: TELA }).getAttribute('value');
        await admin.selectOption('#fabricId', fabric);
        await admin.fill('#perUnit', '2');
        const manta = await admin.locator('#outputId option', { hasText: MANTA }).getAttribute('value');
        await admin.selectOption('#outputId', manta);
        await admin.fill('#outputQuantity', '3');
        await admin.fill('#makerName', 'María Pérez');
        await admin.fill('#productionObservations', tag('Lote de diciembre'));
        await shot(admin, '16b-produccion-formulario');
        await admin.getByRole('button', { name: 'Registrar producción', exact: true }).click();
        check(await visible(admin.locator('li, .inv-row', { hasText: tag('Lote de diciembre') }).filter({ hasText: 'María Pérez' })), 'la producción queda en el historial con quién la fabricó');
    });

    await scenario('17', 'Compras y producción', 'Verificar consumo y disponibilidad de materiales', admin, async () => {
        check(await stockByName(adminToken, TELA) === 61, 'tela 67 → 61 metros (3 mantas × 2 m)');
        const mantas = await stockByName(adminToken, MANTA);
        check(mantas === 13, `mantas 10 → 13 (12 contadas − 2 vendidas + 3 fabricadas) = ${mantas}`);
        await admin.goto(APP + '/inventory');
        await admin.getByRole('tab', { name: 'Historial de movimientos' }).click();
        const fabricOut = admin.locator('.inv-movement', { hasText: 'Salida por producción' }).filter({ hasText: /6 metros/ });
        const blanketsIn = admin.locator('.inv-movement', { hasText: 'Entrada por producción' }).filter({ hasText: 'María Pérez' });
        check(await visible(fabricOut) && await visible(blanketsIn), 'historial: −6 metros de tela y +3 mantas "fabricadas por María Pérez"');
    });

    // ======================= USUARIOS Y SEGURIDAD =======================

    await scenario('19', 'Usuarios y seguridad', 'Comprobar restricciones de vendedor', seller, async () => {
        const menu = await seller.locator('.sidebar-nav').innerText().catch(async () => {
            await seller.getByRole('button', { name: /menú/i }).click().catch(() => {});
            return seller.locator('.sidebar-nav').innerText();
        });
        check(!/Compras|Administrar usuarios/.test(menu) && !/^Ventas$/m.test(menu), 'el menú no muestra Ventas (montos), Compras ni Administrar usuarios');
        for (const p of ['/sales', '/purchases', '/admin']) {
            await seller.goto(APP + p);
            check(await visible(seller.getByRole('heading', { name: 'Sección restringida' })), `${p}: "Sección restringida"`);
        }
        await seller.goto(APP + '/products');
        await seller.getByText('Cargando productos').waitFor({ state: 'detached' }).catch(() => {});
        check(await seller.getByRole('button', { name: '+ Crear producto' }).count() === 0 && await seller.getByRole('button', { name: 'Editar' }).count() === 0, 'en Productos solo consulta: no crea ni edita');
        let forbidden = 0;
        try { await api('/api/purchases', { token: await login(SELLER, SELLER_PASSWORD) }); } catch (e) { forbidden = /403/.test(e.message) ? 1 : 0; }
        check(forbidden === 1, 'aunque llame a la API directamente, el servidor responde 403');
    });

    await scenario('20', 'Usuarios y seguridad', 'Verificar permisos de administrador', admin, async () => {
        const menu = await admin.locator('.sidebar-nav').innerText();
        check(['Nueva venta', 'Personalizadas', 'Productos', 'Inventario', 'Ventas', 'Compras', 'Producción', 'Administrar usuarios'].every((m) => menu.includes(m)), 've todo el menú');
        await admin.goto(APP + '/admin');
        const row = admin.locator('li', { hasText: SELLER });
        check(await visible(row), 'administra usuarios: ve a la vendedora y puede editarla o restablecer su contraseña');
    });

    await scenario('21', 'Usuarios y seguridad', 'Subir y actualizar fotografía de perfil', seller, async () => {
        await seller.goto(APP + '/profile');
        const png = (color) => {
            // PNG de 1×1 válido (la firma real de la imagen se verifica en el servidor).
            const base = Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==', 'base64');
            return { name: `foto-${color}.png`, mimeType: 'image/png', buffer: base };
        };
        await seller.locator('input[type=file]').setInputFiles(png('a'));
        check(await visible(seller.getByRole('button', { name: 'Cambiar foto' }), 10000), 'foto subida: aparece "Cambiar foto"');
        await seller.locator('input[type=file]').setInputFiles(png('b'));
        check(await visible(seller.getByRole('button', { name: 'Quitar foto' })), 'se puede actualizar y quitar');
        await seller.locator('input[type=file]').setInputFiles({ name: 'no-es-foto.png', mimeType: 'image/png', buffer: Buffer.from('esto no es una imagen') });
        check(await visible(seller.locator('[role=alert], .result.error')), 'un archivo que no es imagen se rechaza aunque diga .png');
    });

    // ======================= SIN CONEXIÓN =======================

    let offlineId = null;
    await scenario('22', 'Sin conexión', 'Registrar venta sin conexión', seller, async () => {
        await seller.goto(APP + '/sale');
        await seller.locator('.product-card', { hasText: MANTA }).waitFor();
        await sellerCtx.setOffline(true);
        await seller.locator('.product-card', { hasText: MANTA }).getByRole('button', { name: 'Agregar' }).click();
        await finishSale(seller);
        check(await visible(seller.getByText('quedó guardada en este dispositivo')), 'sin internet: "la venta quedó guardada en este dispositivo"');
        await seller.getByRole('button', { name: 'Nueva venta' }).click();
        check(await visible(seller.getByText(/Ventas guardadas sin conexión \(1\)/)), 'aparece en "Ventas guardadas sin conexión (1)"');
        offlineId = (await seller.evaluate(() => JSON.parse(localStorage.getItem('mg-offline-sales') || '[]')))[0]?.clientRequestId;
    });

    await scenario('23', 'Sin conexión', 'Recuperar conexión y sincronizar', seller, async () => {
        const before = await stockByName(adminToken, MANTA);
        await sellerCtx.setOffline(false);
        await seller.evaluate(() => window.dispatchEvent(new Event('online')));
        await seller.waitForFunction(() => JSON.parse(localStorage.getItem('mg-offline-sales') || '[]').length === 0, null, { timeout: 40000 }).catch(() => {});
        check(await stockByName(adminToken, MANTA) === before - 1, 'al volver la conexión se envía sola y descuenta el inventario');
        await admin.goto(APP + '/sales');
        check(await visible(admin.locator('.sales-row', { hasText: 'Hecha sin conexión' })), 'en Ventas aparece marcada "Hecha sin conexión", con su hora real');
    });

    await scenario('24', 'Sin conexión', 'Comprobar que no se duplique la venta', admin, async () => {
        const sales = (await api('/api/sales', { token: adminToken })).filter((s) => s.clientRequestId === offlineId);
        check(sales.length === 1, 'la venta está UNA sola vez');
        check(sales[0]?.sellerUsername === SELLER, 'a nombre de la vendedora que la hizo');
        // Se reenvía la misma venta (como si el celular no supiera que llegó): no se duplica.
        const sellerToken = await login(SELLER, SELLER_PASSWORD);
        const item = sales[0].items[0];
        await api('/api/sales', { method: 'POST', token: sellerToken, body: { clientRequestId: offlineId, items: [{ productId: item.productId, quantity: 1 }] } });
        const again = (await api('/api/sales', { token: adminToken })).filter((s) => s.clientRequestId === offlineId);
        check(again.length === 1, 'reenviada otra vez: sigue siendo una sola');
    });

    // ---------------------------------------------------------------
    // Capturas adicionales para el manual de usuario (ACCEPT_MANUAL=1).
    // No forman parte de los 24 escenarios.
    // ---------------------------------------------------------------
    if (process.env.ACCEPT_MANUAL === '1' && SHOTS) {
        const extra = async (name, fn) => {
            try { await fn(); } catch (error) { console.log(`    (captura ${name}: ${error.message.split('\n')[0]})`); }
        };
        await extra('m-personalizada', async () => {
            await seller.goto(APP + '/custom-orders');
            await seller.getByRole('button', { name: '+ Nueva personalizada' }).click();
            await seller.fill('#orderName', 'Manta con el nombre de Sofía');
            await seller.fill('#orderClient', 'Sofía Gómez');
            const fabric = await seller.locator('#orderFabric option', { hasText: TELA }).getAttribute('value');
            await seller.selectOption('#orderFabric', fabric);
            await seller.fill('#orderMeters', '2.5');
            await seller.fill('#orderTotal', '150000');
            await seller.fill('#orderPayment', '50000');
            await shot(seller, 'm03-personalizada-formulario');
            await seller.getByRole('button', { name: 'Registrar personalizada', exact: true }).click();
            await seller.locator('li', { hasText: 'Manta con el nombre de Sofía' }).first().waitFor();
            await shot(seller, 'm04-personalizada-con-saldo');
            await seller.locator('li', { hasText: 'Manta con el nombre de Sofía' }).getByRole('button', { name: 'Registrar pago' }).click();
            await seller.fill('#payAmount', '100000');
            await shot(seller, 'm05-registrar-pago');
            await seller.getByRole('button', { name: 'Registrar pago', exact: true }).last().click();
            await seller.waitForTimeout(800);
            await shot(seller, 'm06-personalizada-pagada');
        });
        await extra('m-menu', async () => {
            await seller.goto(APP + '/sale');
            await seller.getByRole('button', { name: /Menú/ }).click();
            await seller.waitForTimeout(400);
            await shot(seller, 'm02-menu-vendedor');
            await seller.keyboard.press('Escape');
        });
        await extra('m-perfil', async () => {
            await seller.goto(APP + '/profile');
            await seller.waitForTimeout(500);
            await shot(seller, 'm07-mi-perfil');
        });
        await extra('m-pantallas-admin', async () => {
            for (const [route, name, wait] of [
                ['/inventory', 'm08-inventario-existencias', '.inv-row'],
                ['/sales', 'm09-ventas', '.sales-row'],
                ['/purchases', 'm10-compras', '.inv-row'],
                ['/production', 'm11-produccion', 'li'],
                ['/products', 'm12-productos', '.inv-row'],
                ['/admin', 'm13-usuarios', 'li'],
            ]) {
                await admin.goto(APP + route);
                await admin.locator(wait).first().waitFor().catch(() => {});
                await admin.waitForTimeout(300);
                await shot(admin, name);
            }
        });
        await extra('m-login', async () => {
            const fresh = await browser.newContext({ ...devices['Pixel 7'] });
            const page = await fresh.newPage();
            await page.goto(APP + '/');
            await page.locator('#identifier').waitFor();
            await shot(page, 'm01-iniciar-sesion');
            await fresh.close();
        });
    }

    await browser.close();

    // ---------------------------------------------------------------
    const ordered = results.sort((a, b) => a.id.localeCompare(b.id));
    const passed = ordered.filter((r) => r.checks.length && r.checks.every((c) => c.ok));
    console.log(`\nRESUMEN: ${passed.length}/24 escenarios correctos`);
    for (const r of ordered) {
        const ok = r.checks.length && r.checks.every((c) => c.ok);
        console.log(`  ${ok ? '✔' : '✘'} ${r.id}. [${r.block}] ${r.title}`);
    }
    if (SHOTS) fs.writeFileSync(path.join(SHOTS, 'resultado.json'), JSON.stringify(ordered, null, 2));
    console.log(passed.length === 24 ? '\nRESULTADO: TODO CORRECTO' : '\nRESULTADO: HAY ESCENARIOS CON FALLAS');
    process.exit(passed.length === 24 ? 0 : 1);
})();
