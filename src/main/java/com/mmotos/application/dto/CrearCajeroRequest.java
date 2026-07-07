package com.mmotos.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearCajeroRequest(
    @NotBlank String nombre,
    @NotBlank String username,
    @NotBlank @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres") String password,
    @NotBlank @Pattern(regexp = "CAJERO|DUENO", message = "Rol debe ser CAJERO o DUENO") String rol
) {}
