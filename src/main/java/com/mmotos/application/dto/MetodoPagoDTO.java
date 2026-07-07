package com.mmotos.application.dto;

public record MetodoPagoDTO(
    String id,
    String codigo,
    String nombre,
    boolean aceptaCobro,
    boolean aceptaPago,
    boolean habilitado,
    int orden
) {}
