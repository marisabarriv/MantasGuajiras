--
-- PostgreSQL database dump
--

\restrict K6hvAhdTRJ4AvCqR3GD4IPo9aCmUVqx6lGKxpwwMYayoxTi0C8Wy35h0KtVHf4v

-- Dumped from database version 18.4
-- Dumped by pg_dump version 18.4

-- Started on 2026-07-30 10:42:14

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 2 (class 3079 OID 16406)
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- TOC entry 5257 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 241 (class 1259 OID 16658)
-- Name: app_user; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.app_user OWNER TO postgres;

--
-- TOC entry 238 (class 1259 OID 16627)
-- Name: custom_order; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.custom_order OWNER TO postgres;

--
-- TOC entry 239 (class 1259 OID 16638)
-- Name: custom_order_item; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.custom_order_item (
    id uuid NOT NULL,
    custom_order_id uuid NOT NULL,
    product_id uuid NOT NULL,
    quantity numeric(12,2) NOT NULL,
    unit_price numeric(12,2) NOT NULL
);


ALTER TABLE public.custom_order_item OWNER TO postgres;

--
-- TOC entry 240 (class 1259 OID 16648)
-- Name: custom_order_payment; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.custom_order_payment (
    id uuid NOT NULL,
    custom_order_id uuid NOT NULL,
    user_id uuid NOT NULL,
    amount numeric(12,2) NOT NULL,
    observations character varying(255),
    created_at timestamp without time zone NOT NULL
);


ALTER TABLE public.custom_order_payment OWNER TO postgres;

--
-- TOC entry 235 (class 1259 OID 16595)
-- Name: customer; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.customer OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 16389)
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.flyway_schema_history OWNER TO postgres;

--
-- TOC entry 229 (class 1259 OID 16538)
-- Name: inventory; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.inventory (
    product_id uuid NOT NULL,
    quantity numeric(12,2) DEFAULT 0 NOT NULL,
    updated_at timestamp without time zone NOT NULL
);


ALTER TABLE public.inventory OWNER TO postgres;

--
-- TOC entry 230 (class 1259 OID 16547)
-- Name: inventory_movement; Type: TABLE; Schema: public; Owner: postgres
--

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


ALTER TABLE public.inventory_movement OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 16467)
-- Name: movement_type; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.movement_type (
    id smallint NOT NULL,
    name character varying(50) NOT NULL,
    description character varying(255),
    active boolean DEFAULT true NOT NULL
);


ALTER TABLE public.movement_type OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 16500)
-- Name: order_status; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.order_status (
    id smallint NOT NULL,
    name character varying(50) NOT NULL,
    description character varying(255),
    active boolean DEFAULT true NOT NULL
);


ALTER TABLE public.order_status OWNER TO postgres;

--
-- TOC entry 228 (class 1259 OID 16522)
-- Name: product; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.product (
    id uuid NOT NULL,
    category_id smallint CONSTRAINT product_category_id_not_null1 NOT NULL,
    unit_id smallint NOT NULL,
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


ALTER TABLE public.product OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 16444)
-- Name: product_category; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.product_category (
    id smallint NOT NULL,
    name character varying(50) NOT NULL,
    description character varying(255),
    active boolean DEFAULT true NOT NULL
);


ALTER TABLE public.product_category OWNER TO postgres;

--
-- TOC entry 231 (class 1259 OID 16558)
-- Name: production; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.production (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    observations character varying(255),
    created_at timestamp without time zone NOT NULL
);


ALTER TABLE public.production OWNER TO postgres;

--
-- TOC entry 232 (class 1259 OID 16566)
-- Name: production_item; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.production_item (
    id uuid NOT NULL,
    production_id uuid NOT NULL,
    product_id uuid NOT NULL,
    operation_id smallint NOT NULL,
    quantity numeric(12,2) NOT NULL
);


ALTER TABLE public.production_item OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 16478)
-- Name: production_operation; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.production_operation (
    id smallint NOT NULL,
    name character varying(20) NOT NULL,
    description character varying(255),
    active boolean DEFAULT true NOT NULL
);


