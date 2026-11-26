/**
 * Pasos de la ayuda "¿Cómo se usa?" de cada sección.
 *
 * Cada paso señala un elemento de la pantalla (`target`, un selector CSS)
 * y explica en pocas palabras qué hacer allí. Si el elemento no está en
 * pantalla (por ejemplo, una lista vacía), el paso se muestra igual, al
 * centro y sin señalar nada.
 *
 * `soloAdmin: true` oculta el paso a los vendedores. Las secciones de
 * Ventas, Compras y Usuarios solo las abre un administrador.
 *
 * Basado en el manual de usuario (docs/14-manual-de-usuario.md).
 */

const FIRST_ROW_ACTIONS = ".inv-list > li:first-child .inv-row-actions";

const TOURS = {
    "/sale": {
        title: "Nueva venta",
        steps: [
            {
                target: ".search-area input",
                title: "Busca el producto",
                text: "Escribe el nombre o el código. Con lector de códigos, escanea y el producto entra solo al carrito.",
            },
            {
                target: ".scan-camera-button",
                title: "Escanear con la cámara",
                text: "En el celular, pulsa aquí para leer el código de barras con la cámara.",
            },
            {
                target: ".product-tabs",
                title: "Elige la categoría",
                text: "Pulsa Manta o Tela para ver solo esos productos.",
            },
            {
                target: ".products-grid",
                title: "Agrega al carrito",
                text: "Pulsa Agregar en el producto. Los que dicen Agotado no se pueden vender.",
            },
            {
                target: ".cart-section",
                title: "Revisa el carrito",
                text: "Con − y + cambias la cantidad de mantas; en las telas escribe los metros. Cambiar precio pone un precio especial solo en esta venta. Quitar saca el producto.",
            },
            {
                target: ".checkout-button",
                title: "Finaliza la venta",
                text: "Pulsa Finalizar venta, revisa el resumen y pulsa Confirmar venta. Sin internet, la venta se guarda en este dispositivo y se envía sola cuando vuelve la conexión.",
            },
        ],
    },

    "/custom-orders": {
        title: "Mantas personalizadas",
        steps: [
            {
                target: ".purchases-new",
                title: "Registra un encargo",
                text: "Pulsa + Nueva personalizada. Escribe el nombre de la manta, la tela y los metros usados, el valor y el abono que paga el cliente ahora.",
            },
            {
                target: ".inv-summary",
                title: "Mira qué falta por cobrar",
                text: "Estas tarjetas muestran las mantas con saldo pendiente, las pagadas y todas. Al pulsar una, la lista muestra solo esas.",
            },
            {
                target: ".inv-search",
                title: "Busca un encargo",
                text: "Escribe el nombre de la manta o del cliente.",
            },
            {
                target: FIRST_ROW_ACTIONS,
                title: "Cobra un abono",
                text: "Cuando el cliente vuelva a pagar, pulsa Registrar pago en su manta. Ver detalle muestra todos los pagos, con fecha y quién los recibió.",
            },
        ],
    },

    "/products": {
        title: "Productos",
        steps: [
            {
                target: ".purchases-new",
                soloAdmin: true,
                title: "Crea un producto",
                text: "Pulsa + Crear producto. Escribe el nombre, la categoría, el precio, las existencias iniciales y el stock mínimo.",
            },
            {
                target: ".inv-filters",
                title: "Filtra la lista",
                text: "Elige qué ver: todos, mantas, telas o los productos inactivos.",
            },
            {
                target: ".inv-search",
                title: "Busca un producto",
                text: "Escribe el nombre o el código, o pulsa Escanear para leer el código de barras.",
            },
            {
                target: ".inv-list",
                title: "Consulta precios",
                text: "Cada producto muestra su código, su categoría y su precio. Las cantidades se ven en Inventario.",
            },
            {
                target: FIRST_ROW_ACTIONS,
                soloAdmin: true,
                title: "Edita o desactiva",
                text: "Pulsa Editar para cambiar el nombre, el precio o el mínimo. Para dejar de venderlo, quita la marca Producto activo: su historial se conserva.",
            },
        ],
    },

    "/inventory": {
        title: "Inventario",
        steps: [
            {
                target: ".inv-summary",
                title: "Revisa las alertas",
                text: "Estas tarjetas cuentan los productos agotados y los que tienen stock bajo. Al pulsar una, la lista muestra solo esos.",
            },
            {
                target: ".inv-tabs",
                title: "Existencias o historial",
                text: "Existencias muestra cuánto hay de cada producto. Historial de movimientos muestra cada entrada y salida, con fecha y quién la hizo.",
            },
            {
                target: ".inv-search",
                title: "Busca un producto",
                text: "Escribe el nombre o el código, o pulsa Escanear.",
            },
            {
                target: FIRST_ROW_ACTIONS,
                title: "Ajusta por conteo",
                text: "Si al contar el número no coincide, pulsa Ajustar, escribe cuántas hay realmente y el motivo, y pulsa Guardar ajuste. Ver historial muestra las entradas y salidas de ese producto.",
            },
        ],
    },

    "/production": {
        title: "Producción",
        steps: [
            {
                target: ".purchases-new",
                title: "Registra lo fabricado",
                text: "Pulsa + Registrar producción. Elige la tela, los metros por manta, la manta, la cantidad y quién las fabricó.",
            },
            {
                target: ".inv-summary",
                title: "Resumen del mes",
                text: "Aquí ves cuántas mantas se fabricaron este mes y cuánta tela se usó.",
            },
            {
                target: ".inv-list",
                title: "Historial",
                text: "Cada producción queda con la fecha, la tela usada y quién la fabricó. Al registrarla, la tela baja y las mantas suben en un solo paso.",
            },
        ],
    },

    "/sales": {
        title: "Ventas",
        steps: [
            {
                target: ".sales-period",
                title: "Elige el periodo",
                text: "Pulsa Hoy, 7 días, 30 días o 3 meses, o escribe las fechas Desde y Hasta.",
            },
            {
                target: ".inv-summary",
                title: "Totales",
                text: "Vendido hoy es el dinero recibido hoy, con los pagos de personalizadas. En el periodo es el total de las fechas elegidas.",
            },
            {
                target: FIRST_ROW_ACTIONS,
                title: "Mira el detalle",
                text: "Pulsa Ver detalle para ver cada producto, la cantidad, el precio cobrado, el descuento y quién vendió.",
            },
        ],
    },

    "/purchases": {
        title: "Compras",
        steps: [
            {
                target: ".purchases-new",
                title: "Registra una compra",
                text: "Pulsa + Registrar compra. Por cada producto escribe la cantidad y el costo, y en observaciones el número de factura.",
            },
            {
                target: ".inv-summary",
                title: "Totales",
                text: "Aquí ves lo comprado este mes y todas las compras. Al pulsar una tarjeta, la lista cambia.",
            },
            {
                target: FIRST_ROW_ACTIONS,
                title: "Mira el detalle",
                text: "Pulsa Ver detalle para ver los productos y costos de esa compra. Las compras no se borran: un error se corrige con un ajuste de inventario.",
            },
        ],
    },

    "/admin": {
        title: "Administrar usuarios",
        steps: [
            {
                target: ".purchases-new",
                title: "Crea un usuario",
                text: "Pulsa + Crear usuario. Escribe el usuario, el teléfono y la contraseña, y elige si es Vendedor o Administrador.",
            },
            {
                target: ".admin-pending",
                title: "Cuentas nuevas",
                text: "Quien se registra solo queda esperando aprobación. Pulsa Activar para darle acceso.",
            },
            {
                target: ".inv-search",
                title: "Busca una persona",
                text: "Escribe el usuario o el teléfono.",
            },
            {
                target: FIRST_ROW_ACTIONS,
                title: "Edita o restablece",
                text: "Editar cambia el rol o desactiva la cuenta. Restablecer contraseña sirve cuando alguien la olvida: dile que la cambie en Mi perfil.",
            },
        ],
    },

    "/profile": {
        title: "Mi perfil",
        steps: [
            {
                target: ".profile-photo-actions",
                title: "Tu foto",
                text: "Sube, cambia o quita tu foto. Se aceptan imágenes JPG, PNG, WEBP o GIF.",
            },
            {
                target: ".profile-card .purchase-form",
                title: "Cambia tu contraseña",
                text: "Escribe la contraseña actual y la nueva dos veces. Para cambiar tu usuario o teléfono, pídeselo a un administrador.",
            },
            {
                target: ".app-version",
                title: "Versión de la app",
                text: "Sirve para confirmar una actualización o para reportar un problema.",
            },
        ],
    },
};

/** La ayuda de la sección `path`, con los pasos que corresponden al rol. */
export function tourFor(path, isAdmin) {
    const tour = TOURS[path];

    if (!tour) {
        return null;
    }

    const steps = tour.steps.filter((step) => isAdmin || !step.soloAdmin);

    return steps.length > 0 ? { ...tour, steps } : null;
}
