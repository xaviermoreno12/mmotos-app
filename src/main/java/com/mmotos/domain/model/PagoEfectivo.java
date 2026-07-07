package com.mmotos.domain.model;

import java.math.BigDecimal;

public record PagoEfectivo(BigDecimal monto) implements Pago {

    public PagoEfectivo {
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto en efectivo debe ser mayor a 0");
        }
    }

    @Override
    public MetodoPago metodo() {
        return MetodoPago.EFECTIVO;
    }
}
