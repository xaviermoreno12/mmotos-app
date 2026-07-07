package com.mmotos.infrastructure.output.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "configuracion_sistema")
public class ConfiguracionEntity {

    @Id
    @Column(length = 50)
    private String clave;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal valor;

    @Column(name = "ultima_actualizacion", nullable = false)
    private LocalDateTime ultimaActualizacion = LocalDateTime.now();

    @Column(name = "usuario_id", columnDefinition = "uuid")
    private UUID usuarioId;

    protected ConfiguracionEntity() {}

    public ConfiguracionEntity(String clave, BigDecimal valor, UUID usuarioId) {
        this.clave = clave;
        this.valor = valor;
        this.usuarioId = usuarioId;
        this.ultimaActualizacion = LocalDateTime.now();
    }

    public String getClave() { return clave; }
    public BigDecimal getValor() { return valor; }
    public LocalDateTime getUltimaActualizacion() { return ultimaActualizacion; }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
        this.ultimaActualizacion = LocalDateTime.now();
    }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }
}
