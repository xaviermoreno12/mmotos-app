-- =============================================================================
-- MMotosApp - Schema Inicial
-- Estrategia: SQL+JSONB híbrido sobre PostgreSQL 16
-- =============================================================================

-- Extensiones necesarias
CREATE EXTENSION IF NOT EXISTS "pgcrypto";     -- gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS "pg_trgm";      -- búsquedas parciales de texto en JSONB

-- Tipos enumerados
CREATE TYPE moneda_enum        AS ENUM ('ARS', 'USD');
CREATE TYPE estado_fiscal_enum AS ENUM ('PENDIENTE', 'APROBADO', 'ERROR_HARDWARE', 'CONTINGENCIA');
CREATE TYPE sync_status_enum   AS ENUM ('LOCAL', 'PENDING', 'SYNCED', 'CONFLICT', 'FAILED');
CREATE TYPE tipo_factura_enum  AS ENUM ('A', 'B', 'C', 'NO_FISCAL');
CREATE TYPE metodo_pago_enum   AS ENUM ('EFECTIVO', 'TARJETA_DEBITO', 'TARJETA_CREDITO', 'TRANSFERENCIA', 'MERCADO_PAGO');
CREATE TYPE rol_usuario_enum   AS ENUM ('CAJERO', 'DUENO');

