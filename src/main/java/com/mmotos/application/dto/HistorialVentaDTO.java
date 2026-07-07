package com.mmotos.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistorialVentaDTO(
    String ventaId,
    String numeroTicket,
    LocalDateTime fechaEmision,
    int cantidad,
    BigDecimal precioUnitario
) {}
