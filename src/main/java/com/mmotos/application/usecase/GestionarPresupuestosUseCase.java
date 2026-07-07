package com.mmotos.application.usecase;

import com.mmotos.application.dto.*;
import com.mmotos.infrastructure.output.persistence.entity.PresupuestoDetalleEntity;
import com.mmotos.infrastructure.output.persistence.entity.PresupuestoEntity;
import com.mmotos.infrastructure.output.persistence.jpa.PresupuestoDetalleJpaRepository;
import com.mmotos.infrastructure.output.persistence.jpa.PresupuestoJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class GestionarPresupuestosUseCase {

    private final PresupuestoJpaRepository repo;
    private final PresupuestoDetalleJpaRepository detalleRepo;

    public GestionarPresupuestosUseCase(PresupuestoJpaRepository repo,
                                         PresupuestoDetalleJpaRepository detalleRepo) {
        this.repo       = repo;
        this.detalleRepo = detalleRepo;
    }

    @Transactional(readOnly = true)
    public List<PresupuestoDTO> listar() {
        return repo.findAllByOrderByFechaDesc().stream()
            .map(p -> toDTO(p, List.of())).toList();
    }

    @Transactional(readOnly = true)
    public PresupuestoDTO obtener(UUID id) {
        var p = repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var detalle = detalleRepo.findByPresupuestoId(id).stream()
            .map(d -> new CompraDetalleDTO(
                d.getProductoId() != null ? d.getProductoId().toString() : null,
                d.getSkuHistorico(), d.getNombreHistorico(),
                d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal()))
            .toList();
        return toDTO(p, detalle);
    }

    @Transactional
    public PresupuestoDTO crear(CrearPresupuestoRequest req) {
        BigDecimal total = req.lineas().stream()
            .map(l -> l.precioUnitario().multiply(BigDecimal.valueOf(l.cantidad())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        String clienteNombre = req.clienteNombre() != null ? req.clienteNombre() : "Sin cliente";
        UUID usuarioId = req.usuarioId() != null ? UUID.fromString(req.usuarioId()) : null;

        UUID presupId = UUID.randomUUID();
        var presup = new PresupuestoEntity(presupId, req.clienteId(), clienteNombre,
            req.fechaValidez().atStartOfDay(),
            total, req.observaciones(), usuarioId);
        repo.save(presup);

        var detalles = req.lineas().stream().map(l ->
            new PresupuestoDetalleEntity(UUID.randomUUID(), presupId, l.productoId(),
                l.skuHistorico(), l.nombreHistorico(), l.cantidad(), l.precioUnitario())
        ).toList();
        detalleRepo.saveAll(detalles);

        return obtener(presupId);
    }

    @Transactional
    public PresupuestoDTO cambiarEstado(UUID id, String estado) {
        var p = repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if ("APROBADO".equals(p.getEstado()) || "RECHAZADO".equals(p.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "El presupuesto ya fue " + p.getEstado().toLowerCase());
        }
        p.setEstado(estado);
        return toDTO(repo.save(p), List.of());
    }

    private PresupuestoDTO toDTO(PresupuestoEntity p, List<CompraDetalleDTO> detalle) {
        return new PresupuestoDTO(
            p.getId().toString(),
            p.getClienteId() != null ? p.getClienteId().toString() : null,
            p.getClienteNombre(),
            p.getFecha().toString(),
            p.getFechaValidez().toString(),
            p.getTotal(),
            p.getEstado(),
            p.getObservaciones(),
            detalle
        );
    }
}
