package com.mmotos.infrastructure.output.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "venta_modificaciones")
public class VentaModificacionEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "venta_id", nullable = false, columnDefinition = "uuid")
    private UUID ventaId;

    @Column(name = "usuario_id", columnDefinition = "uuid")
    private UUID usuarioId;

    @Column(name = "usuario_nombre", nullable = false, length = 100)
    private String usuarioNombre;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, length = 50)
    private String campo;

    @Column(name = "detalle_id", columnDefinition = "uuid")
    private UUID detalleId;

    @Column(name = "valor_anterior", nullable = false, columnDefinition = "text")
    private String valorAnterior;

    @Column(name = "valor_nuevo", nullable = false, columnDefinition = "text")
    private String valorNuevo;

    @Column(nullable = false, length = 255)
    private String motivo;

    protected VentaModificacionEntity() {}

    public VentaModificacionEntity(UUID id, UUID ventaId, UUID usuarioId, String usuarioNombre,
                                    LocalDateTime fecha, String campo, UUID detalleId,
                                    String valorAnterior, String valorNuevo, String motivo) {
        this.id = id;
        this.ventaId = ventaId;
        this.usuarioId = usuarioId;
        this.usuarioNombre = usuarioNombre;
        this.fecha = fecha;
        this.campo = campo;
        this.detalleId = detalleId;
        this.valorAnterior = valorAnterior;
        this.valorNuevo = valorNuevo;
        this.motivo = motivo;
    }

    public UUID getId() { return id; }
    public UUID getVentaId() { return ventaId; }
    public UUID getUsuarioId() { return usuarioId; }
    public String getUsuarioNombre() { return usuarioNombre; }
    public LocalDateTime getFecha() { return fecha; }
    public String getCampo() { return campo; }
    public UUID getDetalleId() { return detalleId; }
    public String getValorAnterior() { return valorAnterior; }
    public String getValorNuevo() { return valorNuevo; }
    public String getMotivo() { return motivo; }
}
