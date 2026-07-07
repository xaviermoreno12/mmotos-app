package com.mmotos.application.dto;

import java.util.List;

public record CompraBorradorDTO(
    String id,
    String fechaRecepcion,
    String proveedorNombre,
    String numeroRemito,
    String imagenBase64,
    List<LineaBorradorDTO> lineas,
    String estado,
    String observaciones
) {
    public record LineaBorradorDTO(
        String productoId,
        String sku,
        String nombre,
        int cantidad,
        double precioUnitario
    ) {}
}
