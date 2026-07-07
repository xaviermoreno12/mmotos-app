package com.mmotos.infrastructure.output.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "gastos")
public class GastoEntity {

    @Id @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(nullable = false, length = 200)
    private String descripcion;

    @Column(nullable = false, length = 50)
    private String categoria;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(name = "metodo_pago", nullable = false, length = 30)
    private String metodoPago;

    @Column(name = "usuario_id", columnDefinition = "uuid")
    private UUID usuarioId;

    @Column(columnDefinition = "text")
    private String observaciones;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected GastoEntity() {}

    public GastoEntity(UUID id, LocalDateTime fecha, String descripcion, String categoria,
                       BigDecimal monto, String metodoPago, UUID usuarioId, String observaciones) {
        this.id           = id;
        this.fecha        = fecha;
        this.descripcion  = descripcion;
        this.categoria    = categoria;
        this.monto        = monto;
        this.metodoPago   = metodoPago;
        this.usuarioId    = usuarioId;
        this.observaciones = observaciones;
    }

    public UUID getId() { return id; }
    public LocalDateTime getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
    public String getCategoria() { return categoria; }
    public BigDecimal getMonto() { return monto; }
    public String getMetodoPago() { return metodoPago; }
    public UUID getUsuarioId() { return usuarioId; }
    public String getObservaciones() { return observaciones; }

    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
