package com.mmotos.application.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CrearChequeRequest(
    @NotBlank @Pattern(regexp = "RECIBIDO|EMITIDO") String tipo,
    @NotBlank String numero,
    @NotBlank String banco,
    String librador,
    @NotNull @DecimalMin("0.01") BigDecimal monto,
    @NotNull LocalDate fechaEmision,
    @NotNull LocalDate fechaCobro,
    UUID clienteId,
    UUID proveedorId,
    String observaciones
) {}
