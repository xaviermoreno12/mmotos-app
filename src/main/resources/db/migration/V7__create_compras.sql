-- =============================================================================
-- V7: Tablas compras y compras_detalle — entrada de mercadería
-- =============================================================================

CREATE TABLE compras (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    proveedor_id     UUID REFERENCES proveedores(id),
    proveedor_nombre VARCHAR(100) NOT NULL,
    numero_remito    VARCHAR(50),
    fecha            TIMESTAMP NOT NULL DEFAULT NOW(),
    total            DECIMAL(15,2) NOT NULL,
    metodo_pago      VARCHAR(30) NOT NULL,
    estado           VARCHAR(20) NOT NULL DEFAULT 'COMPLETADA',
    usuario_id       UUID REFERENCES usuarios(id),
    observaciones    TEXT,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE compras_detalle (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    compra_id        UUID NOT NULL REFERENCES compras(id) ON DELETE CASCADE,
    producto_id      UUID REFERENCES productos(id),
    sku_historico    VARCHAR(50) NOT NULL,
    nombre_historico VARCHAR(200) NOT NULL,
    cantidad         INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario  DECIMAL(15,2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal         DECIMAL(15,2) NOT NULL
);

CREATE INDEX idx_compras_proveedor ON compras (proveedor_id, fecha DESC);
CREATE INDEX idx_compras_fecha ON compras (fecha DESC);
CREATE INDEX idx_compras_detalle_compra ON compras_detalle (compra_id);
