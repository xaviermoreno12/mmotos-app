-- Convierte custom enums de PostgreSQL a varchar para compatibilidad con Hibernate 6 JPA.
-- Las columnas enum no aceptan character varying en INSERT sin un cast explícito.

-- Paso 1: Eliminar índices parciales que dependen de las columnas enum
DROP INDEX IF EXISTS idx_ventas_sync_status;
DROP INDEX IF EXISTS idx_sync_log_pendiente;

-- Paso 2: Alterar columnas — usuarios
ALTER TABLE usuarios ALTER COLUMN rol DROP DEFAULT;
ALTER TABLE usuarios ALTER COLUMN rol TYPE varchar(10) USING rol::text;
ALTER TABLE usuarios ALTER COLUMN rol SET DEFAULT 'CAJERO';

-- Paso 3: Alterar columnas — productos
ALTER TABLE productos ALTER COLUMN moneda DROP DEFAULT;
ALTER TABLE productos ALTER COLUMN moneda TYPE varchar(3) USING moneda::text;
ALTER TABLE productos ALTER COLUMN moneda SET DEFAULT 'ARS';

-- Paso 4: Alterar columnas — ventas
ALTER TABLE ventas ALTER COLUMN tipo_factura  DROP DEFAULT;
ALTER TABLE ventas ALTER COLUMN tipo_factura  TYPE varchar(10) USING tipo_factura::text;
ALTER TABLE ventas ALTER COLUMN tipo_factura  SET DEFAULT 'B';

ALTER TABLE ventas ALTER COLUMN estado_fiscal DROP DEFAULT;
ALTER TABLE ventas ALTER COLUMN estado_fiscal TYPE varchar(20) USING estado_fiscal::text;
ALTER TABLE ventas ALTER COLUMN estado_fiscal SET DEFAULT 'PENDIENTE';

ALTER TABLE ventas ALTER COLUMN sync_status   DROP DEFAULT;
ALTER TABLE ventas ALTER COLUMN sync_status   TYPE varchar(20) USING sync_status::text;
ALTER TABLE ventas ALTER COLUMN sync_status   SET DEFAULT 'LOCAL';

-- Paso 5: Alterar columnas — pagos
ALTER TABLE pagos ALTER COLUMN metodo TYPE varchar(30) USING metodo::text;

-- Paso 6: Alterar columnas — sync_log (columna se llama "estado", no "sync_status")
ALTER TABLE sync_log ALTER COLUMN estado DROP DEFAULT;
ALTER TABLE sync_log ALTER COLUMN estado TYPE varchar(20) USING estado::text;
ALTER TABLE sync_log ALTER COLUMN estado SET DEFAULT 'PENDING';

-- Paso 7: Eliminar tipos enum (ya no tienen dependencias)
DROP TYPE IF EXISTS moneda_enum;
DROP TYPE IF EXISTS estado_fiscal_enum;
DROP TYPE IF EXISTS sync_status_enum;
DROP TYPE IF EXISTS tipo_factura_enum;
DROP TYPE IF EXISTS metodo_pago_enum;
DROP TYPE IF EXISTS rol_usuario_enum;

-- Paso 8: Recrear índices parciales (ahora con varchar)
CREATE INDEX idx_ventas_sync_status ON ventas (sync_status)
    WHERE sync_status IN ('LOCAL', 'PENDING', 'FAILED');

CREATE INDEX idx_sync_log_pendiente ON sync_log (proxima_sincronizacion)
    WHERE estado IN ('PENDING', 'FAILED');
