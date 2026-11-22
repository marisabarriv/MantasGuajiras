-- Usuario que registró cada producción.
-- Las producciones anteriores quedan sin usuario (NULL).

ALTER TABLE production
    ADD COLUMN created_by UUID;

ALTER TABLE production
    ADD CONSTRAINT fk_production_created_by
        FOREIGN KEY (created_by)
        REFERENCES app_user(id);