ALTER TABLE public.production_operation OWNER TO postgres;

--
-- TOC entry 233 (class 1259 OID 16576)
-- Name: purchase; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.purchase (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    total numeric(12,2) NOT NULL,
    observations character varying(255),
    created_at timestamp without time zone NOT NULL
);


ALTER TABLE public.purchase OWNER TO postgres;

--
-- TOC entry 234 (class 1259 OID 16585)
-- Name: purchase_item; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.purchase_item (
    id uuid NOT NULL,
    purchase_id uuid NOT NULL,
    product_id uuid NOT NULL,
    quantity numeric(12,2) NOT NULL,
    unit_cost numeric(12,2) NOT NULL
);


ALTER TABLE public.purchase_item OWNER TO postgres;

--
-- TOC entry 227 (class 1259 OID 16511)
-- Name: role; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.role (
    id smallint NOT NULL,
    name character varying(50) NOT NULL,
    description character varying(255),
    active boolean DEFAULT true NOT NULL
);


ALTER TABLE public.role OWNER TO postgres;

--
-- TOC entry 236 (class 1259 OID 16608)
-- Name: sale; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sale (
    id uuid NOT NULL,
    customer_id uuid,
    user_id uuid NOT NULL,
    total numeric(12,2) NOT NULL,
    observations character varying(255),
    created_at timestamp without time zone NOT NULL
);


ALTER TABLE public.sale OWNER TO postgres;

--
-- TOC entry 237 (class 1259 OID 16617)
-- Name: sale_item; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sale_item (
    id uuid NOT NULL,
    sale_id uuid NOT NULL,
    product_id uuid NOT NULL,
    quantity numeric(12,2) NOT NULL,
    unit_price numeric(12,2) NOT NULL
);


ALTER TABLE public.sale_item OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 16489)
-- Name: source_type; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.source_type (
    id smallint NOT NULL,
    name character varying(50) NOT NULL,
    description character varying(255),
    active boolean DEFAULT true NOT NULL
);


ALTER TABLE public.source_type OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 16455)
-- Name: unit; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.unit (
    id smallint NOT NULL,
    name character varying(20) NOT NULL,
    abbreviation character varying(10) NOT NULL,
    active boolean DEFAULT true NOT NULL
);


ALTER TABLE public.unit OWNER TO postgres;

--
-- TOC entry 5076 (class 2606 OID 16671)
-- Name: app_user app_user_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.app_user
    ADD CONSTRAINT app_user_pkey PRIMARY KEY (id);


--
-- TOC entry 5078 (class 2606 OID 16673)
-- Name: app_user app_user_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.app_user
    ADD CONSTRAINT app_user_username_key UNIQUE (username);


--
-- TOC entry 5068 (class 2606 OID 16647)
-- Name: custom_order_item custom_order_item_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.custom_order_item
    ADD CONSTRAINT custom_order_item_pkey PRIMARY KEY (id);


--
-- TOC entry 5072 (class 2606 OID 16657)
-- Name: custom_order_payment custom_order_payment_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.custom_order_payment
    ADD CONSTRAINT custom_order_payment_pkey PRIMARY KEY (id);


--
-- TOC entry 5061 (class 2606 OID 16637)
-- Name: custom_order custom_order_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.custom_order
    ADD CONSTRAINT custom_order_pkey PRIMARY KEY (id);


--
-- TOC entry 5049 (class 2606 OID 16607)
-- Name: customer customer_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.customer
    ADD CONSTRAINT customer_pkey PRIMARY KEY (id);


--
-- TOC entry 4990 (class 2606 OID 16404)
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- TOC entry 5031 (class 2606 OID 16557)
-- Name: inventory_movement inventory_movement_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory_movement
    ADD CONSTRAINT inventory_movement_pkey PRIMARY KEY (id);


