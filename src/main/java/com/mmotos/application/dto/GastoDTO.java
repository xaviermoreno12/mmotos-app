package com.mmotos.application.dto;

import java.math.BigDecimal;

public record GastoDTO(
    String id, String fecha, String descripcion, String categoria,
    BigDecimal monto, String metodoPago, String observaciones
) {}
