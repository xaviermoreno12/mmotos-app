-- Tabla para almacenar claves y certificados del sistema fiscal (ARCA/AFIP)
CREATE TABLE configuracion_fiscal (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    alias      VARCHAR(50)  UNIQUE NOT NULL,
    valor      TEXT         NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);

-- Seed: guardar la clave privada generada (será actualizada desde la app)
-- La clave privada real se inserta vía el endpoint POST /api/fiscal/generar-csr
