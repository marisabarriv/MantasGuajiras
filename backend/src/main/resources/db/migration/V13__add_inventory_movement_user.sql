-- Usuario responsable de cada movimiento de inventario.
-- Los movimientos anteriores quedan sin usuario (NULL).

ALTER TABLE inventory_movement
    ADD COLUMN created_by UUID;

ALTER TABLE inventory_movement
    ADD CONSTRAINT fk_inventory_movement_created_by
        FOREIGN KEY (created_by)
        REFERENCES app_user(id);

-- Historial por producto, del más reciente al más antiguo.
CREATE INDEX idx_inventory_movement_product_created
    ON inventory_movement (product_id, created_at DESC);