-- =============================================================================
-- USUARIOS
-- =============================================================================
CREATE TABLE usuarios (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre        VARCHAR(100) NOT NULL,
    username      VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    rol           rol_usuario_enum NOT NULL DEFAULT 'CAJERO',
    activo        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================================================================
-- CATÁLOGO: Modelos de Moto (árbol de compatibilidad)
-- =============================================================================
CREATE TABLE modelos_moto (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    marca      VARCHAR(100) NOT NULL,
    modelo     VARCHAR(100) NOT NULL,
    anio_desde SMALLINT,
    anio_hasta SMALLINT,
    UNIQUE (marca, modelo, anio_desde, anio_hasta)
);

-- =============================================================================
-- PRODUCTOS
-- El corazón del catálogo: soporte JSONB para atributos dinámicos
-- (viscosidad, medida de neumático, voltaje, etc.) sin cambiar el esquema
-- =============================================================================
CREATE TABLE productos (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku                  VARCHAR(50) UNIQUE NOT NULL,
    nombre               VARCHAR(255) NOT NULL,
    descripcion          TEXT,
    precio_base          DECIMAL(12,2) NOT NULL CHECK (precio_base >= 0),
    moneda               moneda_enum NOT NULL DEFAULT 'ARS',
    stock_actual         INT NOT NULL DEFAULT 0 CHECK (stock_actual >= -1), -- -1 permitido en conflicto offline
    stock_minimo         INT NOT NULL DEFAULT 2,
    atributos_extra      JSONB NOT NULL DEFAULT '{}',  -- {"viscosidad":"20W50","litros":1}
    ubicacion_fisica     VARCHAR(100),                 -- "Estantería A-4"
    es_kit               BOOLEAN NOT NULL DEFAULT FALSE,
    activo               BOOLEAN NOT NULL DEFAULT TRUE,
    ultima_actualizacion TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Índice B-Tree para búsqueda por SKU (lookup más frecuente del POS)
CREATE INDEX idx_productos_sku ON productos (sku);

-- Índice GIN para containment queries exactas: WHERE atributos_extra @> '{"viscosidad":"20W50"}'
CREATE INDEX idx_productos_atributos_gin ON productos USING GIN (atributos_extra);

-- Índice trigrama sobre el cast text del JSONB para búsquedas PARCIALES:
-- WHERE atributos_extra::text ILIKE '%110/70%'
CREATE INDEX idx_productos_atributos_trgm ON productos USING GIN ((atributos_extra::text) gin_trgm_ops);

-- Índice trigrama sobre nombre para búsqueda fuzzy desde el buscador del POS
CREATE INDEX idx_productos_nombre_trgm ON productos USING GIN (nombre gin_trgm_ops);

-- Índice parcial para el Observer de stock bajo (solo productos activos bajo mínimo)
CREATE INDEX idx_productos_stock_bajo ON productos (id)
    WHERE stock_actual <= stock_minimo AND activo = TRUE;

-- =============================================================================
-- COMPATIBILIDAD: Many-to-Many productos ↔ modelos_moto
-- =============================================================================
CREATE TABLE producto_compatibilidad (
    producto_id    UUID NOT NULL REFERENCES productos(id) ON DELETE CASCADE,
    modelo_moto_id UUID NOT NULL REFERENCES modelos_moto(id) ON DELETE CASCADE,
    PRIMARY KEY (producto_id, modelo_moto_id)
);

CREATE INDEX idx_compat_modelo ON producto_compatibilidad (modelo_moto_id);

-- =============================================================================
-- KITS: Composición recursiva (un producto compuesto por otros productos)
-- =============================================================================
CREATE TABLE kit_composicion (
    kit_id         UUID NOT NULL REFERENCES productos(id) ON DELETE CASCADE,
    componente_id  UUID NOT NULL REFERENCES productos(id),
    cantidad       INT NOT NULL CHECK (cantidad > 0),
    PRIMARY KEY (kit_id, componente_id)
);

-- =============================================================================
-- VENTAS
-- =============================================================================
CREATE TABLE ventas (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero_ticket    VARCHAR(30) UNIQUE,               -- Número fiscal o interno
    tipo_factura     tipo_factura_enum NOT NULL DEFAULT 'B',
    fecha_emision    TIMESTAMP NOT NULL DEFAULT NOW(),
    cliente_cuit     VARCHAR(11),                      -- Requerido solo para Factura A
    total_venta      DECIMAL(15,2) NOT NULL CHECK (total_venta >= 0),
    estado_fiscal    estado_fiscal_enum NOT NULL DEFAULT 'PENDIENTE',
    cae              VARCHAR(14),                      -- Código AFIP
    sync_status      sync_status_enum NOT NULL DEFAULT 'LOCAL',
    usuario_id       UUID REFERENCES usuarios(id),
    created_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Índice para reportes por fecha y consultas del SyncService
CREATE INDEX idx_ventas_fecha ON ventas (fecha_emision DESC);
CREATE INDEX idx_ventas_sync_status ON ventas (sync_status) WHERE sync_status IN ('LOCAL', 'PENDING', 'FAILED');

-- =============================================================================
-- DETALLES DE VENTA (líneas inmutables — "foto" del precio al momento del cierre)
-- =============================================================================
CREATE TABLE ventas_detalles (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    venta_id                  UUID NOT NULL REFERENCES ventas(id) ON DELETE CASCADE,
    producto_id               UUID REFERENCES productos(id),        -- Nullable: puede haberse borrado el producto
    sku_historico             VARCHAR(50) NOT NULL,                  -- Inmutable
    nombre_historico          VARCHAR(255) NOT NULL,                 -- Inmutable
    cantidad                  INT NOT NULL CHECK (cantidad > 0),
    precio_unitario_historico DECIMAL(12,2) NOT NULL CHECK (precio_unitario_historico >= 0),
    subtotal                  DECIMAL(12,2) GENERATED ALWAYS AS (cantidad * precio_unitario_historico) STORED
);

CREATE INDEX idx_detalle_venta ON ventas_detalles (venta_id);

-- =============================================================================
-- PAGOS (soporte para pagos mixtos: efectivo + tarjeta + transferencia)
-- =============================================================================
CREATE TABLE pagos (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    venta_id       UUID NOT NULL REFERENCES ventas(id) ON DELETE CASCADE,
    metodo         metodo_pago_enum NOT NULL,
    monto          DECIMAL(15,2) NOT NULL CHECK (monto > 0),
    detalles_pago  JSONB          -- {"nro_cupon":"1234","cuotas":3,"banco":"Galicia"}
);

CREATE INDEX idx_pagos_venta ON pagos (venta_id);

-- =============================================================================
-- COLA DE SINCRONIZACIÓN (Outbox Pattern para modo offline)
-- =============================================================================
CREATE TABLE sync_log (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entidad_id              UUID NOT NULL,
    entidad_tipo            VARCHAR(50) NOT NULL,          -- 'VENTA'
    estado                  sync_status_enum NOT NULL DEFAULT 'PENDING',
    intentos                INT NOT NULL DEFAULT 0,
    ultimo_error            TEXT,
    proxima_sincronizacion  TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at              TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sync_log_pendiente ON sync_log (proxima_sincronizacion)
    WHERE estado IN ('PENDING', 'FAILED');

-- =============================================================================
-- CONFIGURACIÓN DEL SISTEMA (cotización dólar, punto de venta, etc.)
-- =============================================================================
CREATE TABLE configuracion_sistema (
    clave                VARCHAR(50) PRIMARY KEY,
    valor                DECIMAL(15,4) NOT NULL,
    ultima_actualizacion TIMESTAMP NOT NULL DEFAULT NOW(),
    usuario_id           UUID REFERENCES usuarios(id)
);

-- =============================================================================
-- AUDITORÍA DE PRECIOS
-- =============================================================================
CREATE TABLE auditoria_precios (
    id              BIGSERIAL PRIMARY KEY,
    producto_id     UUID NOT NULL REFERENCES productos(id),
    precio_anterior DECIMAL(15,2),
    precio_nuevo    DECIMAL(15,2) NOT NULL,
    motivo          VARCHAR(100) NOT NULL,   -- 'ACTUALIZACION_DOLAR', 'MANUAL', 'N8N_SYNC'
    fecha           TIMESTAMP NOT NULL DEFAULT NOW(),
    usuario_id      UUID REFERENCES usuarios(id)
);

CREATE INDEX idx_auditoria_producto ON auditoria_precios (producto_id, fecha DESC);

-- =============================================================================
-- DATOS INICIALES
-- =============================================================================
INSERT INTO configuracion_sistema (clave, valor) VALUES
    ('COTIZACION_DOLAR', 1000.00),
    ('PUNTO_VENTA', 1);

INSERT INTO usuarios (nombre, username, password_hash, rol) VALUES
    ('Administrador', 'admin', '$2a$12$placeholder_replace_on_first_login', 'DUENO');
