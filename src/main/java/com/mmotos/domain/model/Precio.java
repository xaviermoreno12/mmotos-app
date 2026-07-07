package com.mmotos.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public record Precio(
    BigDecimal valorBase,
    Moneda moneda,
    LocalDateTime ultimaActualizacion
) {
    public Precio {
        if (valorBase == null || valorBase.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo ni nulo");
        }
    }

    public BigDecimal calcularEnPesos(BigDecimal cotizacionDolar) {
        if (moneda == Moneda.ARS) {
            return valorBase;
        }
        if (cotizacionDolar == null || cotizacionDolar.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cotización del dólar inválida");
        }
        return valorBase.multiply(cotizacionDolar).setScale(2, RoundingMode.HALF_UP);
    }
}
