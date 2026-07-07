package com.mmotos.domain.model;

public enum EstadoFiscal {
    PENDIENTE,          // Venta guardada, aún no enviada a AFIP/controlador
    APROBADO,           // CAE o ticket físico emitido correctamente
    ERROR_HARDWARE,     // Fallo en impresora fiscal — stock revertido por rollback
    CONTINGENCIA        // AFIP no disponible — operando con CAEA (código de contingencia)
}
