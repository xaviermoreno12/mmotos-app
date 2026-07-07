package com.mmotos.application.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record ActualizarFacturaBorradorRequest(
    String fechaFactura,
    String proveedorNombre,
    String cuit,
    String numeroFactura,
    BigDecimal montoTotal,
    String categoriaGasto,
    String estadoPago,
    @NotNull List<LineaFacturaDTO> lineas,
    String observaciones
) {}
