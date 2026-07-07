package com.mmotos.infrastructure.output.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "compras_detalle")
public class CompraDetalleEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "compra_id", nullable = false, columnDefinition = "uuid")
    private UUID compraId;

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

    protected CompraDetalleEntity() {}

    public CompraDetalleEntity(UUID id, UUID compraId, UUID productoId, String skuHistorico,
                                String nombreHistorico, int cantidad, BigDecimal precioUnitario) {
        this.id             = id;
        this.compraId       = compraId;
        this.productoId     = productoId;
        this.skuHistorico   = skuHistorico;
        this.nombreHistorico = nombreHistorico;
        this.cantidad       = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal       = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public UUID getId() { return id; }
    public UUID getCompraId() { return compraId; }
    public UUID getProductoId() { return productoId; }
    public String getSkuHistorico() { return skuHistorico; }
    public String getNombreHistorico() { return nombreHistorico; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
}
