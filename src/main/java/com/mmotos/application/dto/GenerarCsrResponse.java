package com.mmotos.application.dto;

public record GenerarCsrResponse(
    String csrPem,          // Contenido PEM del CSR — subir a ARCA
    String clavePublicaPem  // Solo informativo, la privada se guarda en DB
) {}
