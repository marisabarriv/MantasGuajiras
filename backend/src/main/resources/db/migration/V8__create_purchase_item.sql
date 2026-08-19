CREATE TABLE purchase_item (
    id UUID PRIMARY KEY,

    purchase_id UUID NOT NULL,

    product_id UUID NOT NULL,

    quantity NUMERIC(12,2) NOT NULL,

    unit_cost NUMERIC(12,2) NOT NULL,

    CONSTRAINT fk_purchase_item_purchase
        FOREIGN KEY (purchase_id)
        REFERENCES purchase(id),

    CONSTRAINT fk_purchase_item_product
        FOREIGN KEY (product_id)
        REFERENCES product(id)
);

CREATE INDEX idx_purchase_item_purchase
ON purchase_item(purchase_id);

CREATE INDEX idx_purchase_item_product
ON purchase_item(product_id);