-- =============================================================================
-- V5: Tabla metodos_pago_config — configuración de métodos habilitados
-- =============================================================================

CREATE TABLE metodos_pago_config (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo       VARCHAR(30)  UNIQUE NOT NULL,
    nombre       VARCHAR(50)  NOT NULL,
    acepta_cobro BOOLEAN      NOT NULL DEFAULT true,
    acepta_pago  BOOLEAN      NOT NULL DEFAULT false,
    habilitado   BOOLEAN      NOT NULL DEFAULT true,
    orden        INTEGER      NOT NULL DEFAULT 0
);

INSERT INTO metodos_pago_config (codigo, nombre, acepta_cobro, acepta_pago, habilitado, orden) VALUES
    ('EFECTIVO',        'Efectivo',        true,  true,  true, 1),
    ('TARJETA_DEBITO',  'Tarjeta Débito',  true,  false, true, 2),
    ('TARJETA_CREDITO', 'Tarjeta Crédito', true,  false, true, 3),
    ('TRANSFERENCIA',   'Transferencia',   true,  true,  true, 4),
    ('MERCADO_PAGO',    'MercadoPago',     true,  false, true, 5);
