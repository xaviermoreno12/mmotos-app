package com.mmotos.infrastructure.output.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "factura_borrador")
public class FacturaBorradorEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id = UUID.randomUUID();

    @Column(name = "fecha_recepcion", nullable = false)
    private LocalDateTime fechaRecepcion = LocalDateTime.now();

    @Column(name = "fecha_factura", length = 20)
    private String fechaFactura;

    @Column(name = "proveedor_nombre", length = 200)
    private String proveedorNombre;

    @Column(length = 20)
    private String cuit;

    @Column(name = "numero_factura", length = 100)
    private String numeroFactura;

    @Column(name = "imagen_base64", columnDefinition = "text")
    private String imagenBase64;

    @Column(name = "texto_ocr", columnDefinition = "text")
    private String textoOcr;

    @Column(name = "monto_total", precision = 15, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "categoria_gasto", length = 50)
    private String categoriaGasto;

    @Column(name = "estado_pago", nullable = false, length = 20)
    private String estadoPago = "Pendiente";

    @Column(nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String lineas = "[]";

    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Column(columnDefinition = "text")
    private String observaciones;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // M2: Fecha de ultima modificacion — actualizada automaticamente por Hibernate
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected FacturaBorradorEntity() {}

    public FacturaBorradorEntity(String fechaFactura, String proveedorNombre, String cuit,
                                  String numeroFactura, String imagenBase64, String textoOcr,
                                  BigDecimal montoTotal, String categoriaGasto, String estadoPago,
                                  String lineas, String observaciones) {
        this.fechaFactura    = fechaFactura;
        this.proveedorNombre = proveedorNombre;
        this.cuit            = cuit;
        this.numeroFactura   = numeroFactura;
        this.imagenBase64    = imagenBase64;
        this.textoOcr        = textoOcr;
        this.montoTotal      = montoTotal;
        this.categoriaGasto  = categoriaGasto;
        this.estadoPago      = estadoPago != null ? estadoPago : "Pendiente";
        this.lineas          = lineas != null ? lineas : "[]";
        this.observaciones   = observaciones;
    }

    public UUID getId() { return id; }
    public LocalDateTime getFechaRecepcion() { return fechaRecepcion; }
    public String getFechaFactura() { return fechaFactura; }
    public String getProveedorNombre() { return proveedorNombre; }
    public String getCuit() { return cuit; }
    public String getNumeroFactura() { return numeroFactura; }
    public String getImagenBase64() { return imagenBase64; }
    public String getTextoOcr() { return textoOcr; }
    public BigDecimal getMontoTotal() { return montoTotal; }
    public String getCategoriaGasto() { return categoriaGasto; }
    public String getEstadoPago() { return estadoPago; }
    public String getLineas() { return lineas; }
    public String getEstado() { return estado; }
    public String getObservaciones() { return observaciones; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setFechaFactura(String fechaFactura) { this.fechaFactura = fechaFactura; }
    public void setProveedorNombre(String proveedorNombre) { this.proveedorNombre = proveedorNombre; }
    public void setCuit(String cuit) { this.cuit = cuit; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }
    public void setMontoTotal(BigDecimal montoTotal) { this.montoTotal = montoTotal; }
    public void setCategoriaGasto(String categoriaGasto) { this.categoriaGasto = categoriaGasto; }
    public void setEstadoPago(String estadoPago) { this.estadoPago = estadoPago; }
    public void setLineas(String lineas) { this.lineas = lineas; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
