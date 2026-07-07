package com.mmotos.application.usecase;

import com.mmotos.application.dto.*;
import com.mmotos.domain.port.NotificationPort;
import com.mmotos.infrastructure.output.persistence.entity.VentaDetalleEntity;
import com.mmotos.infrastructure.output.persistence.entity.VentaEntity;
import com.mmotos.infrastructure.output.persistence.entity.VentaModificacionEntity;
import com.mmotos.infrastructure.output.persistence.jpa.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ModificarVentaUseCase {

    private final VentaJpaRepository ventaRepo;
    private final VentaDetalleJpaRepository detalleRepo;
    private final PagoJpaRepository pagoRepo;
    private final VentaModificacionJpaRepository modRepo;
    private final UsuarioJpaRepository usuarioRepo;
    private final ProductoJpaRepository productoRepo;
    private final ConsultarVentasUseCase consultarVentasUseCase;
    private final NotificationPort notificationPort;

    public ModificarVentaUseCase(VentaJpaRepository ventaRepo,
                                  VentaDetalleJpaRepository detalleRepo,
                                  PagoJpaRepository pagoRepo,
                                  VentaModificacionJpaRepository modRepo,
                                  UsuarioJpaRepository usuarioRepo,
                                  ProductoJpaRepository productoRepo,
                                  ConsultarVentasUseCase consultarVentasUseCase,
                                  NotificationPort notificationPort) {
        this.ventaRepo = ventaRepo;
        this.detalleRepo = detalleRepo;
        this.pagoRepo = pagoRepo;
        this.modRepo = modRepo;
        this.usuarioRepo = usuarioRepo;
        this.productoRepo = productoRepo;
        this.consultarVentasUseCase = consultarVentasUseCase;
        this.notificationPort = notificationPort;
    }

    @Transactional
    public VentaDetalleCompletoDTO ejecutar(UUID ventaId, ModificarVentaRequest request, UUID usuarioId) {
        VentaEntity venta = ventaRepo.findById(ventaId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta no encontrada"));

        if (venta.isAnulada()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede modificar una venta anulada");
        }

        String usuarioNombre = usuarioId != null
            ? usuarioRepo.findById(usuarioId).map(u -> u.getNombre()).orElse("Sistema")
            : "Sistema";

        List<VentaDetalleEntity> detallesActuales = detalleRepo.findByVentaId(ventaId);
        Map<UUID, VentaDetalleEntity> detalleMap = new HashMap<>();
        for (VentaDetalleEntity d : detallesActuales) {
            detalleMap.put(d.getId(), d);
        }

        LocalDateTime ahora = LocalDateTime.now();
        List<VentaModificacionEntity> modificaciones = new ArrayList<>();

        for (ModificarVentaRequest.LineaModificada linea : request.lineas()) {
            VentaDetalleEntity detalle = detalleMap.get(linea.detalleId());
            if (detalle == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Detalle no encontrado: " + linea.detalleId());
            }

            boolean cambio = false;

            // Verificar cambio de cantidad
            if (!detalle.getCantidad().equals(linea.cantidad())) {
                int cantidadAnterior = detalle.getCantidad();
                int cantidadNueva = linea.cantidad();

                modificaciones.add(new VentaModificacionEntity(
                    UUID.randomUUID(), ventaId, usuarioId, usuarioNombre, ahora,
                    "CANTIDAD", detalle.getId(),
                    detalle.getNombreHistorico() + ": " + cantidadAnterior + " un.",
                    detalle.getNombreHistorico() + ": " + cantidadNueva + " un.",
                    request.motivo()
                ));

                // Ajustar stock: devolver la cantidad anterior y descontar la nueva
                if (detalle.getProductoId() != null) {
                    productoRepo.findById(detalle.getProductoId()).ifPresent(prod -> {
                        int ajuste = cantidadAnterior - cantidadNueva;
                        prod.setStockActual(prod.getStockActual() + ajuste);
                        productoRepo.save(prod);

                        if (prod.getStockActual() <= prod.getStockMinimo()) {
                            notificationPort.enviarAlertaStockBajo(
                                prod.getId(), prod.getSku(), prod.getNombre(), prod.getStockActual());
                        }
                    });
                }

                detalle.setCantidad(cantidadNueva);
                cambio = true;
            }

            // Verificar cambio de precio
            if (detalle.getPrecioUnitarioHistorico().compareTo(linea.precioUnitario()) != 0) {
                modificaciones.add(new VentaModificacionEntity(
                    UUID.randomUUID(), ventaId, usuarioId, usuarioNombre, ahora,
                    "PRECIO", detalle.getId(),
                    detalle.getNombreHistorico() + ": $" + detalle.getPrecioUnitarioHistorico(),
                    detalle.getNombreHistorico() + ": $" + linea.precioUnitario(),
                    request.motivo()
                ));
                detalle.setPrecioUnitarioHistorico(linea.precioUnitario());
                cambio = true;
            }

            if (cambio) {
                detalleRepo.save(detalle);
            }
        }

        if (modificaciones.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se detectaron cambios en la venta");
        }

        // Recalcular total
        List<VentaDetalleEntity> detallesActualizados = detalleRepo.findByVentaId(ventaId);
        BigDecimal nuevoTotal = BigDecimal.ZERO;
        for (VentaDetalleEntity d : detallesActualizados) {
            nuevoTotal = nuevoTotal.add(
                d.getPrecioUnitarioHistorico().multiply(BigDecimal.valueOf(d.getCantidad()))
            );
        }

        BigDecimal totalAnterior = venta.getTotalVenta();
        if (totalAnterior.compareTo(nuevoTotal) != 0) {
            modificaciones.add(new VentaModificacionEntity(
                UUID.randomUUID(), ventaId, usuarioId, usuarioNombre, ahora,
                "TOTAL", null,
                "Total: $" + totalAnterior,
                "Total: $" + nuevoTotal,
                request.motivo()
            ));
            venta.setTotalVenta(nuevoTotal);
        }

        venta.setModificada(true);
        venta.setCantidadModificaciones(venta.getCantidadModificaciones() + modificaciones.size());
        ventaRepo.save(venta);

        modRepo.saveAll(modificaciones);

        return consultarVentasUseCase.porId(ventaId);
    }
}
