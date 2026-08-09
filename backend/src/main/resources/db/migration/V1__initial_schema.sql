CREATE TABLE app_user (
    id UUID PRIMARY KEY,

    username VARCHAR(50) NOT NULL UNIQUE,

    password VARCHAR(255) NOT NULL,

    role VARCHAR(20) NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE product_category (
    id UUID PRIMARY KEY,

    name VARCHAR(50) NOT NULL UNIQUE,

    description VARCHAR(255),

    display_order INTEGER NOT NULL DEFAULT 0,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE unit (
    id UUID PRIMARY KEY,
    name VARCHAR(20) UNIQUE NOT NULL,
    abbreviation VARCHAR(10) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

create table movement_type (
    id UUID PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

create table source_type (
    id UUID PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE product (
    id UUID PRIMARY KEY,

    category_id UUID NOT NULL,

    unit_id UUID NOT NULL,

    internal_code VARCHAR(30) NOT NULL UNIQUE,

    barcode VARCHAR(50) UNIQUE,

    name VARCHAR(100) NOT NULL,

    purchase_price NUMERIC(12,2),

    unit_price NUMERIC(12,2) NOT NULL,

    wholesale_price NUMERIC(12,2),

    minimum_wholesale_quantity SMALLINT NOT NULL DEFAULT 0,

    minimum_stock NUMERIC(12,2),

    purchasable BOOLEAN NOT NULL,

    manufacturable BOOLEAN NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_product_category
        FOREIGN KEY (category_id)
        REFERENCES product_category(id),

    CONSTRAINT fk_product_unit
        FOREIGN KEY (unit_id)
        REFERENCES unit(id)
);

CREATE TABLE inventory (
    product_id UUID NOT NULL,
    quantity NUMERIC(12,2) NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT pk_inventory PRIMARY KEY (product_id),

    CONSTRAINT fk_inventory_product
        FOREIGN KEY (product_id)
        REFERENCES product(id)
);

CREATE TABLE purchase (
    id UUID PRIMARY KEY,

    total NUMERIC(12,2) NOT NULL,

    observations VARCHAR(255),

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE sale (
    id UUID PRIMARY KEY,

    total NUMERIC(12,2) NOT NULL,

    observations VARCHAR(255),

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE production (
    id UUID PRIMARY KEY,

    observations VARCHAR(255),

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE orders (
    id UUID PRIMARY KEY,

    total NUMERIC(12,2) NOT NULL,

    observations VARCHAR(255),

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE inventory_movement (
    id UUID PRIMARY KEY,

    product_id UUID NOT NULL,
    
    movement_type_id UUID NOT NULL,

    source_type_id UUID NOT NULL,

    source_id UUID,

    quantity NUMERIC(12,2) NOT NULL,

    observations VARCHAR(255),

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_inventory_movement_product
        FOREIGN KEY (product_id)
        REFERENCES product(id),

    CONSTRAINT fk_inventory_movement_movement_type
        FOREIGN KEY (movement_type_id)
        REFERENCES movement_type(id),

    CONSTRAINT fk_inventory_movement_source_type
        FOREIGN KEY (source_type_id)
        REFERENCES source_type(id)
);

CREATE INDEX idx_app_user_username
ON app_user(username);

CREATE INDEX idx_product_internal_code
ON product(internal_code);

CREATE INDEX idx_product_barcode
ON product(barcode);

CREATE INDEX idx_product_category
ON product(category_id);

CREATE INDEX idx_product_unit
ON product(unit_id);

CREATE INDEX idx_inventory_movement_product
ON inventory_movement(product_id);

CREATE INDEX idx_inventory_movement_type
ON inventory_movement(movement_type_id);

CREATE INDEX idx_inventory_movement_source
ON inventory_movement(source_type_id);

CREATE INDEX idx_purchase_created_at
ON purchase(created_at);

CREATE INDEX idx_sale_created_at
ON sale(created_at);

CREATE INDEX idx_production_created_at
ON production(created_at);

CREATE INDEX idx_order_created_at
ON orders(created_at);