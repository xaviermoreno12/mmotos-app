package com.mmotos.application.dto;

public record ConfirmarFacturaResultDTO(
    String compraId,
    String gastoId,
    int productosCreados,
    int productosActualizados
) {}
