package com.mmotos.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ModificarVentaRequest(
    @Valid @NotNull List<LineaModificada> lineas,
    @NotBlank String motivo
) {
    public record LineaModificada(
        @NotNull UUID detalleId,
        @NotNull Integer cantidad,
        @NotNull BigDecimal precioUnitario
    ) {}
}
