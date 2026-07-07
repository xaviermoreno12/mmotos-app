package com.mmotos.application.dto;

public record ActualizarMetodoPagoRequest(
    String nombre,
    Boolean aceptaCobro,
    Boolean aceptaPago,
    Boolean habilitado
) {}
