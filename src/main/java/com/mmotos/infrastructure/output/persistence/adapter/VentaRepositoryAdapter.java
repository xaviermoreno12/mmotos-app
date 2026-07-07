package com.mmotos.infrastructure.output.persistence.adapter;

import com.mmotos.domain.model.SyncStatus;
import com.mmotos.domain.model.Venta;
import com.mmotos.domain.port.VentaRepository;
import com.mmotos.infrastructure.output.persistence.entity.VentaEntity;
import com.mmotos.infrastructure.output.persistence.jpa.PagoJpaRepository;
import com.mmotos.infrastructure.output.persistence.jpa.VentaDetalleJpaRepository;
import com.mmotos.infrastructure.output.persistence.jpa.VentaJpaRepository;
import com.mmotos.infrastructure.output.persistence.mapper.VentaMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class VentaRepositoryAdapter implements VentaRepository {

    private final VentaJpaRepository ventaRepo;
    private final VentaDetalleJpaRepository detalleRepo;
    private final PagoJpaRepository pagoRepo;
    private final VentaMapper mapper;

    public VentaRepositoryAdapter(VentaJpaRepository ventaRepo,
                                   VentaDetalleJpaRepository detalleRepo,
                                   PagoJpaRepository pagoRepo,
                                   VentaMapper mapper) {
        this.ventaRepo  = ventaRepo;
        this.detalleRepo = detalleRepo;
        this.pagoRepo   = pagoRepo;
        this.mapper     = mapper;
    }

    @Override
    @Transactional
    public Venta save(Venta venta) {
        ventaRepo.save(mapper.toVentaEntity(venta));
        detalleRepo.saveAll(mapper.toDetalleEntities(venta));
        pagoRepo.saveAll(mapper.toPagoEntities(venta));
        return venta;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Venta> findById(UUID id) {
        return ventaRepo.findById(id).map(this::reconstituirVenta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> findBySyncStatus(SyncStatus status) {
        return ventaRepo.findBySyncStatusIn(List.of(status.name()))
            .stream()
            .map(this::reconstituirVenta)
            .toList();
    }

    // Reconstruye el objeto Venta con sus datos fiscales desde la entity.
    // Solo necesario para el SyncService; no reconstruye lineas/pagos (no son necesarios en ese flujo).
    private Venta reconstituirVenta(VentaEntity entity) {
        Venta v = new Venta();
        v.setId(entity.getId());
        v.setTipoFactura(com.mmotos.domain.model.TipoFactura.valueOf(entity.getTipoFactura()));
        v.setCuitCliente(entity.getClienteCuit());
        v.setUsuarioId(entity.getUsuarioId());
        if (entity.getCae() != null) {
            v.confirmarDatosFiscales(entity.getCae(), entity.getNumeroTicket());
        }
        return v;
    }
}
