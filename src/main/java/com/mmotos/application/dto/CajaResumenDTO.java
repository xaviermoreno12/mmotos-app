package com.mmotos.application.dto;

import java.math.BigDecimal;

/**
 * DTO for the Caja (cash register) daily summary.
 */
public record CajaResumenDTO(
    long cantidadVentas,
    BigDecimal totalVentas,
    BigDecimal totalEfectivo,
    BigDecimal totalTarjeta,
    BigDecimal totalTransferencia,
    BigDecimal totalMercadoPago,
    BigDecimal totalOtros
) {}
