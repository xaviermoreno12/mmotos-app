package com.mmotos.domain.model;

import java.math.BigDecimal;

public record PagoMercadoPago(BigDecimal monto, String referenciaPago) implements Pago {

    public PagoMercadoPago {
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto de MercadoPago debe ser mayor a 0");
        }
    }

    @Override
    public MetodoPago metodo() {
        return MetodoPago.MERCADO_PAGO;
    }
}
