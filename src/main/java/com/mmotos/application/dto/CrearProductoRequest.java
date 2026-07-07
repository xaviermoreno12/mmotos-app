package com.mmotos.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record CrearProductoRequest(
    @NotBlank String sku,
    @NotBlank String nombre,
    @NotNull @DecimalMin("0.01") BigDecimal precioBase,
    @NotBlank @Pattern(regexp = "ARS|USD") String moneda,
    @Min(0) int stockActual,
    @Min(0) int stockMinimo,
    String ubicacionFisica,
    BigDecimal precioCompra
) {}
