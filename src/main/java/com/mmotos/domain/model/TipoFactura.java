package com.mmotos.domain.model;

public enum TipoFactura {
    A,          // Responsable Inscripto (requiere CUIT del cliente)
    B,          // Consumidor Final
    C,          // Monotributista
    NO_FISCAL   // Ticket interno / comanda (no válido legalmente)
}
