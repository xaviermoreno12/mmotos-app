-- V17: Tabla para borradores de facturas de compra generados por IA (foto vía Telegram)
CREATE TABLE factura_borrador (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fecha_recepcion  TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_factura    VARCHAR(20),
    proveedor_nombre VARCHAR(200),
    cuit             VARCHAR(20),
    numero_factura   VARCHAR(100),
    imagen_base64    TEXT,
    texto_ocr        TEXT,
    monto_total      DECIMAL(15,2),
    categoria_gasto  VARCHAR(50),
    estado_pago      VARCHAR(20) NOT NULL DEFAULT 'Pendiente',
    lineas           JSONB NOT NULL DEFAULT '[]',
    estado           VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    observaciones    TEXT,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_factura_borrador_estado ON factura_borrador (estado, fecha_recepcion DESC);
