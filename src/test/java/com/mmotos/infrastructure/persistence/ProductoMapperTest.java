package com.mmotos.infrastructure.persistence;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.mmotos.domain.model.Moneda;
import com.mmotos.domain.model.RepuestoSimple;
import com.mmotos.infrastructure.output.persistence.entity.ProductoEntity;
import com.mmotos.infrastructure.output.persistence.mapper.ProductoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ProductoMapperTest {

    private final ProductoMapper mapper = new ProductoMapper();

    private ProductoEntity entityBase() {
        return new ProductoEntity(
            UUID.randomUUID(), "FLT-001", "Filtro de Aceite",
            new BigDecimal("500.00"), "ARS",
            10, 2, JsonNodeFactory.instance.objectNode(),
            "A-4", false
        );
    }

    @Test
    @DisplayName("toDomain mapea campos básicos correctamente")
    void toDomainMapaCampos() {
        ProductoEntity entity = entityBase();
        RepuestoSimple domain = mapper.toDomain(entity);

        assertThat(domain.getId()).isEqualTo(entity.getId());
        assertThat(domain.getSku()).isEqualTo("FLT-001");
        assertThat(domain.getNombre()).isEqualTo("Filtro de Aceite");
        assertThat(domain.getStockActual()).isEqualTo(10);
        assertThat(domain.getStockMinimo()).isEqualTo(2);
        assertThat(domain.getUbicacionFisica()).isEqualTo("A-4");
    }

    @Test
    @DisplayName("toDomain mapea Precio con moneda ARS correctamente")
    void toDomainMapaPrecio() {
        ProductoEntity entity = entityBase();
        RepuestoSimple domain = mapper.toDomain(entity);

        assertThat(domain.getPrecio().valorBase()).isEqualByComparingTo("500.00");
        assertThat(domain.getPrecio().moneda()).isEqualTo(Moneda.ARS);
    }

    @Test
    @DisplayName("toEntity mapea campos de vuelta correctamente")
    void toEntityMapaCampos() {
        ProductoEntity entity    = entityBase();
        RepuestoSimple domain    = mapper.toDomain(entity);
        ProductoEntity resultado = mapper.toEntity(domain);

        assertThat(resultado.getId()).isEqualTo(entity.getId());
        assertThat(resultado.getSku()).isEqualTo(entity.getSku());
        assertThat(resultado.getPrecioBase()).isEqualByComparingTo(entity.getPrecioBase());
        assertThat(resultado.getMoneda()).isEqualTo(entity.getMoneda());
    }

    @Test
    @DisplayName("syncStock actualiza stockActual y ultimaActualizacion")
    void syncStockActualizaStock() {
        ProductoEntity entity = entityBase();
        mapper.syncStock(entity, 7);

        assertThat(entity.getStockActual()).isEqualTo(7);
        assertThat(entity.getUbicacionFisica()).isEqualTo("A-4");
    }
}
