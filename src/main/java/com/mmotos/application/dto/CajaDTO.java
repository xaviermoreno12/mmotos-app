package com.mmotos.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing the full state of a Caja (cash register session).
 */
public record CajaDTO(
    UUID id,
    String cajeroNombre,
    String cajeroUsername,
    LocalDateTime fechaApertura,
    LocalDateTime fechaCierre,
    BigDecimal montoInicial,
    BigDecimal montoFinalSistema,
    BigDecimal montoFinalContado,
    BigDecimal diferencia,
    String observaciones,
    String estado,
    // Resumen de ventas de esta caja
    CajaResumenDTO resumen
) {}
