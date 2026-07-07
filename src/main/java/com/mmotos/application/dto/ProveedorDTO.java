package com.mmotos.application.dto;

public record ProveedorDTO(
    String id,
    String cuit,
    String nombre,
    String contacto,
    String telefono,
    String email,
    boolean activo
) {}
