package com.mmotos.infrastructure.output.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "metodos_pago_config")
public class MetodoPagoConfigEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(unique = true, nullable = false, length = 30)
    private String codigo;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(name = "acepta_cobro", nullable = false)
    private boolean aceptaCobro;

    @Column(name = "acepta_pago", nullable = false)
    private boolean aceptaPago;

    @Column(nullable = false)
    private boolean habilitado;

    @Column(nullable = false)
    private int orden;

    protected MetodoPagoConfigEntity() {}

    public MetodoPagoConfigEntity(UUID id, String codigo, String nombre,
                                   boolean aceptaCobro, boolean aceptaPago,
                                   boolean habilitado, int orden) {
        this.id = id; this.codigo = codigo; this.nombre = nombre;
        this.aceptaCobro = aceptaCobro; this.aceptaPago = aceptaPago;
        this.habilitado = habilitado; this.orden = orden;
    }

    public UUID getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public boolean isAceptaCobro() { return aceptaCobro; }
    public boolean isAceptaPago() { return aceptaPago; }
    public boolean isHabilitado() { return habilitado; }
    public int getOrden() { return orden; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setAceptaCobro(boolean aceptaCobro) { this.aceptaCobro = aceptaCobro; }
    public void setAceptaPago(boolean aceptaPago) { this.aceptaPago = aceptaPago; }
    public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }
}
