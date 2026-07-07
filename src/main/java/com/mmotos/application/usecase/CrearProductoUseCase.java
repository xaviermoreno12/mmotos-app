package com.mmotos.application.usecase;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.mmotos.application.dto.CrearProductoRequest;
import com.mmotos.application.dto.ProductoDTO;
import com.mmotos.domain.port.ConfiguracionRepository;
import com.mmotos.infrastructure.output.persistence.entity.ProductoEntity;
import com.mmotos.infrastructure.output.persistence.jpa.ProductoJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class CrearProductoUseCase {

    private final ProductoJpaRepository repo;
    private final ConfiguracionRepository configuracionRepository;

    public CrearProductoUseCase(ProductoJpaRepository repo,
                                ConfiguracionRepository configuracionRepository) {
        this.repo = repo;
        this.configuracionRepository = configuracionRepository;
    }

    @Transactional
    public ProductoDTO crear(CrearProductoRequest req) {
        if (repo.findBySkuAndActivoTrue(req.sku()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con SKU: " + req.sku());
        }

        ProductoEntity entity = new ProductoEntity(
            UUID.randomUUID(),
            req.sku().toUpperCase().trim(),
            req.nombre().trim(),
            req.precioBase(),
            req.moneda(),
            req.stockActual(),
            req.stockMinimo(),
            JsonNodeFactory.instance.objectNode(),
            req.ubicacionFisica() != null ? req.ubicacionFisica().trim() : null,
            false
        );
        if (req.precioCompra() != null) entity.setPrecioCompra(req.precioCompra());
        repo.save(entity);

        BigDecimal cotizacion = configuracionRepository.getCotizacionDolar().orElse(BigDecimal.ONE);
        BigDecimal precioEnPesos = "USD".equals(req.moneda())
            ? req.precioBase().multiply(cotizacion).setScale(2, RoundingMode.HALF_UP)
            : req.precioBase();

        return new ProductoDTO(
            entity.getId(), entity.getSku(), entity.getNombre(),
            entity.getPrecioBase(), entity.getMoneda(), precioEnPesos,
            entity.getStockActual(), entity.getStockMinimo(),
            entity.getStockActual() <= entity.getStockMinimo(),
            entity.getUbicacionFisica(), entity.isEsKit(), entity.isActivo(),
            entity.getPrecioCompra()
        );
    }
}
