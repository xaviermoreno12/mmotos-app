package com.mmotos.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record CrearCompraRequest(
    UUID proveedorId,
    String proveedorNombre,
    String numeroRemito,
    @NotBlank String metodoPago,
    @NotEmpty @Valid List<LineaCompraRequest> lineas,
    String observaciones,
    String usuarioId
) {}
