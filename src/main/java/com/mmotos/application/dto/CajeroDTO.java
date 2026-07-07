package com.mmotos.application.dto;

public record CajeroDTO(
    String id,
    String nombre,
    String username,
    String rol,
    boolean activo,
    String createdAt
) {}
