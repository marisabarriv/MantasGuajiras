CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;
CREATE TABLE public.app_user (
    id uuid NOT NULL,
    role_id smallint NOT NULL,
    full_name character varying(150) NOT NULL,
    username character varying(50) NOT NULL,
    password character varying(255) NOT NULL,
    active boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL
);
CREATE TABLE public.custom_order (
    id uuid NOT NULL,
    customer_id uuid NOT NULL,
    user_id uuid NOT NULL,
    status_id smallint NOT NULL,
    total numeric(12,2) NOT NULL,
    delivery_date date,
    observations character varying(255),
    created_at timestamp without time zone NOT NULL
);
CREATE TABLE public.custom_order_item (
    id uuid NOT NULL,
    custom_order_id uuid NOT NULL,
    product_id uuid NOT NULL,
    quantity numeric(12,2) NOT NULL,
    unit_price numeric(12,2) NOT NULL
);
CREATE TABLE public.custom_order_payment (
    id uuid NOT NULL,
    custom_order_id uuid NOT NULL,
    user_id uuid NOT NULL,
    amount numeric(12,2) NOT NULL,
    observations character varying(255),
    created_at timestamp without time zone NOT NULL
);
CREATE TABLE public.customer (
    id uuid NOT NULL,
    full_name character varying(150) NOT NULL,
    phone character varying(20),
    address character varying(255),
    notes character varying(255),
    active boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL
);
CREATE TABLE public.inventory (
    product_id uuid NOT NULL,
    quantity numeric(12,2) DEFAULT 0 NOT NULL,
    updated_at timestamp without time zone NOT NULL
);
CREATE TABLE public.inventory_movement (
    id uuid NOT NULL,
    product_id uuid NOT NULL,
    movement_type_id smallint NOT NULL,
    source_type_id smallint NOT NULL,
    source_id uuid,
    quantity numeric(12,2) NOT NULL,
    observations character varying(255),
    created_at timestamp without time zone NOT NULL
);
CREATE TABLE public.movement_type (
    id smallint NOT NULL,
    name character varying(50) NOT NULL,
    description character varying(255),
    active boolean DEFAULT true NOT NULL
);
CREATE TABLE public.order_status (
    id smallint NOT NULL,
    name character varying(50) NOT NULL,
    description character varying(255),
    active boolean DEFAULT true NOT NULL
);
CREATE TABLE public.product (
    id uuid NOT NULL,
    category_id smallint NOT NULL,
    unit_id smallint NOT NULL,
    internal_code character varying(30) NOT NULL,
    barcode character varying(50),
    name character varying(100) NOT NULL,
    purchase_price numeric(12,2),
    unit_price numeric(12,2) NOT NULL,
    wholesale_price numeric(12,2),
    minimum_wholesale_quantity smallint,
    minimum_stock numeric(12,2),
    purchasable boolean NOT NULL,
    manufacturable boolean NOT NULL,
    active boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL
);
CREATE TABLE public.product_category (
    id smallint NOT NULL,
    name character varying(50) NOT NULL,
    description character varying(255),
    display_order smallint DEFAULT 0 NOT NULL,
    active boolean DEFAULT true NOT NULL
);
CREATE TABLE public.production (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    observations character varying(255),
    created_at timestamp without time zone NOT NULL
);
CREATE TABLE public.production_item (
    id uuid NOT NULL,
    production_id uuid NOT NULL,
    product_id uuid NOT NULL,
    operation_id smallint NOT NULL,
    quantity numeric(12,2) NOT NULL
);
CREATE TABLE public.production_operation (
    id smallint NOT NULL,
    name character varying(20) NOT NULL,
    description character varying(255),
    active boolean DEFAULT true NOT NULL
);
CREATE TABLE public.purchase (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    total numeric(12,2) NOT NULL,
    observations character varying(255),
    created_at timestamp without time zone NOT NULL
);
CREATE TABLE public.purchase_item (
    id uuid NOT NULL,
    purchase_id uuid NOT NULL,
    product_id uuid NOT NULL,
    quantity numeric(12,2) NOT NULL,
    unit_cost numeric(12,2) NOT NULL
);
CREATE TABLE public.role (
    id smallint NOT NULL,
    name character varying(50) NOT NULL,
    description character varying(255),
    active boolean DEFAULT true NOT NULL
);
CREATE TABLE public.sale (
    id uuid NOT NULL,
    customer_id uuid,
    user_id uuid NOT NULL,
    total numeric(12,2) NOT NULL,
    observations character varying(255),
    created_at timestamp without time zone NOT NULL
);
CREATE TABLE public.sale_item (
    id uuid NOT NULL,
    sale_id uuid NOT NULL,
    product_id uuid NOT NULL,
    quantity numeric(12,2) NOT NULL,
    unit_price numeric(12,2) NOT NULL
);
CREATE TABLE public.source_type (
    id smallint NOT NULL,
    name character varying(50) NOT NULL,
    description character varying(255),
    active boolean DEFAULT true NOT NULL
);
CREATE TABLE public.unit (
    id smallint NOT NULL,
    name character varying(20) NOT NULL,
    abbreviation character varying(10) NOT NULL,
    active boolean DEFAULT true NOT NULL
);
ALTER TABLE ONLY public.app_user
    ADD CONSTRAINT app_user_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.app_user
    ADD CONSTRAINT app_user_username_key UNIQUE (username);
