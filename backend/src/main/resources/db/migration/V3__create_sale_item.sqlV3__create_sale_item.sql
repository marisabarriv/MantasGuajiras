CREATE TABLE sale_item (
    id UUID PRIMARY KEY,

    sale_id UUID NOT NULL,

    product_id UUID NOT NULL,

    quantity NUMERIC(12,2) NOT NULL,

    unit_price NUMERIC(12,2) NOT NULL,

    discount_percentage NUMERIC(5,2),

    final_unit_price NUMERIC(12,2) NOT NULL,

    subtotal NUMERIC(12,2) NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_sale_item_sale
        FOREIGN KEY (sale_id)
        REFERENCES sale(id),

    CONSTRAINT fk_sale_item_product
        FOREIGN KEY (product_id)
        REFERENCES product(id)
);

CREATE INDEX idx_sale_item_sale
ON sale_item(sale_id);

CREATE INDEX idx_sale_item_product
ON sale_item(product_id);