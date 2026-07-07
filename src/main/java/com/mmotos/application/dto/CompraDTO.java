package com.mmotos.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record CompraDTO(
    String id,
    String proveedorId,
    String proveedorNombre,
    String numeroRemito,
    String fecha,
    BigDecimal total,
    String metodoPago,
    String estado,
    String observaciones,
    List<CompraDetalleDTO> detalle
) {}
