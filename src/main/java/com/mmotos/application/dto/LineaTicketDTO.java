package com.mmotos.application.dto;

import java.math.BigDecimal;

public record LineaTicketDTO(
    String nombreHistorico,
    String skuHistorico,
    int cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal
) {}
