package com.mmotos.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class KitComposicion extends Repuesto {

    private final List<ComponenteKit> componentes;
    private BigDecimal precioPromocional; // Si es null, se suma el precio de los componentes

    public KitComposicion(UUID id, String sku, String nombre, List<ComponenteKit> componentes,
                          int stockMinimo, String ubicacionFisica) {
        this.id = id;
        this.sku = sku;
        this.nombre = nombre;
        this.componentes = componentes;
        this.stockMinimo = stockMinimo;
        this.ubicacionFisica = ubicacionFisica;
        this.stockActual = calcularStockDisponible();
    }

    @Override
    public void descontarStock(int cantidad) {
        // Composite Pattern: primero validamos TODOS para garantizar atomicidad
        componentes.forEach(c -> c.repuesto().validarDisponibilidad(c.cantidad() * cantidad));
        // Si todos tienen stock, descontamos en cascada
        componentes.forEach(c -> c.repuesto().descontarStock(c.cantidad() * cantidad));
        this.stockActual = calcularStockDisponible();
    }

    public BigDecimal obtenerPrecioFinal(BigDecimal cotizacionDolar) {
        if (precioPromocional != null) {
            return precioPromocional;
        }
        return componentes.stream()
            .map(c -> c.repuesto().getPrecio()
                    .calcularEnPesos(cotizacionDolar)
                    .multiply(BigDecimal.valueOf(c.cantidad())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // El stock del kit es el mínimo que se puede armar con los componentes disponibles
    private int calcularStockDisponible() {
        return componentes.stream()
            .mapToInt(c -> c.repuesto().getStockActual() / c.cantidad())
            .min()
            .orElse(0);
    }

    public List<ComponenteKit> getComponentes() { return componentes; }

    public void setPrecioPromocional(BigDecimal precioPromocional) {
        this.precioPromocional = precioPromocional;
    }
}