ALTER TABLE ONLY public.custom_order_item
    ADD CONSTRAINT custom_order_item_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.custom_order_payment
    ADD CONSTRAINT custom_order_payment_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.custom_order
    ADD CONSTRAINT custom_order_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.customer
    ADD CONSTRAINT customer_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.inventory_movement
    ADD CONSTRAINT inventory_movement_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.inventory
    ADD CONSTRAINT inventory_pkey PRIMARY KEY (product_id);
ALTER TABLE ONLY public.movement_type
    ADD CONSTRAINT movement_type_name_key UNIQUE (name);
ALTER TABLE ONLY public.movement_type
    ADD CONSTRAINT movement_type_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.order_status
    ADD CONSTRAINT order_status_name_key UNIQUE (name);
ALTER TABLE ONLY public.order_status
    ADD CONSTRAINT order_status_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.product_category
    ADD CONSTRAINT product_category_name_key UNIQUE (name);
ALTER TABLE ONLY public.product_category
    ADD CONSTRAINT product_category_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.product
    ADD CONSTRAINT product_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.product
    ADD CONSTRAINT product_internal_code_key UNIQUE (internal_code);
ALTER TABLE ONLY public.product
    ADD CONSTRAINT product_barcode_key UNIQUE (barcode);
ALTER TABLE ONLY public.production_item
    ADD CONSTRAINT production_item_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.production_operation
    ADD CONSTRAINT production_operation_name_key UNIQUE (name);
ALTER TABLE ONLY public.production_operation
    ADD CONSTRAINT production_operation_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.production
    ADD CONSTRAINT production_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.purchase_item
    ADD CONSTRAINT purchase_item_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.purchase
    ADD CONSTRAINT purchase_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_name_key UNIQUE (name);
ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.sale_item
    ADD CONSTRAINT sale_item_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.sale
    ADD CONSTRAINT sale_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.source_type
    ADD CONSTRAINT source_type_name_key UNIQUE (name);
ALTER TABLE ONLY public.source_type
    ADD CONSTRAINT source_type_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.unit
    ADD CONSTRAINT unit_name_key UNIQUE (name);
ALTER TABLE ONLY public.unit
    ADD CONSTRAINT unit_pkey PRIMARY KEY (id);
CREATE INDEX idx_app_user_role_id ON public.app_user USING btree (role_id);
CREATE INDEX idx_custom_order_created_at ON public.custom_order USING btree (created_at);
CREATE INDEX idx_custom_order_customer_id ON public.custom_order USING btree (customer_id);
CREATE INDEX idx_custom_order_delivery_date ON public.custom_order USING btree (delivery_date);
CREATE INDEX idx_custom_order_item_order_id ON public.custom_order_item USING btree (custom_order_id);
CREATE INDEX idx_custom_order_item_product_id ON public.custom_order_item USING btree (product_id);
CREATE INDEX idx_custom_order_payment_order_id ON public.custom_order_payment USING btree (custom_order_id);
CREATE INDEX idx_custom_order_payment_user_id ON public.custom_order_payment USING btree (user_id);
CREATE INDEX idx_custom_order_status_id ON public.custom_order USING btree (status_id);
CREATE INDEX idx_custom_order_user_id ON public.custom_order USING btree (user_id);
CREATE INDEX idx_customer_full_name ON public.customer USING btree (full_name);
CREATE INDEX idx_inventory_movement_created_at ON public.inventory_movement USING btree (created_at);
CREATE INDEX idx_inventory_movement_product_id ON public.inventory_movement USING btree (product_id);
CREATE INDEX idx_inventory_movement_source_type_id ON public.inventory_movement USING btree (source_type_id);
CREATE INDEX idx_inventory_movement_type_id ON public.inventory_movement USING btree (movement_type_id);
CREATE INDEX idx_product_category_id ON public.product USING btree (category_id);
CREATE INDEX idx_product_barcode ON public.product(barcode);
CREATE INDEX idx_product_internal_code ON public.product(internal_code);
CREATE INDEX idx_product_price ON public.product(unit_price);
CREATE INDEX idx_product_unit_id ON public.product USING btree (unit_id);
CREATE INDEX idx_production_created_at ON public.production USING btree (created_at);
CREATE INDEX idx_production_item_product_id ON public.production_item USING btree (product_id);
CREATE INDEX idx_production_item_production_id ON public.production_item USING btree (production_id);
CREATE INDEX idx_production_user_id ON public.production USING btree (user_id);
CREATE INDEX idx_purchase_created_at ON public.purchase USING btree (created_at);
CREATE INDEX idx_purchase_item_product_id ON public.purchase_item USING btree (product_id);
CREATE INDEX idx_purchase_item_purchase_id ON public.purchase_item USING btree (purchase_id);
CREATE INDEX idx_purchase_user_id ON public.purchase USING btree (user_id);
CREATE INDEX idx_sale_created_at ON public.sale USING btree (created_at);
CREATE INDEX idx_sale_customer_id ON public.sale USING btree (customer_id);
CREATE INDEX idx_sale_item_product_id ON public.sale_item USING btree (product_id);
CREATE INDEX idx_sale_item_sale_id ON public.sale_item USING btree (sale_id);
CREATE INDEX idx_sale_user_id ON public.sale USING btree (user_id);
ALTER TABLE ONLY public.app_user
    ADD CONSTRAINT fk_app_user_role FOREIGN KEY (role_id) REFERENCES public.role(id);
