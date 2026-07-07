package com.mmotos.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CotizacionRequest(
    @NotNull @DecimalMin("1.00") BigDecimal valorDolar,
    String usuarioId
) {}
