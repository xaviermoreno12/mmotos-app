-- Re-seed admin DUENO si no existe
INSERT INTO usuarios (id, nombre, username, password_hash, rol, activo)
VALUES (
    gen_random_uuid(),
    'Administrador',
    'admin',
    '$2b$10$5NjtTt0jdAPGb.j1ZofeQOWNvsfHlgZ6FWR99F.XyEArjT8SPciPW',
    'DUENO',
    true
) ON CONFLICT (username) DO NOTHING;

-- Re-seed cajero de prueba si no existe
INSERT INTO usuarios (id, nombre, username, password_hash, rol, activo)
VALUES (
    gen_random_uuid(),
    'Cajero Test',
    'cajero',
    '$2b$10$4bRLVgnODye9iX/ej5du0OpQh0m1sGr8BhG2BspFBxjXEcP8Ohm7.',
    'CAJERO',
    true
) ON CONFLICT (username) DO NOTHING;

-- Re-seed métodos de pago si no existen
INSERT INTO metodos_pago_config (codigo, nombre, acepta_cobro, acepta_pago, habilitado, orden) VALUES
    ('EFECTIVO',        'Efectivo',        true,  true,  true, 1),
    ('TARJETA_DEBITO',  'Tarjeta Débito',  true,  false, true, 2),
    ('TARJETA_CREDITO', 'Tarjeta Crédito', true,  false, true, 3),
    ('TRANSFERENCIA',   'Transferencia',   true,  true,  true, 4),
    ('MERCADO_PAGO',    'MercadoPago',     true,  false, true, 5)
ON CONFLICT (codigo) DO NOTHING;

-- Asegurar cotización dólar inicial si no existe
INSERT INTO configuracion_sistema (clave, valor)
VALUES ('COTIZACION_DOLAR', '1000.00')
ON CONFLICT (clave) DO NOTHING;
