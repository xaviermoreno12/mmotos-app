package com.mmotos.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CrearCobranzaRequest(
    @NotNull UUID clienteId,
    @NotNull @DecimalMin("0.01") BigDecimal monto,
    @NotBlank String metodoPago,
    String referencia,
    String observaciones,
    String usuarioId
) {}
