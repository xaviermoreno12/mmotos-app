package com.mmotos.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record LineaVenta(
    UUID productoId,
    String skuHistorico,
    String nombreHistorico,
    int cantidad,
    BigDecimal precioUnitarioHistorico  // "Foto" del precio al momento del cierre — inmutable
) {
    public LineaVenta {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        if (precioUnitarioHistorico.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio histórico no puede ser negativo");
        }
    }

    public BigDecimal subtotal() {
        return precioUnitarioHistorico.multiply(BigDecimal.valueOf(cantidad));
    }
}
