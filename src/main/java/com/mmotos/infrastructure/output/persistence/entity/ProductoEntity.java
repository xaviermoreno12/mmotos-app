package com.mmotos.infrastructure.output.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "productos")
public class ProductoEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(unique = true, nullable = false, length = 50)
    private String sku;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(columnDefinition = "text")
    private String descripcion;

    @Column(name = "precio_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda;

    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    // JSONB: atributos dinámicos como {"viscosidad":"20W50","litros":1}
    @Type(JsonType.class)
    @Column(name = "atributos_extra", columnDefinition = "jsonb")
    private JsonNode atributosExtra;

    @Column(name = "ubicacion_fisica", length = 100)
    private String ubicacionFisica;

    @Column(name = "precio_compra", precision = 12, scale = 2)
    private BigDecimal precioCompra;

    @Column(name = "es_kit", nullable = false)
    private boolean esKit = false;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "ultima_actualizacion", nullable = false)
    private LocalDateTime ultimaActualizacion = LocalDateTime.now();

    protected ProductoEntity() {}

    public ProductoEntity(UUID id, String sku, String nombre, BigDecimal precioBase, String moneda,
                          int stockActual, int stockMinimo, JsonNode atributosExtra,
                          String ubicacionFisica, boolean esKit) {
        this.id = id;
        this.sku = sku;
        this.nombre = nombre;
        this.precioBase = precioBase;
        this.moneda = moneda;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.atributosExtra = atributosExtra;
        this.ubicacionFisica = ubicacionFisica;
        this.esKit = esKit;
    }

    public UUID getId() { return id; }
    public String getSku() { return sku; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecioBase() { return precioBase; }
    public String getMoneda() { return moneda; }
    public Integer getStockActual() { return stockActual; }
    public Integer getStockMinimo() { return stockMinimo; }
    public JsonNode getAtributosExtra() { return atributosExtra; }
    public String getUbicacionFisica() { return ubicacionFisica; }
    public BigDecimal getPrecioCompra() { return precioCompra; }
    public boolean isEsKit() { return esKit; }
    public boolean isActivo() { return activo; }
    public LocalDateTime getUltimaActualizacion() { return ultimaActualizacion; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setStockActual(Integer stockActual) { this.stockActual = stockActual; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }
    public void setPrecioBase(BigDecimal precioBase) { this.precioBase = precioBase; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public void setUbicacionFisica(String ubicacionFisica) { this.ubicacionFisica = ubicacionFisica; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public void setPrecioCompra(BigDecimal precioCompra) { this.precioCompra = precioCompra; }
    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) { this.ultimaActualizacion = ultimaActualizacion; }
}
