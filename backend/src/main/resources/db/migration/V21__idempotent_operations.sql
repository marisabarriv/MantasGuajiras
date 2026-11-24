-- =========================================================
-- V21: compras, producción, mantas personalizadas y pagos sin duplicados.
--
-- Igual que las ventas (V20): cada operación lleva un identificador que
-- genera el dispositivo. Si se corta la conexión justo al enviarla y la
-- persona vuelve a pulsar "Guardar", el backend reconoce el identificador,
-- devuelve la operación ya registrada y NO la repite (no suma dos veces el
-- inventario, no descuenta dos veces la tela, no cobra dos veces un pago).
-- =========================================================

ALTER TABLE purchase ADD COLUMN client_request_id UUID;
ALTER TABLE purchase ADD CONSTRAINT uk_purchase_client_request_id UNIQUE (client_request_id);

ALTER TABLE production ADD COLUMN client_request_id UUID;
ALTER TABLE production ADD CONSTRAINT uk_production_client_request_id UNIQUE (client_request_id);

ALTER TABLE orders ADD COLUMN client_request_id UUID;
ALTER TABLE orders ADD CONSTRAINT uk_orders_client_request_id UNIQUE (client_request_id);

ALTER TABLE order_payment ADD COLUMN client_request_id UUID;
ALTER TABLE order_payment ADD CONSTRAINT uk_order_payment_client_request_id UNIQUE (client_request_id);
