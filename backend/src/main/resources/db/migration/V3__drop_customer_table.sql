-- Eliminar la columna customer_id de sale
ALTER TABLE sale
DROP COLUMN IF EXISTS customer_id;

-- Eliminar la columna customer_id de custom_order
ALTER TABLE custom_order
DROP COLUMN IF EXISTS customer_id;

-- Eliminar la tabla customer
DROP TABLE IF EXISTS customer;