package com.mmotos.domain.model;

public record ComponenteKit(Repuesto repuesto, int cantidad) {
    public ComponenteKit {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad del componente debe ser mayor a 0");
    }
}
