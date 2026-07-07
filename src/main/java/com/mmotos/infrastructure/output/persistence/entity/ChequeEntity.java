package com.mmotos.infrastructure.output.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cheques")
public class ChequeEntity {

    @Id @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 10)
    private String tipo;

    @Column(nullable = false, length = 50)
    private String numero;

    @Column(nullable = false, length = 100)
    private String banco;

    @Column(length = 100)
    private String librador;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "fecha_cobro", nullable = false)
    private LocalDate fechaCobro;

    @Column(nullable = false, length = 15)
    private String estado = "PENDIENTE";

    @Column(name = "cliente_id", columnDefinition = "uuid")
    private UUID clienteId;

    @Column(name = "proveedor_id", columnDefinition = "uuid")
    private UUID proveedorId;

    @Column(columnDefinition = "text")
    private String observaciones;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected ChequeEntity() {}

    public ChequeEntity(UUID id, String tipo, String numero, String banco, String librador,
                         BigDecimal monto, LocalDate fechaEmision, LocalDate fechaCobro,
                         UUID clienteId, UUID proveedorId, String observaciones) {
        this.id          = id;
        this.tipo        = tipo;
        this.numero      = numero;
        this.banco       = banco;
        this.librador    = librador;
        this.monto       = monto;
        this.fechaEmision = fechaEmision;
        this.fechaCobro  = fechaCobro;
        this.clienteId   = clienteId;
        this.proveedorId = proveedorId;
        this.observaciones = observaciones;
    }

    public UUID getId() { return id; }
    public String getTipo() { return tipo; }
    public String getNumero() { return numero; }
    public String getBanco() { return banco; }
    public String getLibrador() { return librador; }
    public BigDecimal getMonto() { return monto; }
    public LocalDate getFechaEmision() { return fechaEmision; }
    public LocalDate getFechaCobro() { return fechaCobro; }
    public String getEstado() { return estado; }
    public UUID getClienteId() { return clienteId; }
    public UUID getProveedorId() { return proveedorId; }
    public String getObservaciones() { return observaciones; }

    public void setEstado(String estado) { this.estado = estado; }
    public void setObservaciones(String obs) { this.observaciones = obs; }
}
