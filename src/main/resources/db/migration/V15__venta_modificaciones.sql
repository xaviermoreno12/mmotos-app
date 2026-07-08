-- =============================================================================
-- V15: Historial inmutable de modificaciones de ventas
-- =============================================================================

-- Tabla de auditoría: cada modificación genera un registro que NO se puede borrar
CREATE TABLE venta_modificaciones (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    venta_id       UUID NOT NULL REFERENCES ventas(id),
    usuario_id     UUID REFERENCES usuarios(id),
    usuario_nombre VARCHAR(100) NOT NULL,
    fecha          TIMESTAMP NOT NULL DEFAULT NOW(),
    campo          VARCHAR(50) NOT NULL,
    detalle_id     UUID,
    valor_anterior TEXT NOT NULL,
    valor_nuevo    TEXT NOT NULL,
    motivo         VARCHAR(255) NOT NULL
);

CREATE INDEX idx_venta_mod_venta ON venta_modificaciones (venta_id, fecha DESC);

-- Campos de control en ventas
ALTER TABLE ventas ADD COLUMN IF NOT EXISTS modificada BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE ventas ADD COLUMN IF NOT EXISTS cantidad_modificaciones INT NOT NULL DEFAULT 0;
