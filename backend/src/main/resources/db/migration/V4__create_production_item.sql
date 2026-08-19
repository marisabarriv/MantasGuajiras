CREATE TABLE production_item (
    id UUID PRIMARY KEY,

    production_id UUID NOT NULL,

    product_id UUID NOT NULL,

    type VARCHAR(20) NOT NULL,

    quantity NUMERIC(12,2) NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_production_item_production
        FOREIGN KEY (production_id)
        REFERENCES production(id),

    CONSTRAINT fk_production_item_product
        FOREIGN KEY (product_id)
        REFERENCES product(id)
);

CREATE INDEX idx_production_item_production
ON production_item(production_id);

CREATE INDEX idx_production_item_product
ON production_item(product_id);