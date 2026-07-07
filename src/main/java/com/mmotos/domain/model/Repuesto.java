package com.mmotos.domain.model;

import com.mmotos.domain.exception.InsufficientStockException;

import java.util.UUID;

public abstract class Repuesto {

    protected UUID id;
    protected String sku;
    protected String nombre;
    protected Precio precio;
    protected Integer stockActual;
    protected Integer stockMinimo;
    protected String ubicacionFisica;
    protected boolean activo = true;

    public abstract void descontarStock(int cantidad);

    protected void validarDisponibilidad(int cantidad) {
        if (this.stockActual < cantidad) {
            throw new InsufficientStockException(
                "Stock insuficiente para '%s' (SKU: %s). Disponible: %d, solicitado: %d"
                    .formatted(nombre, sku, stockActual, cantidad)
            );
        }
    }

    public boolean estaBajoMinimo() {
        return stockActual <= stockMinimo;
    }

    public UUID getId() { return id; }
    public String getSku() { return sku; }
    public String getNombre() { return nombre; }
    public Precio getPrecio() { return precio; }
    public Integer getStockActual() { return stockActual; }
    public Integer getStockMinimo() { return stockMinimo; }
    public String getUbicacionFisica() { return ubicacionFisica; }
    public boolean isActivo() { return activo; }

    // Uso exclusivo del mapper de infraestructura al reconstruir el dominio desde persistencia
    void actualizarPrecio(Precio precio) { this.precio = precio; }
    void actualizarStock(Integer stockActual) { this.stockActual = stockActual; }
}
