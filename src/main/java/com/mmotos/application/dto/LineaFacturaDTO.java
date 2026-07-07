package com.mmotos.application.dto;

public record LineaFacturaDTO(
    String productoId,
    boolean esNuevo,
    String sku,
    String nombre,
    int cantidad,
    double precioUnitario,
    String ubicacionFisica,
    int stockMinimo,
    String moneda
) {}
