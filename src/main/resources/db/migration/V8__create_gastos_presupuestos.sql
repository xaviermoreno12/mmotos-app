-- =============================================================================
-- V8: Gastos y Presupuestos
-- =============================================================================

CREATE TABLE gastos (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fecha       TIMESTAMP NOT NULL DEFAULT NOW(),
    descripcion VARCHAR(200) NOT NULL,
    categoria   VARCHAR(50) NOT NULL,
    monto       DECIMAL(15,2) NOT NULL CHECK (monto > 0),
    metodo_pago VARCHAR(30) NOT NULL DEFAULT 'EFECTIVO',
    usuario_id  UUID REFERENCES usuarios(id),
    observaciones TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_gastos_fecha ON gastos (fecha DESC);

CREATE TABLE presupuestos (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id     UUID REFERENCES clientes(id),
    cliente_nombre VARCHAR(100) NOT NULL,
    fecha          TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_validez  TIMESTAMP NOT NULL,
    total          DECIMAL(15,2) NOT NULL,
    estado         VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    observaciones  TEXT,
    usuario_id     UUID REFERENCES usuarios(id),
    created_at     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE presupuestos_detalle (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    presupuesto_id   UUID NOT NULL REFERENCES presupuestos(id) ON DELETE CASCADE,
    producto_id      UUID REFERENCES productos(id),
    sku_historico    VARCHAR(50) NOT NULL,
    nombre_historico VARCHAR(200) NOT NULL,
    cantidad         INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario  DECIMAL(15,2) NOT NULL,
    subtotal         DECIMAL(15,2) NOT NULL
);

CREATE INDEX idx_presupuestos_estado ON presupuestos (estado, fecha DESC);
CREATE INDEX idx_presupuestos_detalle ON presupuestos_detalle (presupuesto_id);
