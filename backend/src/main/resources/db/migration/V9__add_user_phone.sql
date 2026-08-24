ALTER TABLE app_user
ADD COLUMN phone VARCHAR(15) NOT NULL UNIQUE;

CREATE INDEX idx_app_user_phone
ON app_user(phone);