package com.mmotos.domain.model;

import java.util.UUID;

public class RepuestoSimple extends Repuesto {

    public RepuestoSimple(UUID id, String sku, String nombre, Precio precio,
                          int stockActual, int stockMinimo, String ubicacionFisica) {
        this.id = id;
        this.sku = sku;
        this.nombre = nombre;
        this.precio = precio;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.ubicacionFisica = ubicacionFisica;
    }

    @Override
    public void descontarStock(int cantidad) {
        validarDisponibilidad(cantidad);
        this.stockActual -= cantidad;
    }
}
