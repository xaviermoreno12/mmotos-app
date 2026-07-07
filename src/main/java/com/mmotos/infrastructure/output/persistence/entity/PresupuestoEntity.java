package com.mmotos.infrastructure.output.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "presupuestos")
public class PresupuestoEntity {

    @Id @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "cliente_id", columnDefinition = "uuid")
    private UUID clienteId;

    @Column(name = "cliente_nombre", nullable = false, length = 100)
    private String clienteNombre;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "fecha_validez", nullable = false)
    private LocalDateTime fechaValidez;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    @Column(nullable = false, length = 20)
    private String estado = "BORRADOR";

    @Column(columnDefinition = "text")
    private String observaciones;

    @Column(name = "usuario_id", columnDefinition = "uuid")
    private UUID usuarioId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected PresupuestoEntity() {}

    public PresupuestoEntity(UUID id, UUID clienteId, String clienteNombre,
                              LocalDateTime fechaValidez, BigDecimal total,
                              String observaciones, UUID usuarioId) {
        this.id            = id;
        this.clienteId     = clienteId;
        this.clienteNombre = clienteNombre;
        this.fechaValidez  = fechaValidez;
        this.total         = total;
        this.observaciones = observaciones;
        this.usuarioId     = usuarioId;
    }

    public UUID getId() { return id; }
    public UUID getClienteId() { return clienteId; }
    public String getClienteNombre() { return clienteNombre; }
    public LocalDateTime getFecha() { return fecha; }
    public LocalDateTime getFechaValidez() { return fechaValidez; }
    public BigDecimal getTotal() { return total; }
    public String getEstado() { return estado; }
    public String getObservaciones() { return observaciones; }
    public UUID getUsuarioId() { return usuarioId; }

    public void setEstado(String estado) { this.estado = estado; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
