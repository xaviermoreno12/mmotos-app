package com.mmotos.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CrearGastoRequest(
    @NotBlank String descripcion,
    @NotBlank String categoria,
    @NotNull @DecimalMin("0.01") BigDecimal monto,
    @NotBlank String metodoPago,
    String observaciones,
    String usuarioId
) {}
