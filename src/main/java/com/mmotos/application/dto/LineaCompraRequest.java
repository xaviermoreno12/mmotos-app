package com.mmotos.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record LineaCompraRequest(
    @NotNull UUID productoId,
    @NotBlank String skuHistorico,
    @NotBlank String nombreHistorico,
    @Min(1) int cantidad,
    @NotNull @DecimalMin("0.00") BigDecimal precioUnitario
) {}
