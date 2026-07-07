package com.mmotos.application.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CrearCompraBorradorRequest(
    String proveedorNombre,
    String numeroRemito,
    String imagenBase64,
    @NotNull List<LineaBorradorRequest> lineas,
    String observaciones
) {
    public record LineaBorradorRequest(
        String productoId,
        String sku,
        String nombre,
        int cantidad,
        double precioUnitario
    ) {}
}
