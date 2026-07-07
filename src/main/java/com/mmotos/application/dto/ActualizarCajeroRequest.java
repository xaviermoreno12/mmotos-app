package com.mmotos.application.dto;

public record ActualizarCajeroRequest(
    String nombre,
    String password,
    String rol,
    Boolean activo
) {}
