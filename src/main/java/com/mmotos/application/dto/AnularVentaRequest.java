package com.mmotos.application.dto;

import jakarta.validation.constraints.NotBlank;

public record AnularVentaRequest(@NotBlank String motivo) {}
