package com.mmotos.application.dto;

import java.math.BigDecimal;

public record RentabilidadDTO(
    BigDecimal ventas,
    BigDecimal costos,
    BigDecimal beneficio,
    long cantidadVentas
) {}
