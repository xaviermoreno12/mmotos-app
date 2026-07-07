package com.mmotos.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for listing ventas in the Ventas page.
 * Includes basic venta info plus payment method summary.
 */
public record VentaListDTO(
    UUID id,
    String numeroTicket,
    String tipoFactura,
    LocalDateTime fechaEmision,
    BigDecimal total,
    String estadoFiscal,
    String cajero,
    String clienteCuit,
    List<PagoResumenDTO> pagos,
    boolean anulada,
    boolean modificada
) {
    public record PagoResumenDTO(String metodo, BigDecimal monto) {}
}
