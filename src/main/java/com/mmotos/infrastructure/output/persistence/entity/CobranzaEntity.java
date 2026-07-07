package com.mmotos.infrastructure.output.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cobranzas")
public class CobranzaEntity {

    @Id @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "cliente_id", nullable = false, columnDefinition = "uuid")
    private UUID clienteId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "metodo_pago", nullable = false, length = 30)
    private String metodoPago;

    @Column(length = 100)
    private String referencia;

    @Column(columnDefinition = "text")
    private String observaciones;

    @Column(name = "usuario_id", columnDefinition = "uuid")
    private UUID usuarioId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected CobranzaEntity() {}

    public CobranzaEntity(UUID id, UUID clienteId, BigDecimal monto, String metodoPago,
                           String referencia, String observaciones, UUID usuarioId) {
        this.id           = id;
        this.clienteId    = clienteId;
        this.monto        = monto;
        this.metodoPago   = metodoPago;
        this.referencia   = referencia;
        this.observaciones = observaciones;
        this.usuarioId    = usuarioId;
    }

    public UUID getId() { return id; }
    public UUID getClienteId() { return clienteId; }
    public BigDecimal getMonto() { return monto; }
    public LocalDateTime getFecha() { return fecha; }
    public String getMetodoPago() { return metodoPago; }
    public String getReferencia() { return referencia; }
    public String getObservaciones() { return observaciones; }
}
