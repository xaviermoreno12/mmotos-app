package com.mmotos.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CrearMetodoPagoRequest(
    @NotBlank String codigo,
    @NotBlank String nombre,
    boolean aceptaCobro,
    boolean aceptaPago
) {}
