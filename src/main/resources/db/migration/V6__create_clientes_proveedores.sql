-- =============================================================================
-- V6: Tablas clientes y proveedores
-- =============================================================================

CREATE TABLE clientes (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cuit        VARCHAR(13)  UNIQUE,
    nombre      VARCHAR(100) NOT NULL,
    direccion   VARCHAR(200),
    telefono    VARCHAR(30),
    email       VARCHAR(100),
    saldo       DECIMAL(15,2) NOT NULL DEFAULT 0,
    activo      BOOLEAN NOT NULL DEFAULT true,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_clientes_nombre ON clientes USING GIN (to_tsvector('spanish', nombre));
CREATE INDEX idx_clientes_activo ON clientes (activo);

CREATE TABLE proveedores (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cuit        VARCHAR(13)  UNIQUE,
    nombre      VARCHAR(100) NOT NULL,
    contacto    VARCHAR(100),
    telefono    VARCHAR(30),
    email       VARCHAR(100),
    activo      BOOLEAN NOT NULL DEFAULT true,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_proveedores_nombre ON proveedores (nombre);
