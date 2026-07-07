package com.mmotos.infrastructure.output.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "compra_borrador")
public class CompraBorradorEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id = UUID.randomUUID();

    @Column(name = "fecha_recepcion", nullable = false)
    private LocalDateTime fechaRecepcion = LocalDateTime.now();

    @Column(name = "proveedor_nombre", length = 200)
    private String proveedorNombre;

    @Column(name = "numero_remito", length = 100)
    private String numeroRemito;

    @Column(name = "imagen_base64", columnDefinition = "text")
    private String imagenBase64;

    @Column(nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String lineas = "[]";

    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Column(columnDefinition = "text")
    private String observaciones;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected CompraBorradorEntity() {}

    public CompraBorradorEntity(String proveedorNombre, String numeroRemito,
                                 String imagenBase64, String lineas, String observaciones) {
        this.proveedorNombre = proveedorNombre;
        this.numeroRemito    = numeroRemito;
        this.imagenBase64    = imagenBase64;
        this.lineas          = lineas != null ? lineas : "[]";
        this.observaciones   = observaciones;
    }

    public UUID getId() { return id; }
    public LocalDateTime getFechaRecepcion() { return fechaRecepcion; }
    public String getProveedorNombre() { return proveedorNombre; }
    public String getNumeroRemito() { return numeroRemito; }
    public String getImagenBase64() { return imagenBase64; }
    public String getLineas() { return lineas; }
    public String getEstado() { return estado; }
    public String getObservaciones() { return observaciones; }

    public void setLineas(String lineas) { this.lineas = lineas; }
    public void setEstado(String estado) { this.estado = estado; }
}
