-- =========================================================
-- Volumen de 3 años de operación para la prueba de carga.
-- NUNCA contra la base de trabajo: solo contra la base de prueba,
-- después de seed.py (usa sus productos y vendedores).
--   psql -U postgres -d mantas_perf -f volume.sql
-- Resultado: 33.000 ventas (66.000 líneas), 1.000 personalizadas con
-- 1.750 pagos, reposición semanal y unos 74.000 movimientos. Al final el
-- inventario se recalcula desde los movimientos (integridad intacta:
-- comprobar con scripts/backup/integrity-check.sql).
-- =========================================================
BEGIN;
CREATE TEMP TABLE prods AS SELECT p.id, p.unit_price, row_number() OVER (ORDER BY p.internal_code) n,
  (SELECT u.abbreviation FROM unit u WHERE u.id = p.unit_id) AS abbr FROM product p;
CREATE TEMP TABLE blankets AS SELECT id, unit_price, row_number() OVER (ORDER BY n) n FROM prods WHERE abbr = '#';
CREATE TEMP TABLE fabrics AS SELECT id, row_number() OVER (ORDER BY n) n FROM prods WHERE abbr = 'm';
CREATE TEMP TABLE sellers AS SELECT id, row_number() OVER (ORDER BY username) n FROM app_user WHERE role = 'SELLER';
CREATE TEMP TABLE ids AS SELECT
  (SELECT id FROM movement_type WHERE name='OUT') t_out, (SELECT id FROM movement_type WHERE name='IN') t_in,
  (SELECT id FROM source_type WHERE name='SALE') s_sale, (SELECT id FROM source_type WHERE name='PURCHASE') s_purchase,
  (SELECT id FROM source_type WHERE name='ORDER') s_order;

CREATE TEMP TABLE gen AS
SELECT g, gen_random_uuid() sale_id, now() - (g * interval '47 minutes') ts,
       (SELECT id FROM sellers WHERE n = 1 + g % 5) seller
FROM generate_series(1, 33000) g;
CREATE TEMP TABLE lines AS
SELECT gen.sale_id, gen.ts, gen.seller, b.id product_id, b.unit_price, (1 + (gen.g + k) % 2)::numeric qty
FROM gen CROSS JOIN LATERAL generate_series(0, gen.g % 3) k
JOIN blankets b ON b.n = 1 + (gen.g * 7 + k * 13) % 40;

INSERT INTO sale (id, total, observations, created_at, updated_at, created_by)
SELECT sale_id, sum(unit_price * qty), NULL, min(ts), min(ts), min(seller::text)::uuid FROM lines GROUP BY sale_id;
INSERT INTO sale_item (id, sale_id, product_id, quantity, unit_price, discount_percentage, final_unit_price, subtotal, created_at, updated_at)
SELECT gen_random_uuid(), sale_id, product_id, qty, unit_price, 0, unit_price, unit_price * qty, ts, ts FROM lines;
INSERT INTO inventory_movement (id, product_id, movement_type_id, source_type_id, source_id, quantity, observations, created_at, updated_at, created_by)
SELECT gen_random_uuid(), product_id, (SELECT t_out FROM ids), (SELECT s_sale FROM ids), sale_id, qty, 'Venta', ts, ts, seller FROM lines;

-- Personalizadas: 1.000, con abono y pago final.
CREATE TEMP TABLE ord AS SELECT gen_random_uuid() id, now() - (g * interval '1 day') ts, g,
  (SELECT id FROM fabrics WHERE n = 1 + g % 12) fabric, (SELECT id FROM sellers WHERE n = 1 + g % 5) seller
FROM generate_series(1, 1000) g;
INSERT INTO orders (id, total, created_at, updated_at, active, name, client_name, paid_amount, fabric_product_id, fabric_quantity, created_by)
SELECT id, 150000, ts, ts, true, 'Personalizada ' || g, 'Cliente ' || g, CASE WHEN g % 4 = 0 THEN 50000 ELSE 150000 END, fabric, 2.5, seller FROM ord;
INSERT INTO order_payment (id, order_id, amount, created_by, created_at)
SELECT gen_random_uuid(), id, 50000, seller, ts FROM ord;
INSERT INTO order_payment (id, order_id, amount, created_by, created_at)
SELECT gen_random_uuid(), id, 100000, seller, ts + interval '5 days' FROM ord WHERE g % 4 <> 0;
INSERT INTO inventory_movement (id, product_id, movement_type_id, source_type_id, source_id, quantity, observations, created_at, updated_at, created_by)
SELECT gen_random_uuid(), fabric, (SELECT t_out FROM ids), (SELECT s_order FROM ids), id, 2.5, 'Personalizada', ts, ts, seller FROM ord;

-- Reposición semanal de todo lo que salió (compras).
INSERT INTO inventory_movement (id, product_id, movement_type_id, source_type_id, quantity, observations, created_at, updated_at)
SELECT gen_random_uuid(), product_id, (SELECT t_in FROM ids), (SELECT s_purchase FROM ids), sum(q), 'Reposición semanal', min(ts), min(ts)
FROM (SELECT product_id, qty q, ts FROM lines UNION ALL SELECT fabric, 2.5, ts FROM ord) x
GROUP BY product_id, date_trunc('week', ts);

UPDATE inventory i SET quantity = s.calc, updated_at = now()
FROM (SELECT m.product_id, sum(CASE WHEN t.name='IN' THEN m.quantity ELSE -m.quantity END) calc
      FROM inventory_movement m JOIN movement_type t ON t.id = m.movement_type_id GROUP BY m.product_id) s
WHERE i.product_id = s.product_id;
COMMIT;
ANALYZE;
