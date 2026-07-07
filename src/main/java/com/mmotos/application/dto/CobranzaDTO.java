package com.mmotos.application.dto;

import java.math.BigDecimal;

public record CobranzaDTO(
    String id, String clienteId, String clienteNombre,
    BigDecimal monto, String fecha, String metodoPago,
    String referencia, String observaciones
) {}
