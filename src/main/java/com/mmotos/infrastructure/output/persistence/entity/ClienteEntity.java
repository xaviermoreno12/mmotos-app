package com.mmotos.infrastructure.output.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "clientes")
public class ClienteEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(unique = true, length = 13)
    private String cuit;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 200)
    private String direccion;

    @Column(length = 30)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected ClienteEntity() {}

    public ClienteEntity(UUID id, String cuit, String nombre, String direccion,
                         String telefono, String email) {
        this.id        = id;
        this.cuit      = cuit;
        this.nombre    = nombre;
        this.direccion = direccion;
        this.telefono  = telefono;
        this.email     = email;
    }

    public UUID getId() { return id; }
    public String getCuit() { return cuit; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public BigDecimal getSaldo() { return saldo; }
    public boolean isActivo() { return activo; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCuit(String cuit) { this.cuit = cuit; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setEmail(String email) { this.email = email; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public void setSaldo(java.math.BigDecimal saldo) { this.saldo = saldo; }
}
