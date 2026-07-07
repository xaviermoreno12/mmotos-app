package com.mmotos.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record PresupuestoDTO(
    String id, String clienteId, String clienteNombre,
    String fecha, String fechaValidez, BigDecimal total,
    String estado, String observaciones,
    List<CompraDetalleDTO> detalle
) {}
