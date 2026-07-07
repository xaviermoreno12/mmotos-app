package com.mmotos.infrastructure.output.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "presupuestos_detalle")
public class PresupuestoDetalleEntity {

    @Id @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "presupuesto_id", nullable = false, columnDefinition = "uuid")
    private UUID presupuestoId;

    @Column(name = "producto_id", columnDefinition = "uuid")
    private UUID productoId;

    @Column(name = "sku_historico", nullable = false, length = 50)
    private String skuHistorico;

    @Column(name = "nombre_historico", nullable = false, length = 200)
    private String nombreHistorico;

    @Column(nullable = false)
    private int cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    protected PresupuestoDetalleEntity() {}

    public PresupuestoDetalleEntity(UUID id, UUID presupuestoId, UUID productoId,
                                     String skuHistorico, String nombreHistorico,
                                     int cantidad, BigDecimal precioUnitario) {
        this.id              = id;
        this.presupuestoId   = presupuestoId;
        this.productoId      = productoId;
        this.skuHistorico    = skuHistorico;
        this.nombreHistorico = nombreHistorico;
        this.cantidad        = cantidad;
        this.precioUnitario  = precioUnitario;
        this.subtotal        = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public UUID getId() { return id; }
    public UUID getPresupuestoId() { return presupuestoId; }
    public UUID getProductoId() { return productoId; }
    public String getSkuHistorico() { return skuHistorico; }
    public String getNombreHistorico() { return nombreHistorico; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
}
