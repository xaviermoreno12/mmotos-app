package com.mmotos.infrastructure.output.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cajas")
public class CajaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "usuario_id", nullable = false, columnDefinition = "uuid")
    private UUID usuarioId;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "monto_inicial", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoInicial;

    @Column(name = "monto_final_sistema", precision = 15, scale = 2)
    private BigDecimal montoFinalSistema;

    @Column(name = "monto_final_contado", precision = 15, scale = 2)
    private BigDecimal montoFinalContado;

    @Column(name = "diferencia", precision = 15, scale = 2)
    private BigDecimal diferencia;

    @Column(name = "observaciones")
    private String observaciones;

    @Column(name = "estado", nullable = false, length = 10)
    private String estado;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected CajaEntity() {}

    public CajaEntity(UUID id, UUID usuarioId, BigDecimal montoInicial) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.fechaApertura = LocalDateTime.now();
        this.montoInicial = montoInicial;
        this.estado = "ABIERTA";
    }

    // --- Getters ---
    public UUID getId() { return id; }
    public UUID getUsuarioId() { return usuarioId; }
    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public BigDecimal getMontoInicial() { return montoInicial; }
    public BigDecimal getMontoFinalSistema() { return montoFinalSistema; }
    public BigDecimal getMontoFinalContado() { return montoFinalContado; }
    public BigDecimal getDiferencia() { return diferencia; }
    public String getObservaciones() { return observaciones; }
    public String getEstado() { return estado; }

    // --- Setters para cierre ---
    public void cerrar(BigDecimal montoFinalSistema, BigDecimal montoFinalContado, String observaciones) {
        this.fechaCierre = LocalDateTime.now();
        this.montoFinalSistema = montoFinalSistema;
        this.montoFinalContado = montoFinalContado;
        this.diferencia = montoFinalContado.subtract(montoFinalSistema);
        this.observaciones = observaciones;
        this.estado = "CERRADA";
    }
}
