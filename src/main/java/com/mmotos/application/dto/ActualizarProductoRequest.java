package com.mmotos.application.dto;

import java.math.BigDecimal;

public record ActualizarProductoRequest(
    String nombre,
    BigDecimal precioBase,
    String moneda,
    Integer stockActual,
    Integer stockMinimo,
    String ubicacionFisica,
    Boolean activo,
    BigDecimal precioCompra
) {}
