package com.mmotos.domain.model;

import java.math.BigDecimal;

public sealed interface Pago permits PagoEfectivo, PagoTarjeta, PagoTransferencia, PagoMercadoPago {
    MetodoPago metodo();
    BigDecimal monto();
}
