package com.mmotos.infrastructure.output.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sync_log")
public class SyncLogEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "entidad_id", nullable = false, columnDefinition = "uuid")
    private UUID entidadId;

    @Column(name = "entidad_tipo", nullable = false, length = 50)
    private String entidadTipo;

    @Column(nullable = false, length = 15)
    private String estado;

    @Column(nullable = false)
    private Integer intentos = 0;

    @Column(name = "ultimo_error", columnDefinition = "text")
    private String ultimoError;

    @Column(name = "proxima_sincronizacion", nullable = false)
    private LocalDateTime proximaSincronizacion;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected SyncLogEntity() {}

    public SyncLogEntity(UUID id, UUID entidadId, String entidadTipo) {
        this.id = id;
        this.entidadId = entidadId;
        this.entidadTipo = entidadTipo;
        this.estado = "PENDING";
        this.proximaSincronizacion = LocalDateTime.now();
    }

    public void registrarFallo(String error) {
        this.intentos++;
        this.ultimoError = error;
        this.estado = "FAILED";
        // Backoff exponencial: 1min → 4min → 9min → 16min...
        long minutosEspera = (long) Math.pow(intentos, 2);
        this.proximaSincronizacion = LocalDateTime.now().plusMinutes(minutosEspera);
    }

    public void marcarSynced() {
        this.estado = "SYNCED";
        this.ultimoError = null;
    }

    public void marcarConflicto(String error) {
        this.estado = "CONFLICT";
        this.ultimoError = error;
    }

    public UUID getId() { return id; }
    public UUID getEntidadId() { return entidadId; }
    public String getEntidadTipo() { return entidadTipo; }
    public String getEstado() { return estado; }
    public Integer getIntentos() { return intentos; }
    public String getUltimoError() { return ultimoError; }
    public LocalDateTime getProximaSincronizacion() { return proximaSincronizacion; }
}
