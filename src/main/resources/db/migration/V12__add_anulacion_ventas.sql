ALTER TABLE ventas
  ADD COLUMN anulada          BOOLEAN      NOT NULL DEFAULT false,
  ADD COLUMN fecha_anulacion  TIMESTAMP,
  ADD COLUMN motivo_anulacion VARCHAR(255);

CREATE INDEX idx_ventas_anulada ON ventas(anulada) WHERE anulada = true;
