package com.mmotos.infrastructure.output.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "proveedores")
public class ProveedorEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(unique = true, length = 13)
    private String cuit;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 100)
    private String contacto;

    @Column(length = 30)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected ProveedorEntity() {}

    public ProveedorEntity(UUID id, String cuit, String nombre, String contacto,
                           String telefono, String email) {
        this.id       = id;
        this.cuit     = cuit;
        this.nombre   = nombre;
        this.contacto = contacto;
        this.telefono = telefono;
        this.email    = email;
    }

    public UUID getId() { return id; }
    public String getCuit() { return cuit; }
    public String getNombre() { return nombre; }
    public String getContacto() { return contacto; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public boolean isActivo() { return activo; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCuit(String cuit) { this.cuit = cuit; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setContacto(String contacto) { this.contacto = contacto; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setEmail(String email) { this.email = email; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
