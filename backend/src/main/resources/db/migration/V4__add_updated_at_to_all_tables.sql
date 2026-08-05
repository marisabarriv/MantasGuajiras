ALTER TABLE product
ADD COLUMN updated_at TIMESTAMP;

ALTER TABLE product_category
ADD COLUMN updated_at TIMESTAMP;

ALTER TABLE unit
ADD COLUMN updated_at TIMESTAMP;

ALTER TABLE inventory
ADD COLUMN updated_at TIMESTAMP;

ALTER TABLE inventory_movement
ADD COLUMN updated_at TIMESTAMP;

ALTER TABLE purchase
ADD COLUMN updated_at TIMESTAMP;

ALTER TABLE sale
ADD COLUMN updated_at TIMESTAMP;

ALTER TABLE production
ADD COLUMN updated_at TIMESTAMP;

ALTER TABLE "order"
ADD COLUMN updated_at TIMESTAMP;

UPDATE product
SET updated_at = created_at;

UPDATE product_category
SET updated_at = created_at;

UPDATE unit
SET updated_at = created_at;

UPDATE inventory
SET updated_at = created_at;

UPDATE inventory_movement
SET updated_at = created_at;

UPDATE purchase
SET updated_at = created_at;

UPDATE sale
SET updated_at = created_at;

UPDATE production
SET updated_at = created_at;

UPDATE "order"
SET updated_at = created_at;

ALTER TABLE product
ALTER COLUMN updated_at SET NOT NULL;