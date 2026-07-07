package com.mmotos.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record VentaDetalleCompletoDTO(
    UUID id,
    String numeroTicket,
    String cae,
    String estadoFiscal,
    LocalDateTime fechaEmision,
    BigDecimal total,
    List<LineaTicketDTO> lineas,
    List<PagoTicketDTO> pagos,
    boolean modificada,
    List<VentaModificacionDTO> modificaciones
) {}
