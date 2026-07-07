package com.mmotos.application.dto;

import jakarta.validation.constraints.NotBlank;

public record DistinguishedNameDTO(
    @NotBlank String cn,            // Common Name (nombre del titular o sistema)
    @NotBlank String o,             // Organization (razón social)
    String ou,                      // Organizational Unit (rubro)
    String l,                       // Locality (ciudad)
    String st,                      // State (provincia)
    @NotBlank String cuit           // CUIT sin guiones (ej: 20449623453)
) {}
