-- Nombre de la persona que fabricó las mantas de cada producción.
-- (Puede ser distinta del usuario que la registró en el sistema.)
-- Las producciones anteriores quedan sin nombre (NULL).

ALTER TABLE production
    ADD COLUMN maker_name VARCHAR(100);
