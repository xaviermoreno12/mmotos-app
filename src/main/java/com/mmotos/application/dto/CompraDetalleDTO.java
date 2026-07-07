package com.mmotos.application.dto;

import java.math.BigDecimal;

public record CompraDetalleDTO(
    String productoId,
    String skuHistorico,
    String nombreHistorico,
    int cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal
) {}