--
-- TOC entry 5025 (class 2606 OID 16546)
-- Name: inventory inventory_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory
    ADD CONSTRAINT inventory_pkey PRIMARY KEY (product_id);


--
-- TOC entry 5001 (class 2606 OID 16477)
-- Name: movement_type movement_type_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movement_type
    ADD CONSTRAINT movement_type_name_key UNIQUE (name);


--
-- TOC entry 5003 (class 2606 OID 16475)
-- Name: movement_type movement_type_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movement_type
    ADD CONSTRAINT movement_type_pkey PRIMARY KEY (id);


--
-- TOC entry 5013 (class 2606 OID 16510)
-- Name: order_status order_status_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.order_status
    ADD CONSTRAINT order_status_name_key UNIQUE (name);


--
-- TOC entry 5015 (class 2606 OID 16508)
-- Name: order_status order_status_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.order_status
    ADD CONSTRAINT order_status_pkey PRIMARY KEY (id);


--
-- TOC entry 4993 (class 2606 OID 16454)
-- Name: product_category product_category_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product_category
    ADD CONSTRAINT product_category_name_key UNIQUE (name);


--
-- TOC entry 4995 (class 2606 OID 16452)
-- Name: product_category product_category_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product_category
    ADD CONSTRAINT product_category_pkey PRIMARY KEY (id);


--
-- TOC entry 5023 (class 2606 OID 16537)
-- Name: product product_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product
    ADD CONSTRAINT product_pkey PRIMARY KEY (id);


--
-- TOC entry 5039 (class 2606 OID 16575)
-- Name: production_item production_item_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.production_item
    ADD CONSTRAINT production_item_pkey PRIMARY KEY (id);


--
-- TOC entry 5005 (class 2606 OID 16488)
-- Name: production_operation production_operation_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.production_operation
    ADD CONSTRAINT production_operation_name_key UNIQUE (name);


--
-- TOC entry 5007 (class 2606 OID 16486)
-- Name: production_operation production_operation_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.production_operation
    ADD CONSTRAINT production_operation_pkey PRIMARY KEY (id);


--
-- TOC entry 5035 (class 2606 OID 16565)
-- Name: production production_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.production
    ADD CONSTRAINT production_pkey PRIMARY KEY (id);


--
-- TOC entry 5047 (class 2606 OID 16594)
-- Name: purchase_item purchase_item_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.purchase_item
    ADD CONSTRAINT purchase_item_pkey PRIMARY KEY (id);


--
-- TOC entry 5043 (class 2606 OID 16584)
-- Name: purchase purchase_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.purchase
    ADD CONSTRAINT purchase_pkey PRIMARY KEY (id);


--
-- TOC entry 5017 (class 2606 OID 16521)
-- Name: role role_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_name_key UNIQUE (name);


--
-- TOC entry 5019 (class 2606 OID 16519)
-- Name: role role_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_pkey PRIMARY KEY (id);


--
-- TOC entry 5059 (class 2606 OID 16626)
-- Name: sale_item sale_item_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sale_item
    ADD CONSTRAINT sale_item_pkey PRIMARY KEY (id);


--
-- TOC entry 5055 (class 2606 OID 16616)
-- Name: sale sale_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sale
    ADD CONSTRAINT sale_pkey PRIMARY KEY (id);


--
-- TOC entry 5009 (class 2606 OID 16499)
-- Name: source_type source_type_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.source_type
    ADD CONSTRAINT source_type_name_key UNIQUE (name);


--
-- TOC entry 5011 (class 2606 OID 16497)
-- Name: source_type source_type_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.source_type
    ADD CONSTRAINT source_type_pkey PRIMARY KEY (id);


--
-- TOC entry 4997 (class 2606 OID 16466)
-- Name: unit unit_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.unit
    ADD CONSTRAINT unit_name_key UNIQUE (name);


--
-- TOC entry 4999 (class 2606 OID 16464)
-- Name: unit unit_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.unit
    ADD CONSTRAINT unit_pkey PRIMARY KEY (id);


