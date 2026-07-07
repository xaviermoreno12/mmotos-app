package com.mmotos.application.usecase;

import com.mmotos.application.dto.ActualizarProductoRequest;
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
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ActualizarProductoUseCase {

    private final ProductoJpaRepository repo;
    private final ConfiguracionRepository configuracionRepository;

    public ActualizarProductoUseCase(ProductoJpaRepository repo,
                                     ConfiguracionRepository configuracionRepository) {
        this.repo = repo;
        this.configuracionRepository = configuracionRepository;
    }

    @Transactional
    public ProductoDTO actualizar(UUID id, ActualizarProductoRequest req) {
        ProductoEntity p = repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        if (req.nombre() != null && !req.nombre().isBlank()) p.setNombre(req.nombre().trim());
        if (req.precioBase() != null) p.setPrecioBase(req.precioBase());
        if (req.moneda() != null) p.setMoneda(req.moneda());
        if (req.stockActual() != null) p.setStockActual(req.stockActual());
        if (req.stockMinimo() != null) p.setStockMinimo(req.stockMinimo());
        if (req.ubicacionFisica() != null) p.setUbicacionFisica(req.ubicacionFisica().trim());
        if (req.activo() != null) p.setActivo(req.activo());
        if (req.precioCompra() != null) p.setPrecioCompra(req.precioCompra());

        p.setUltimaActualizacion(LocalDateTime.now());
        repo.save(p);

        BigDecimal cotizacion = configuracionRepository.getCotizacionDolar().orElse(BigDecimal.ONE);
        BigDecimal precioEnPesos = "USD".equals(p.getMoneda())
            ? p.getPrecioBase().multiply(cotizacion).setScale(2, RoundingMode.HALF_UP)
            : p.getPrecioBase();

        return new ProductoDTO(
            p.getId(), p.getSku(), p.getNombre(),
            p.getPrecioBase(), p.getMoneda(), precioEnPesos,
            p.getStockActual(), p.getStockMinimo(),
            p.getStockActual() <= p.getStockMinimo(),
            p.getUbicacionFisica(), p.isEsKit(), p.isActivo(),
            p.getPrecioCompra()
        );
    }
}
