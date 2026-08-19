INSERT INTO source_type (
    id,
    name,
    description,
    active,
    created_at,
    updated_at
)
VALUES
(
    gen_random_uuid(),
    'PURCHASE',
    'Movimiento originado por una compra',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    gen_random_uuid(),
    'SALE',
    'Movimiento originado por una venta',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    gen_random_uuid(),
    'PRODUCTION',
    'Movimiento originado por una producción',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    gen_random_uuid(),
    'ADJUSTMENT',
    'Movimiento originado por un ajuste de inventario',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);