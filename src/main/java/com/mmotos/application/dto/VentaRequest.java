package com.mmotos.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record VentaRequest(
    @NotNull String tipoFactura,          // A | B | C | NO_FISCAL
    String cuitCliente,                   // obligatorio si tipoFactura = A
    @NotNull @NotEmpty @Valid List<LineaVentaRequest> lineas,
    @NotNull @NotEmpty @Valid List<PagoRequest> pagos,
    UUID usuarioId
) {}
