package com.mmotos.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CrearProveedorRequest(
    String cuit,
    @NotBlank String nombre,
    String contacto,
    String telefono,
    String email
) {}
