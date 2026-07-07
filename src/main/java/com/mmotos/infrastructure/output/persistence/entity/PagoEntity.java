package com.mmotos.infrastructure.output.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "pagos")
public class PagoEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "venta_id", nullable = false, columnDefinition = "uuid")
    private UUID ventaId;

    @Column(nullable = false, length = 20)
    private String metodo;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Type(JsonType.class)
    @Column(name = "detalles_pago", columnDefinition = "jsonb")
    private JsonNode detallesPago;   // {"nro_cupon":"1234","cuotas":3}

    protected PagoEntity() {}

    public PagoEntity(UUID id, UUID ventaId, String metodo, BigDecimal monto, JsonNode detallesPago) {
        this.id = id;
        this.ventaId = ventaId;
        this.metodo = metodo;
        this.monto = monto;
        this.detallesPago = detallesPago;
    }

    public UUID getId() { return id; }
    public UUID getVentaId() { return ventaId; }
    public String getMetodo() { return metodo; }
    public BigDecimal getMonto() { return monto; }
    public JsonNode getDetallesPago() { return detallesPago; }
}
