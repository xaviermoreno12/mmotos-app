package com.mmotos.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Request para cerrar la caja activa.
 */
public record CerrarCajaRequest(
    @NotNull(message = "El monto contado es obligatorio")
    @DecimalMin(value = "0.00", message = "El monto contado no puede ser negativo")
    BigDecimal montoFinalContado,

    String observaciones
) {}
