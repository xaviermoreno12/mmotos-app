package com.mmotos.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductoDTO(
    UUID id,
    String sku,
    String nombre,
    BigDecimal precioBase,
    String moneda,
    BigDecimal precioEnPesos,
    int stockActual,
    int stockMinimo,
    boolean bajominimo,
    String ubicacionFisica,
    boolean esKit,
    boolean activo,
    BigDecimal precioCompra
) {}
