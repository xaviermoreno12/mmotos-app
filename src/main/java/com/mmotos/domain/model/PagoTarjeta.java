package com.mmotos.domain.model;

import java.math.BigDecimal;

public record PagoTarjeta(
    MetodoPago metodo,
    BigDecimal monto,
    String numeroCupon,
    int cuotas
) implements Pago {

    public PagoTarjeta {
        if (metodo != MetodoPago.TARJETA_DEBITO && metodo != MetodoPago.TARJETA_CREDITO) {
            throw new IllegalArgumentException("Método inválido para PagoTarjeta");
        }
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto de tarjeta debe ser mayor a 0");
        }
        if (cuotas < 1) {
            throw new IllegalArgumentException("Las cuotas deben ser al menos 1");
        }
    }
}
