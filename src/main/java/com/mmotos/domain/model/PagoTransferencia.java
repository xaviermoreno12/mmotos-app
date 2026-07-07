package com.mmotos.domain.model;

import java.math.BigDecimal;

public record PagoTransferencia(BigDecimal monto, String cbuOrigen) implements Pago {

    public PagoTransferencia {
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto de transferencia debe ser mayor a 0");
        }
    }

    @Override
    public MetodoPago metodo() {
        return MetodoPago.TRANSFERENCIA;
    }
}
