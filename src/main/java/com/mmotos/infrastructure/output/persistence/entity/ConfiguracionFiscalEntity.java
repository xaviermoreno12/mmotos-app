package com.mmotos.infrastructure.output.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "configuracion_fiscal")
public class ConfiguracionFiscalEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(unique = true, nullable = false, length = 50)
    private String alias;

    @Column(nullable = false, columnDefinition = "text")
    private String valor;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected ConfiguracionFiscalEntity() {}

    public ConfiguracionFiscalEntity(String alias, String valor) {
        this.id = UUID.randomUUID();
        this.alias = alias;
        this.valor = valor;
    }

    public UUID getId() { return id; }
    public String getAlias() { return alias; }
    public String getValor() { return valor; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setValor(String valor) { this.valor = valor; }
}
