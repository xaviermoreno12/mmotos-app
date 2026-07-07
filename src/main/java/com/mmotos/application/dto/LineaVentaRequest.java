package com.mmotos.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LineaVentaRequest(
    @NotNull UUID productoId,
    @Min(1) int cantidad
) {}
