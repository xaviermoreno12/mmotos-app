-- Actualiza el hash placeholder del admin con un hash BCrypt real
-- Password: admin123
UPDATE usuarios
SET password_hash = '$2b$10$5NjtTt0jdAPGb.j1ZofeQOWNvsfHlgZ6FWR99F.XyEArjT8SPciPW'
WHERE username = 'admin';

-- Cajero de prueba
-- Password: cajero123
INSERT INTO usuarios (id, nombre, username, password_hash, rol, activo)
VALUES (
    gen_random_uuid(),
    'Cajero Test',
    'cajero',
    '$2b$10$4bRLVgnODye9iX/ej5du0OpQh0m1sGr8BhG2BspFBxjXEcP8Ohm7.',
    'CAJERO',
    true
)
ON CONFLICT (username) DO NOTHING;
