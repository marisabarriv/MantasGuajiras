-- Mantas personalizadas (encargos de clientes con diseño y abono).
-- Se construye sobre la tabla "orders", que existía sin uso (vacía),
-- y sobre el tipo de origen de inventario ORDER, también sin uso.

ALTER TABLE orders
    ADD COLUMN name VARCHAR(100) NOT NULL DEFAULT '';

-- El DEFAULT solo era para poder agregar la columna; se quita.
ALTER TABLE orders
    ALTER COLUMN name DROP DEFAULT;

ALTER TABLE orders
    ADD COLUMN client_name VARCHAR(100);

-- Total ya pagado (suma de los pagos de order_payment).
ALTER TABLE orders
    ADD COLUMN paid_amount NUMERIC(12,2) NOT NULL DEFAULT 0;

-- Tela usada (sale del inventario de tela; la manta va directo al cliente).
ALTER TABLE orders
    ADD COLUMN fabric_product_id UUID;

ALTER TABLE orders
    ADD COLUMN fabric_quantity NUMERIC(12,2);

ALTER TABLE orders
    ADD COLUMN created_by UUID;

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_fabric_product
        FOREIGN KEY (fabric_product_id)
        REFERENCES product(id);

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_created_by
        FOREIGN KEY (created_by)
        REFERENCES app_user(id);

CREATE INDEX idx_orders_created_at
    ON orders (created_at DESC);

-- Cada pago (el abono inicial y los pagos posteriores).
CREATE TABLE order_payment (
    id UUID PRIMARY KEY,

    order_id UUID NOT NULL,

    amount NUMERIC(12,2) NOT NULL,

    created_by UUID,

    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_order_payment_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id),

    CONSTRAINT fk_order_payment_created_by
        FOREIGN KEY (created_by)
        REFERENCES app_user(id)
);

CREATE INDEX idx_order_payment_order
    ON order_payment (order_id);

CREATE INDEX idx_order_payment_created_at
    ON order_payment (created_at DESC);
