-- V16: Tabla para borradores de compras generados por IA (foto de remito vía Telegram)
CREATE TABLE compra_borrador (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fecha_recepcion  TIMESTAMP NOT NULL DEFAULT NOW(),
    proveedor_nombre VARCHAR(200),
    numero_remito    VARCHAR(100),
    imagen_base64    TEXT,
    lineas           JSONB NOT NULL DEFAULT '[]',
    estado           VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    observaciones    TEXT,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_compra_borrador_estado ON compra_borrador (estado, fecha_recepcion DESC);
