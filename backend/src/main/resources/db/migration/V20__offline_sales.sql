-- =========================================================
-- V20: ventas registradas sin conexión.
--
-- client_request_id: identificador que genera el dispositivo para cada
--   venta. Si la misma venta llega dos veces (se cortó la conexión justo
--   al enviarla y se reintenta), el backend la reconoce y NO la duplica.
-- offline_created_at: hora real en que se hizo la venta en el
--   dispositivo, cuando se registró sin conexión y se envió después.
--   created_at sigue siendo la hora en que llegó al servidor.
-- =========================================================

ALTER TABLE sale
    ADD COLUMN client_request_id UUID;

ALTER TABLE sale
    ADD CONSTRAINT uk_sale_client_request_id UNIQUE (client_request_id);

ALTER TABLE sale
    ADD COLUMN offline_created_at TIMESTAMP;
