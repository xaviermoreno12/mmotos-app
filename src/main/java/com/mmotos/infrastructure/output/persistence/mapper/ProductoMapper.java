package com.mmotos.infrastructure.output.persistence.mapper;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.mmotos.domain.model.Moneda;
import com.mmotos.domain.model.Precio;
import com.mmotos.domain.model.RepuestoSimple;
import com.mmotos.infrastructure.output.persistence.entity.ProductoEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ProductoMapper {

    public RepuestoSimple toDomain(ProductoEntity entity) {
        Precio precio = new Precio(
            entity.getPrecioBase(),
            Moneda.valueOf(entity.getMoneda()),
            entity.getUltimaActualizacion()
        );
        return new RepuestoSimple(
            entity.getId(),
            entity.getSku(),
            entity.getNombre(),
            precio,
            entity.getStockActual(),
            entity.getStockMinimo(),
            entity.getUbicacionFisica()
        );
    }

    public ProductoEntity toEntity(RepuestoSimple domain) {
        return new ProductoEntity(
            domain.getId(),
            domain.getSku(),
            domain.getNombre(),
            domain.getPrecio().valorBase(),
            domain.getPrecio().moneda().name(),
            domain.getStockActual(),
            domain.getStockMinimo(),
            JsonNodeFactory.instance.objectNode(),
            domain.getUbicacionFisica(),
            false
        );
    }

    // Sincroniza el stock desde el dominio hacia la entity existente (evita reemplazar la entity)
    public void syncStock(ProductoEntity entity, int nuevoStock) {
        entity.setStockActual(nuevoStock);
        entity.setUltimaActualizacion(LocalDateTime.now());
    }
}
