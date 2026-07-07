package com.mmotos.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record VentaModificacionDTO(
    UUID id,
    String usuarioNombre,
    LocalDateTime fecha,
    String campo,
    String valorAnterior,
    String valorNuevo,
    String motivo
) {}
