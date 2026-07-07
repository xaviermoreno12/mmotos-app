package com.mmotos.application.dto;

import java.math.BigDecimal;

public record ClienteDTO(
    String id,
    String cuit,
    String nombre,
    String direccion,
    String telefono,
    String email,
    BigDecimal saldo,
    boolean activo
) {}
