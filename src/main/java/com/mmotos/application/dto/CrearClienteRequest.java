package com.mmotos.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CrearClienteRequest(
    String cuit,
    @NotBlank String nombre,
    String direccion,
    String telefono,
    String email
) {}
