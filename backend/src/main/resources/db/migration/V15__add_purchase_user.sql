-- Usuario que registró cada compra.
-- Las compras anteriores quedan sin usuario (NULL).
-- (El índice por fecha idx_purchase_created_at ya existe desde V1.)

ALTER TABLE purchase
    ADD COLUMN created_by UUID;

ALTER TABLE purchase
    ADD CONSTRAINT fk_purchase_created_by
        FOREIGN KEY (created_by)
        REFERENCES app_user(id);
