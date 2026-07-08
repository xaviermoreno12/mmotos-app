-- =============================================================================
-- V4: Tabla cajas — control de apertura/cierre de caja por turno
-- =============================================================================

CREATE TABLE cajas (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id          UUID NOT NULL REFERENCES usuarios(id),
    fecha_apertura      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_cierre        TIMESTAMP,
    monto_inicial       DECIMAL(15,2) NOT NULL DEFAULT 0 CHECK (monto_inicial >= 0),
    monto_final_sistema DECIMAL(15,2),             -- calculado automáticamente al cerrar
    monto_final_contado DECIMAL(15,2),             -- ingresado manualmente por el cajero
    diferencia          DECIMAL(15,2),             -- contado - sistema
    observaciones       TEXT,
    estado              VARCHAR(10) NOT NULL DEFAULT 'ABIERTA' CHECK (estado IN ('ABIERTA', 'CERRADA')),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Solo puede haber una caja abierta por usuario a la vez
CREATE UNIQUE INDEX idx_cajas_abierta_usuario ON cajas (usuario_id) WHERE estado = 'ABIERTA';

-- Índice para buscar caja activa rápidamente
CREATE INDEX idx_cajas_estado ON cajas (estado, fecha_apertura DESC);

-- Vincular ventas con caja: agregar columna caja_id a ventas
ALTER TABLE ventas ADD COLUMN caja_id UUID REFERENCES cajas(id);
CREATE INDEX idx_ventas_caja ON ventas (caja_id);
