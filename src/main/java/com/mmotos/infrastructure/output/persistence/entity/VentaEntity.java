package com.mmotos.infrastructure.output.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ventas")
public class VentaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "numero_ticket", length = 30, unique = true)
    private String numeroTicket;

    @Column(name = "tipo_factura", nullable = false, length = 10)
    private String tipoFactura;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @Column(name = "cliente_cuit", length = 11)
    private String clienteCuit;

    @Column(name = "total_venta", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalVenta;

    @Column(name = "estado_fiscal", nullable = false, length = 20)
    private String estadoFiscal;

    @Column(name = "cae", length = 14)
    private String cae;

    @Column(name = "sync_status", nullable = false, length = 15)
    private String syncStatus;

    @Column(name = "usuario_id", columnDefinition = "uuid")
    private UUID usuarioId;

    @Column(name = "caja_id", columnDefinition = "uuid")
    private UUID cajaId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "anulada", nullable = false)
    private boolean anulada = false;

    @Column(name = "fecha_anulacion")
    private LocalDateTime fechaAnulacion;

    @Column(name = "motivo_anulacion", length = 255)
    private String motivoAnulacion;

    @Column(name = "modificada", nullable = false)
    private boolean modificada = false;

    @Column(name = "cantidad_modificaciones", nullable = false)
    private int cantidadModificaciones = 0;

    // Sin @OneToMany: los detalles y pagos se persisten en el adapter por separado
    // dentro de la misma transacción. Evita el mappedBy sobre UUID plano (bug JPA).

    protected VentaEntity() {}

    public VentaEntity(UUID id, String tipoFactura, LocalDateTime fechaEmision,
                       String clienteCuit, BigDecimal totalVenta, String estadoFiscal,
                       String syncStatus, UUID usuarioId) {
        this.id = id;
        this.tipoFactura = tipoFactura;
        this.fechaEmision = fechaEmision;
        this.clienteCuit = clienteCuit;
        this.totalVenta = totalVenta;
        this.estadoFiscal = estadoFiscal;
        this.syncStatus = syncStatus;
        this.usuarioId = usuarioId;
    }

    public UUID getId() { return id; }
    public String getNumeroTicket() { return numeroTicket; }
    public String getTipoFactura() { return tipoFactura; }
    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public String getClienteCuit() { return clienteCuit; }
    public BigDecimal getTotalVenta() { return totalVenta; }
    public String getEstadoFiscal() { return estadoFiscal; }
    public String getCae() { return cae; }
    public String getSyncStatus() { return syncStatus; }
    public UUID getUsuarioId() { return usuarioId; }
    public UUID getCajaId() { return cajaId; }

    public boolean isAnulada() { return anulada; }
    public LocalDateTime getFechaAnulacion() { return fechaAnulacion; }
    public String getMotivoAnulacion() { return motivoAnulacion; }

    public void setCae(String cae) { this.cae = cae; }
    public void setNumeroTicket(String numeroTicket) { this.numeroTicket = numeroTicket; }
    public void setEstadoFiscal(String estadoFiscal) { this.estadoFiscal = estadoFiscal; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
    public void setCajaId(UUID cajaId) { this.cajaId = cajaId; }
    public void setTotalVenta(BigDecimal totalVenta) { this.totalVenta = totalVenta; }
    public void setAnulada(boolean anulada) { this.anulada = anulada; }
    public void setFechaAnulacion(LocalDateTime fechaAnulacion) { this.fechaAnulacion = fechaAnulacion; }
    public void setMotivoAnulacion(String motivoAnulacion) { this.motivoAnulacion = motivoAnulacion; }

    public boolean isModificada() { return modificada; }
    public void setModificada(boolean modificada) { this.modificada = modificada; }
    public int getCantidadModificaciones() { return cantidadModificaciones; }
    public void setCantidadModificaciones(int cantidadModificaciones) { this.cantidadModificaciones = cantidadModificaciones; }
}
