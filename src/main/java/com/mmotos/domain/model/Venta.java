package com.mmotos.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Venta {

    private UUID id;
    private LocalDateTime fechaEmision;
    private TipoFactura tipoFactura;
    private String cuitCliente;
    private EstadoFiscal estadoFiscal;
    private SyncStatus syncStatus;
    private String cae;
    private String numeroTicket;
    private UUID usuarioId;

    private final List<LineaVenta> lineas = new ArrayList<>();
    private final List<Pago> pagos = new ArrayList<>();

    public Venta() {
        this.id = UUID.randomUUID();
        this.fechaEmision = LocalDateTime.now();
        this.estadoFiscal = EstadoFiscal.PENDIENTE;
        this.syncStatus = SyncStatus.LOCAL;
        this.tipoFactura = TipoFactura.B;
    }

    public BigDecimal totalLineas() {
        return lineas.stream()
            .map(LineaVenta::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalPagos() {
        return pagos.stream()
            .map(Pago::monto)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean estaBalanceada() {
        return totalLineas().subtract(totalPagos()).abs().compareTo(new BigDecimal("0.01")) <= 0;
    }

    public void confirmarDatosFiscales(String cae, String numeroTicket) {
        this.cae = cae;
        this.numeroTicket = numeroTicket;
        this.estadoFiscal = EstadoFiscal.APROBADO;
        this.syncStatus = SyncStatus.SYNCED;
    }

    public void marcarErrorHardware() {
        this.estadoFiscal = EstadoFiscal.ERROR_HARDWARE;
        this.syncStatus = SyncStatus.FAILED;
    }

    public void marcarContingencia(String numeroTicket) {
        this.numeroTicket = numeroTicket;
        this.estadoFiscal = EstadoFiscal.CONTINGENCIA;
        this.syncStatus = SyncStatus.PENDING;
    }

    public void agregarLinea(LineaVenta linea) { lineas.add(linea); }
    public void agregarPago(Pago pago) { pagos.add(pago); }

    public List<LineaVenta> getLineas() { return Collections.unmodifiableList(lineas); }
    public List<Pago> getPagos() { return Collections.unmodifiableList(pagos); }

    public UUID getId() { return id; }
    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public TipoFactura getTipoFactura() { return tipoFactura; }
    public String getCuitCliente() { return cuitCliente; }
    public EstadoFiscal getEstadoFiscal() { return estadoFiscal; }
    public SyncStatus getSyncStatus() { return syncStatus; }
    public String getCae() { return cae; }
    public String getNumeroTicket() { return numeroTicket; }
    public UUID getUsuarioId() { return usuarioId; }

    public void setTipoFactura(TipoFactura tipoFactura) { this.tipoFactura = tipoFactura; }
    public void setCuitCliente(String cuitCliente) { this.cuitCliente = cuitCliente; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }
    // Solo para reconstitución desde persistencia — no usar para crear nuevas ventas
    public void setId(UUID id) { this.id = id; }
}