--
-- TOC entry 4991 (class 1259 OID 16405)
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- TOC entry 5079 (class 1259 OID 16828)
-- Name: idx_app_user_role_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_app_user_role_id ON public.app_user USING btree (role_id);


--
-- TOC entry 5062 (class 1259 OID 16823)
-- Name: idx_custom_order_created_at; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_custom_order_created_at ON public.custom_order USING btree (created_at);


--
-- TOC entry 5063 (class 1259 OID 16819)
-- Name: idx_custom_order_customer_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_custom_order_customer_id ON public.custom_order USING btree (customer_id);


--
-- TOC entry 5064 (class 1259 OID 16822)
-- Name: idx_custom_order_delivery_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_custom_order_delivery_date ON public.custom_order USING btree (delivery_date);


--
-- TOC entry 5069 (class 1259 OID 16824)
-- Name: idx_custom_order_item_order_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_custom_order_item_order_id ON public.custom_order_item USING btree (custom_order_id);


--
-- TOC entry 5070 (class 1259 OID 16825)
-- Name: idx_custom_order_item_product_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_custom_order_item_product_id ON public.custom_order_item USING btree (product_id);


--
-- TOC entry 5073 (class 1259 OID 16826)
-- Name: idx_custom_order_payment_order_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_custom_order_payment_order_id ON public.custom_order_payment USING btree (custom_order_id);


--
-- TOC entry 5074 (class 1259 OID 16827)
-- Name: idx_custom_order_payment_user_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_custom_order_payment_user_id ON public.custom_order_payment USING btree (user_id);


--
-- TOC entry 5065 (class 1259 OID 16821)
-- Name: idx_custom_order_status_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_custom_order_status_id ON public.custom_order USING btree (status_id);


--
-- TOC entry 5066 (class 1259 OID 16820)
-- Name: idx_custom_order_user_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_custom_order_user_id ON public.custom_order USING btree (user_id);


--
-- TOC entry 5050 (class 1259 OID 16813)
-- Name: idx_customer_full_name; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_customer_full_name ON public.customer USING btree (full_name);


--
-- TOC entry 5026 (class 1259 OID 16804)
-- Name: idx_inventory_movement_created_at; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_inventory_movement_created_at ON public.inventory_movement USING btree (created_at);


--
-- TOC entry 5027 (class 1259 OID 16801)
-- Name: idx_inventory_movement_product_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_inventory_movement_product_id ON public.inventory_movement USING btree (product_id);


--
-- TOC entry 5028 (class 1259 OID 16803)
-- Name: idx_inventory_movement_source_type_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_inventory_movement_source_type_id ON public.inventory_movement USING btree (source_type_id);


--
-- TOC entry 5029 (class 1259 OID 16802)
-- Name: idx_inventory_movement_type_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_inventory_movement_type_id ON public.inventory_movement USING btree (movement_type_id);


--
-- TOC entry 5020 (class 1259 OID 16799)
-- Name: idx_product_category_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_product_category_id ON public.product USING btree (category_id);


--
-- TOC entry 5021 (class 1259 OID 16800)
-- Name: idx_product_unit_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_product_unit_id ON public.product USING btree (unit_id);


--
-- TOC entry 5032 (class 1259 OID 16806)
-- Name: idx_production_created_at; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_production_created_at ON public.production USING btree (created_at);


--
-- TOC entry 5036 (class 1259 OID 16808)
-- Name: idx_production_item_product_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_production_item_product_id ON public.production_item USING btree (product_id);


--
-- TOC entry 5037 (class 1259 OID 16807)
-- Name: idx_production_item_production_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_production_item_production_id ON public.production_item USING btree (production_id);


--
-- TOC entry 5033 (class 1259 OID 16805)
-- Name: idx_production_user_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_production_user_id ON public.production USING btree (user_id);


--
-- TOC entry 5040 (class 1259 OID 16810)
-- Name: idx_purchase_created_at; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_purchase_created_at ON public.purchase USING btree (created_at);


