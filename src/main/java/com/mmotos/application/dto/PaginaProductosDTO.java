package com.mmotos.application.dto;

import java.util.List;

public record PaginaProductosDTO(
    List<ProductoDTO> contenido,
    int paginaActual,
    int totalPaginas,
    long totalElementos,
    boolean esUltima
) {}
