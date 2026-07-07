package com.mmotos.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record VentaResponse(
    UUID id,
    String numeroTicket,
    String cae,
    String estadoFiscal,
    String syncStatus,
    BigDecimal total,
    LocalDateTime fechaEmision
) {}
