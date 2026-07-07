package com.mmotos.application.dto;

public record ProductoFiltroDTO(
    String sku,
    String nombre,
    String atributoJson,   // ej: {"viscosidad":"20W50"} — usa índice GIN con @>
    String terminoParcial  // ej: "110" — búsqueda fuzzy en atributos con pg_trgm
) {}