ALTER TABLE ONLY public.custom_order
    ADD CONSTRAINT fk_custom_order_customer FOREIGN KEY (customer_id) REFERENCES public.customer(id);
ALTER TABLE ONLY public.custom_order_item
    ADD CONSTRAINT fk_custom_order_item_order FOREIGN KEY (custom_order_id) REFERENCES public.custom_order(id);
ALTER TABLE ONLY public.custom_order_item
    ADD CONSTRAINT fk_custom_order_item_product FOREIGN KEY (product_id) REFERENCES public.product(id);
ALTER TABLE ONLY public.custom_order_payment
    ADD CONSTRAINT fk_custom_order_payment_order FOREIGN KEY (custom_order_id) REFERENCES public.custom_order(id);
ALTER TABLE ONLY public.custom_order_payment
    ADD CONSTRAINT fk_custom_order_payment_user FOREIGN KEY (user_id) REFERENCES public.app_user(id);
ALTER TABLE ONLY public.custom_order
    ADD CONSTRAINT fk_custom_order_status FOREIGN KEY (status_id) REFERENCES public.order_status(id);
ALTER TABLE ONLY public.custom_order
    ADD CONSTRAINT fk_custom_order_user FOREIGN KEY (user_id) REFERENCES public.app_user(id);
ALTER TABLE ONLY public.inventory_movement
    ADD CONSTRAINT fk_inventory_movement_product FOREIGN KEY (product_id) REFERENCES public.product(id);
ALTER TABLE ONLY public.inventory_movement
    ADD CONSTRAINT fk_inventory_movement_source_type FOREIGN KEY (source_type_id) REFERENCES public.source_type(id);
ALTER TABLE ONLY public.inventory_movement
    ADD CONSTRAINT fk_inventory_movement_type FOREIGN KEY (movement_type_id) REFERENCES public.movement_type(id);
ALTER TABLE ONLY public.inventory
    ADD CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES public.product(id);
ALTER TABLE ONLY public.product
    ADD CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES public.product_category(id);
ALTER TABLE ONLY public.product
    ADD CONSTRAINT fk_product_unit FOREIGN KEY (unit_id) REFERENCES public.unit(id);
ALTER TABLE ONLY public.production_item
    ADD CONSTRAINT fk_production_item_operation FOREIGN KEY (operation_id) REFERENCES public.production_operation(id);
ALTER TABLE ONLY public.production_item
    ADD CONSTRAINT fk_production_item_product FOREIGN KEY (product_id) REFERENCES public.product(id);
ALTER TABLE ONLY public.production_item
    ADD CONSTRAINT fk_production_item_production FOREIGN KEY (production_id) REFERENCES public.production(id);
ALTER TABLE ONLY public.production
    ADD CONSTRAINT fk_production_user FOREIGN KEY (user_id) REFERENCES public.app_user(id);
ALTER TABLE ONLY public.purchase_item
    ADD CONSTRAINT fk_purchase_item_product FOREIGN KEY (product_id) REFERENCES public.product(id);
ALTER TABLE ONLY public.purchase_item
    ADD CONSTRAINT fk_purchase_item_purchase FOREIGN KEY (purchase_id) REFERENCES public.purchase(id);
ALTER TABLE ONLY public.purchase
    ADD CONSTRAINT fk_purchase_user FOREIGN KEY (user_id) REFERENCES public.app_user(id);
ALTER TABLE ONLY public.sale
    ADD CONSTRAINT fk_sale_customer FOREIGN KEY (customer_id) REFERENCES public.customer(id);
ALTER TABLE ONLY public.sale_item
    ADD CONSTRAINT fk_sale_item_product FOREIGN KEY (product_id) REFERENCES public.product(id);
ALTER TABLE ONLY public.sale_item
    ADD CONSTRAINT fk_sale_item_sale FOREIGN KEY (sale_id) REFERENCES public.sale(id);
ALTER TABLE ONLY public.sale
    ADD CONSTRAINT fk_sale_user FOREIGN KEY (user_id) REFERENCES public.app_user(id);