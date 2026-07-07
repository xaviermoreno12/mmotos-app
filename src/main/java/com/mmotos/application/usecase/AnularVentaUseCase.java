package com.mmotos.application.usecase;

import com.mmotos.domain.port.NotificationPort;
import com.mmotos.infrastructure.output.persistence.jpa.ProductoJpaRepository;
import com.mmotos.infrastructure.output.persistence.jpa.UsuarioJpaRepository;
import com.mmotos.infrastructure.output.persistence.jpa.VentaDetalleJpaRepository;
import com.mmotos.infrastructure.output.persistence.jpa.VentaJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AnularVentaUseCase {

    private static final Logger log = LoggerFactory.getLogger(AnularVentaUseCase.class);

    private final VentaJpaRepository ventaRepo;
    private final VentaDetalleJpaRepository detalleRepo;
    private final ProductoJpaRepository productoRepo;
    private final UsuarioJpaRepository usuarioRepo;
    private final NotificationPort notificationPort;

    public AnularVentaUseCase(VentaJpaRepository ventaRepo,
                               VentaDetalleJpaRepository detalleRepo,
                               ProductoJpaRepository productoRepo,
                               UsuarioJpaRepository usuarioRepo,
                               NotificationPort notificationPort) {
        this.ventaRepo   = ventaRepo;
        this.detalleRepo = detalleRepo;
        this.productoRepo = productoRepo;
        this.usuarioRepo = usuarioRepo;
        this.notificationPort = notificationPort;
    }

    @Transactional
    public void ejecutar(UUID ventaId, String motivo, UUID usuarioId) {
        var venta = ventaRepo.findById(ventaId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta no encontrada"));

        if (venta.isAnulada()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La venta ya fue anulada");
        }

        // Restaurar stock de cada línea
        var detalles = detalleRepo.findByVentaId(ventaId);
        for (var detalle : detalles) {
            if (detalle.getProductoId() != null) {
                productoRepo.findByIdWithLock(detalle.getProductoId()).ifPresent(producto -> {
                    producto.setStockActual(producto.getStockActual() + detalle.getCantidad());
                    productoRepo.save(producto);
                    log.info("Stock restaurado: {} → +{} (venta {} anulada)",
                        producto.getSku(), detalle.getCantidad(), ventaId);
                });
            }
        }

        venta.setAnulada(true);
        venta.setFechaAnulacion(LocalDateTime.now());
        venta.setMotivoAnulacion(motivo);
        ventaRepo.save(venta);

        String usuarioNombre = usuarioId != null
            ? usuarioRepo.findById(usuarioId).map(u -> u.getNombre()).orElse("Sistema")
            : "Sistema";
        notificationPort.enviarVentaAnulada(venta.getNumeroTicket(), venta.getTotalVenta(), usuarioNombre, motivo);

        log.info("Venta {} anulada por {}. Motivo: {}", ventaId, usuarioNombre, motivo);
    }
}
