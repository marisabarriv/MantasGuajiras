-- =========================================================
-- Verificación de integridad de MantasGuajiras.
-- Se ejecuta sobre la copia RESTAURADA (restore-test).
-- Cada consulta devuelve la cantidad de problemas: 0 = correcto.
-- =========================================================

-- 1. La existencia de cada producto debe ser igual a la suma de sus
--    movimientos (entradas - salidas). Si no coincide, el inventario
--    fue modificado por fuera del sistema o la copia está dañada.
SELECT 'inventario_vs_movimientos' AS verificacion, COUNT(*) AS problemas
FROM inventory i
LEFT JOIN (
    SELECT m.product_id,
           SUM(CASE WHEN t.name = 'IN'  THEN m.quantity
                    WHEN t.name = 'OUT' THEN -m.quantity
                    ELSE 0 END) AS calculado
    FROM inventory_movement m
    JOIN movement_type t ON t.id = m.movement_type_id
    GROUP BY m.product_id
) s ON s.product_id = i.product_id
WHERE i.quantity <> COALESCE(s.calculado, 0)

UNION ALL

-- 2. Ningún inventario puede ser negativo.
SELECT 'inventario_negativo', COUNT(*) FROM inventory WHERE quantity < 0

UNION ALL

-- 3. Toda venta debe tener al menos un producto.
SELECT 'ventas_sin_productos', COUNT(*)
FROM sale s
WHERE NOT EXISTS (SELECT 1 FROM sale_item si WHERE si.sale_id = s.id)

UNION ALL

-- 4. Las migraciones aplicadas deben haber terminado bien.
SELECT 'migraciones_fallidas', COUNT(*)
FROM flyway_schema_history WHERE success = false

UNION ALL

-- 5. Debe existir al menos un administrador activo.
SELECT 'sin_administrador_activo',
       CASE WHEN EXISTS (SELECT 1 FROM app_user
                         WHERE role = 'ADMIN' AND active = true)
            THEN 0 ELSE 1 END;
