package com.mmotos.application.dto;

import java.math.BigDecimal;

public record ChequeDTO(
    String id, String tipo, String numero, String banco, String librador,
    BigDecimal monto, String fechaEmision, String fechaCobro,
    String estado, String clienteId, String proveedorId, String observaciones
) {}
