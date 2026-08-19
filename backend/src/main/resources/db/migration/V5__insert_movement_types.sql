INSERT INTO movement_type (
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
    'IN',
    'Entrada de inventario',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    gen_random_uuid(),
    'OUT',
    'Salida de inventario',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);