package com.mmotos.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record FacturaBorradorDTO(
    String id,
    String fechaRecepcion,
    String fechaFactura,
    String proveedorNombre,
    String cuit,
    String numeroFactura,
    String imagenBase64,
    String textoOcr,
    BigDecimal montoTotal,
    String categoriaGasto,
    String estadoPago,
    List<LineaFacturaDTO> lineas,
    String estado,
    String observaciones
) {}
