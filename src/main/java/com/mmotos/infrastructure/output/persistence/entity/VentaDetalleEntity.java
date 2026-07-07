package com.mmotos.infrastructure.output.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "ventas_detalles")
public class VentaDetalleEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "venta_id", nullable = false, columnDefinition = "uuid")
    private UUID ventaId;

    @Column(name = "producto_id", columnDefinition = "uuid")
    private UUID productoId;

    @Column(name = "sku_historico", nullable = false, length = 50)
    private String skuHistorico;

    @Column(name = "nombre_historico", nullable = false, length = 255)
    private String nombreHistorico;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario_historico", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitarioHistorico;

    protected VentaDetalleEntity() {}

    public VentaDetalleEntity(UUID id, UUID ventaId, UUID productoId, String skuHistorico,
                               String nombreHistorico, int cantidad, BigDecimal precioUnitarioHistorico) {
        this.id = id;
        this.ventaId = ventaId;
        this.productoId = productoId;
        this.skuHistorico = skuHistorico;
        this.nombreHistorico = nombreHistorico;
        this.cantidad = cantidad;
        this.precioUnitarioHistorico = precioUnitarioHistorico;
    }

    public UUID getId() { return id; }
    public UUID getVentaId() { return ventaId; }
    public UUID getProductoId() { return productoId; }
    public String getSkuHistorico() { return skuHistorico; }
    public String getNombreHistorico() { return nombreHistorico; }
    public Integer getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitarioHistorico() { return precioUnitarioHistorico; }

    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public void setPrecioUnitarioHistorico(BigDecimal precioUnitarioHistorico) { this.precioUnitarioHistorico = precioUnitarioHistorico; }
}
