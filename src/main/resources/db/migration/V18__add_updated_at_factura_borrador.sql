-- M2: Columna para registrar la fecha de ultima modificacion del borrador
ALTER TABLE factura_borrador ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
