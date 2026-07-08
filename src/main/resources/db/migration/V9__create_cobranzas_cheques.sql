-- =============================================================================
-- V9: Cobranzas y Cheques
-- =============================================================================

CREATE TABLE cobranzas (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id   UUID NOT NULL REFERENCES clientes(id),
    monto        DECIMAL(15,2) NOT NULL CHECK (monto > 0),
    fecha        TIMESTAMP NOT NULL DEFAULT NOW(),
    metodo_pago  VARCHAR(30) NOT NULL DEFAULT 'EFECTIVO',
    referencia   VARCHAR(100),
    observaciones TEXT,
    usuario_id   UUID REFERENCES usuarios(id),
    created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cobranzas_cliente ON cobranzas (cliente_id, fecha DESC);

CREATE TABLE cheques (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo           VARCHAR(10) NOT NULL CHECK (tipo IN ('RECIBIDO', 'EMITIDO')),
    numero         VARCHAR(50) NOT NULL,
    banco          VARCHAR(100) NOT NULL,
    librador       VARCHAR(100),
    monto          DECIMAL(15,2) NOT NULL CHECK (monto > 0),
    fecha_emision  DATE NOT NULL,
    fecha_cobro    DATE NOT NULL,
    estado         VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE',
    cliente_id     UUID REFERENCES clientes(id),
    proveedor_id   UUID REFERENCES proveedores(id),
    observaciones  TEXT,
    created_at     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cheques_estado ON cheques (estado, fecha_cobro);
CREATE INDEX idx_cheques_tipo ON cheques (tipo, estado);
