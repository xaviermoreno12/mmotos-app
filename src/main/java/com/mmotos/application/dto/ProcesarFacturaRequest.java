package com.mmotos.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProcesarFacturaRequest(
    @NotBlank
    @Size(max = 10_000_000, message = "La imagen no puede superar los 7.5 MB")
    String imagenBase64
) {}
