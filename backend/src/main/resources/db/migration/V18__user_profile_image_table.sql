-- Foto de perfil en una tabla propia.
--
-- Por qué:
-- 1) V11 y V12 quedaron vacías: las columnas profile_image y
--    profile_image_type existen en algunas bases (agregadas a mano)
--    pero una instalación nueva no las tendría.
-- 2) Guardar la foto (hasta 20 MB) dentro de app_user hacía que se
--    cargara cada vez que se consulta un usuario (por ejemplo, al
--    registrar quién hizo cada venta o movimiento).

CREATE TABLE user_profile_image (
    user_id UUID PRIMARY KEY,

    content_type VARCHAR(100) NOT NULL,

    image BYTEA NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_user_profile_image_user
        FOREIGN KEY (user_id)
        REFERENCES app_user(id)
);

-- Copiar las fotos existentes (si la base tenía las columnas viejas)
-- y luego quitar esas columnas de app_user.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'app_user'
          AND column_name = 'profile_image'
    ) THEN

        INSERT INTO user_profile_image (user_id, content_type, image, updated_at)
        SELECT id,
               COALESCE(profile_image_type, 'image/jpeg'),
               profile_image,
               CURRENT_TIMESTAMP
        FROM app_user
        WHERE profile_image IS NOT NULL;

        ALTER TABLE app_user DROP COLUMN profile_image;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'app_user'
          AND column_name = 'profile_image_type'
    ) THEN
        ALTER TABLE app_user DROP COLUMN profile_image_type;
    END IF;
END $$;
