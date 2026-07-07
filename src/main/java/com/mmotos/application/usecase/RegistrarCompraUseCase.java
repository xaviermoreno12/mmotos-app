package com.mmotos.application.usecase;

import com.mmotos.application.dto.*;
import com.mmotos.infrastructure.output.persistence.entity.CompraDetalleEntity;
import com.mmotos.infrastructure.output.persistence.entity.CompraEntity;
import com.mmotos.infrastructure.output.persistence.jpa.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class RegistrarCompraUseCase {

    private final CompraJpaRepository compraRepo;
    private final CompraDetalleJpaRepository detalleRepo;
    private final ProductoJpaRepository productoRepo;

    public RegistrarCompraUseCase(CompraJpaRepository compraRepo,
                                   CompraDetalleJpaRepository detalleRepo,
                                   ProductoJpaRepository productoRepo) {
        this.compraRepo  = compraRepo;
        this.detalleRepo = detalleRepo;
        this.productoRepo = productoRepo;
    }

    @Transactional(readOnly = true)
    public List<CompraDTO> listar() {
        return compraRepo.findAllByOrderByFechaDesc().stream()
            .map(c -> toDTO(c, List.of()))
            .toList();
    }

    @Transactional
    public CompraDTO registrar(CrearCompraRequest req) {
        // Calcular total
        BigDecimal total = req.lineas().stream()
            .map(l -> l.precioUnitario().multiply(BigDecimal.valueOf(l.cantidad())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Determinar nombre del proveedor
        String proveedorNombre = req.proveedorNombre() != null ? req.proveedorNombre() : "Sin proveedor";

        // Guardar compra
        UUID compraId = UUID.randomUUID();
        UUID proveedorId = req.proveedorId();
        UUID usuarioId = req.usuarioId() != null ? UUID.fromString(req.usuarioId()) : null;

        var compra = new CompraEntity(compraId, proveedorId, proveedorNombre,
                                      req.numeroRemito(), total, req.metodoPago(),
                                      usuarioId, req.observaciones());
        compraRepo.save(compra);

        // Guardar detalles y aumentar stock
        var detalles = req.lineas().stream().map(linea -> {
            // Aumentar stock del producto
            productoRepo.findById(linea.productoId()).ifPresentOrElse(producto -> {
                producto.setStockActual(producto.getStockActual() + linea.cantidad());
                productoRepo.save(producto);
            }, () -> {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Producto no encontrado: " + linea.productoId());
            });

            return new CompraDetalleEntity(
                UUID.randomUUID(), compraId, linea.productoId(),
                linea.skuHistorico(), linea.nombreHistorico(),
                linea.cantidad(), linea.precioUnitario()
            );
        }).toList();

        detalleRepo.saveAll(detalles);

        var detalleDTOs = detalles.stream()
            .map(d -> new CompraDetalleDTO(
                d.getProductoId() != null ? d.getProductoId().toString() : null,
                d.getSkuHistorico(), d.getNombreHistorico(),
                d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal()))
            .toList();

        return toDTO(compra, detalleDTOs);
    }

    private CompraDTO toDTO(CompraEntity c, List<CompraDetalleDTO> detalle) {
        return new CompraDTO(
            c.getId().toString(),
            c.getProveedorId() != null ? c.getProveedorId().toString() : null,
            c.getProveedorNombre(),
            c.getNumeroRemito(),
            c.getFecha().toString(),
            c.getTotal(),
            c.getMetodoPago(),
            c.getEstado(),
            c.getObservaciones(),
            detalle
        );
    }
}