--
-- TOC entry 5044 (class 1259 OID 16812)
-- Name: idx_purchase_item_product_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_purchase_item_product_id ON public.purchase_item USING btree (product_id);


--
-- TOC entry 5045 (class 1259 OID 16811)
-- Name: idx_purchase_item_purchase_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_purchase_item_purchase_id ON public.purchase_item USING btree (purchase_id);


--
-- TOC entry 5041 (class 1259 OID 16809)
-- Name: idx_purchase_user_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_purchase_user_id ON public.purchase USING btree (user_id);


--
-- TOC entry 5051 (class 1259 OID 16816)
-- Name: idx_sale_created_at; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sale_created_at ON public.sale USING btree (created_at);


--
-- TOC entry 5052 (class 1259 OID 16814)
-- Name: idx_sale_customer_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sale_customer_id ON public.sale USING btree (customer_id);


--
-- TOC entry 5056 (class 1259 OID 16818)
-- Name: idx_sale_item_product_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sale_item_product_id ON public.sale_item USING btree (product_id);


--
-- TOC entry 5057 (class 1259 OID 16817)
-- Name: idx_sale_item_sale_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sale_item_sale_id ON public.sale_item USING btree (sale_id);


--
-- TOC entry 5053 (class 1259 OID 16815)
-- Name: idx_sale_user_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sale_user_id ON public.sale USING btree (user_id);


--
-- TOC entry 5104 (class 2606 OID 16794)
-- Name: app_user fk_app_user_role; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.app_user
    ADD CONSTRAINT fk_app_user_role FOREIGN KEY (role_id) REFERENCES public.role(id);


--
-- TOC entry 5097 (class 2606 OID 16759)
-- Name: custom_order fk_custom_order_customer; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.custom_order
    ADD CONSTRAINT fk_custom_order_customer FOREIGN KEY (customer_id) REFERENCES public.customer(id);


--
-- TOC entry 5100 (class 2606 OID 16774)
-- Name: custom_order_item fk_custom_order_item_order; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.custom_order_item
    ADD CONSTRAINT fk_custom_order_item_order FOREIGN KEY (custom_order_id) REFERENCES public.custom_order(id);


--
-- TOC entry 5101 (class 2606 OID 16779)
-- Name: custom_order_item fk_custom_order_item_product; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.custom_order_item
    ADD CONSTRAINT fk_custom_order_item_product FOREIGN KEY (product_id) REFERENCES public.product(id);


--
-- TOC entry 5102 (class 2606 OID 16784)
-- Name: custom_order_payment fk_custom_order_payment_order; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.custom_order_payment
    ADD CONSTRAINT fk_custom_order_payment_order FOREIGN KEY (custom_order_id) REFERENCES public.custom_order(id);


--
-- TOC entry 5103 (class 2606 OID 16789)
-- Name: custom_order_payment fk_custom_order_payment_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.custom_order_payment
    ADD CONSTRAINT fk_custom_order_payment_user FOREIGN KEY (user_id) REFERENCES public.app_user(id);


--
-- TOC entry 5098 (class 2606 OID 16769)
-- Name: custom_order fk_custom_order_status; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.custom_order
    ADD CONSTRAINT fk_custom_order_status FOREIGN KEY (status_id) REFERENCES public.order_status(id);


--
-- TOC entry 5099 (class 2606 OID 16764)
-- Name: custom_order fk_custom_order_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.custom_order
    ADD CONSTRAINT fk_custom_order_user FOREIGN KEY (user_id) REFERENCES public.app_user(id);


--
-- TOC entry 5083 (class 2606 OID 16689)
-- Name: inventory_movement fk_inventory_movement_product; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory_movement
    ADD CONSTRAINT fk_inventory_movement_product FOREIGN KEY (product_id) REFERENCES public.product(id);


