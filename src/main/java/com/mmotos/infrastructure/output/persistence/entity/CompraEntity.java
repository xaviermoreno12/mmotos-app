package com.mmotos.infrastructure.output.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "compras")
public class CompraEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "proveedor_id", columnDefinition = "uuid")
    private UUID proveedorId;

    @Column(name = "proveedor_nombre", nullable = false, length = 100)
    private String proveedorNombre;

    @Column(name = "numero_remito", length = 50)
    private String numeroRemito;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    @Column(name = "metodo_pago", nullable = false, length = 30)
    private String metodoPago;

    @Column(nullable = false, length = 20)
    private String estado = "COMPLETADA";

    @Column(name = "usuario_id", columnDefinition = "uuid")
    private UUID usuarioId;

    @Column(columnDefinition = "text")
    private String observaciones;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected CompraEntity() {}

    public CompraEntity(UUID id, UUID proveedorId, String proveedorNombre, String numeroRemito,
                        BigDecimal total, String metodoPago, UUID usuarioId, String observaciones) {
        this.id              = id;
        this.proveedorId     = proveedorId;
        this.proveedorNombre = proveedorNombre;
        this.numeroRemito    = numeroRemito;
        this.total           = total;
        this.metodoPago      = metodoPago;
        this.usuarioId       = usuarioId;
        this.observaciones   = observaciones;
    }

    public UUID getId() { return id; }
    public UUID getProveedorId() { return proveedorId; }
    public String getProveedorNombre() { return proveedorNombre; }
    public String getNumeroRemito() { return numeroRemito; }
    public LocalDateTime getFecha() { return fecha; }
    public BigDecimal getTotal() { return total; }
    public String getMetodoPago() { return metodoPago; }
    public String getEstado() { return estado; }
    public UUID getUsuarioId() { return usuarioId; }
    public String getObservaciones() { return observaciones; }
}
