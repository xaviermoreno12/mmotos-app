package com.mmotos.domain.model;

public enum SyncStatus {
    LOCAL,      // Solo guardada en la terminal local (modo offline)
    PENDING,    // En cola para sincronizar con servidor/AFIP
    SYNCED,     // Confirmada por AFIP o servidor central
    CONFLICT,   // Error de negocio al sincronizar (ej: CUIT inválido) — requiere intervención
    FAILED      // Error técnico — se reintentará con backoff exponencial
}
