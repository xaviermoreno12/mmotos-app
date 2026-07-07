package com.mmotos.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record FacturaExtraidaDTO(
    String fecha,
    String proveedor,
    String cuit,
    String numeroFactura,
    BigDecimal total,
    String categoria,
    String estadoPago,
    List<LineaExtraidaDTO> lineas
) {
    public record LineaExtraidaDTO(
        String nombre,
        int cantidad,
        BigDecimal precioUnitario
    ) {}
}