--
-- TOC entry 5084 (class 2606 OID 16699)
-- Name: inventory_movement fk_inventory_movement_source_type; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory_movement
    ADD CONSTRAINT fk_inventory_movement_source_type FOREIGN KEY (source_type_id) REFERENCES public.source_type(id);


--
-- TOC entry 5085 (class 2606 OID 16694)
-- Name: inventory_movement fk_inventory_movement_type; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory_movement
    ADD CONSTRAINT fk_inventory_movement_type FOREIGN KEY (movement_type_id) REFERENCES public.movement_type(id);


--
-- TOC entry 5082 (class 2606 OID 16684)
-- Name: inventory fk_inventory_product; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory
    ADD CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES public.product(id);


--
-- TOC entry 5080 (class 2606 OID 16674)
-- Name: product fk_product_category; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product
    ADD CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES public.product_category(id);


--
-- TOC entry 5081 (class 2606 OID 16679)
-- Name: product fk_product_unit; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product
    ADD CONSTRAINT fk_product_unit FOREIGN KEY (unit_id) REFERENCES public.unit(id);


--
-- TOC entry 5087 (class 2606 OID 16719)
-- Name: production_item fk_production_item_operation; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.production_item
    ADD CONSTRAINT fk_production_item_operation FOREIGN KEY (operation_id) REFERENCES public.production_operation(id);


--
-- TOC entry 5088 (class 2606 OID 16714)
-- Name: production_item fk_production_item_product; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.production_item
    ADD CONSTRAINT fk_production_item_product FOREIGN KEY (product_id) REFERENCES public.product(id);


--
-- TOC entry 5089 (class 2606 OID 16709)
-- Name: production_item fk_production_item_production; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.production_item
    ADD CONSTRAINT fk_production_item_production FOREIGN KEY (production_id) REFERENCES public.production(id);


--
-- TOC entry 5086 (class 2606 OID 16704)
-- Name: production fk_production_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.production
    ADD CONSTRAINT fk_production_user FOREIGN KEY (user_id) REFERENCES public.app_user(id);


--
-- TOC entry 5091 (class 2606 OID 16734)
-- Name: purchase_item fk_purchase_item_product; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.purchase_item
    ADD CONSTRAINT fk_purchase_item_product FOREIGN KEY (product_id) REFERENCES public.product(id);


--
-- TOC entry 5092 (class 2606 OID 16729)
-- Name: purchase_item fk_purchase_item_purchase; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.purchase_item
    ADD CONSTRAINT fk_purchase_item_purchase FOREIGN KEY (purchase_id) REFERENCES public.purchase(id);


--
-- TOC entry 5090 (class 2606 OID 16724)
-- Name: purchase fk_purchase_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.purchase
    ADD CONSTRAINT fk_purchase_user FOREIGN KEY (user_id) REFERENCES public.app_user(id);


--
-- TOC entry 5093 (class 2606 OID 16739)
-- Name: sale fk_sale_customer; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sale
    ADD CONSTRAINT fk_sale_customer FOREIGN KEY (customer_id) REFERENCES public.customer(id);


--
-- TOC entry 5095 (class 2606 OID 16754)
-- Name: sale_item fk_sale_item_product; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sale_item
    ADD CONSTRAINT fk_sale_item_product FOREIGN KEY (product_id) REFERENCES public.product(id);


--
-- TOC entry 5096 (class 2606 OID 16749)
-- Name: sale_item fk_sale_item_sale; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sale_item
    ADD CONSTRAINT fk_sale_item_sale FOREIGN KEY (sale_id) REFERENCES public.sale(id);


--
-- TOC entry 5094 (class 2606 OID 16744)
-- Name: sale fk_sale_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sale
    ADD CONSTRAINT fk_sale_user FOREIGN KEY (user_id) REFERENCES public.app_user(id);


-- Completed on 2026-07-30 10:42:14

--
-- PostgreSQL database dump complete
--

\unrestrict K6hvAhdTRJ4AvCqR3GD4IPo9aCmUVqx6lGKxpwwMYayoxTi0C8Wy35h0KtVHf4v

