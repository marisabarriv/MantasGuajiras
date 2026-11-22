-- Usuario que registró cada venta (vendedor).
-- Las ventas anteriores quedan sin usuario (NULL).
-- (El índice por fecha idx_sale_created_at ya existe desde V1.)

ALTER TABLE sale
    ADD COLUMN created_by UUID;

ALTER TABLE sale
    ADD CONSTRAINT fk_sale_created_by
        FOREIGN KEY (created_by)
        REFERENCES app_user(id);
