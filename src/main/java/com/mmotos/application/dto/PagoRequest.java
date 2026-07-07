package com.mmotos.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PagoRequest(
    @NotNull String metodo,          // EFECTIVO | TARJETA_DEBITO | TARJETA_CREDITO | TRANSFERENCIA | MERCADO_PAGO
    @NotNull @DecimalMin("0.01") BigDecimal monto,
    String numeroCupon,              // para tarjeta
    Integer cuotas,                  // para tarjeta crédito
    String cbuOrigen,                // para transferencia
    String referenciaPago            // para MercadoPago
) {}
