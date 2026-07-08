package com.mmotos.application.usecase;

import com.mmotos.application.dto.*;
import com.mmotos.domain.model.*;
import com.mmotos.domain.port.*;
import com.mmotos.domain.service.VentaBuilder;
import com.mmotos.infrastructure.output.persistence.entity.SyncLogEntity;
import com.mmotos.infrastructure.output.persistence.jpa.SyncLogJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

// Bean separado para que @Transactional funcione correctamente vía Spring AOP proxy.
// RealizarVentaUseCase no puede llamar sus propios métodos @Transactional porque Spring
// bypasea el proxy en llamadas internas (self-invocation).
@Service
public class VentaTransaccionHelper {

    private static final Logger log = LoggerFactory.getLogger(VentaTransaccionHelper.class);

    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;
    private final NotificationPort notificationPort;
    private final SyncLogJpaRepository syncLogRepository;

    public VentaTransaccionHelper(ProductoRepository productoRepository,
                                   VentaRepository ventaRepository,
                                   NotificationPort notificationPort,
                                   SyncLogJpaRepository syncLogRepository) {
        this.productoRepository = productoRepository;
        this.ventaRepository    = ventaRepository;
        this.notificationPort   = notificationPort;
        this.syncLogRepository  = syncLogRepository;
    }

    @Transactional
    public Venta guardarPendiente(VentaRequest request, BigDecimal cotizacion, FiscalPort fiscalPort) {
        VentaBuilder builder = new VentaBuilder().conUsuario(request.usuarioId());

        builder = switch (request.tipoFactura().toUpperCase()) {
            case "A"        -> builder.conFacturaA(request.cuitCliente());
            case "C"        -> builder.conFacturaC();
            case "NO_FISCAL"-> builder.sinFactura();
            default         -> builder.conFacturaB();
        };

        for (LineaVentaRequest item : request.lineas()) {
            Repuesto repuesto = productoRepository.findByIdWithLock(item.productoId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + item.productoId()));

            BigDecimal precioHistorico = repuesto.getPrecio().calcularEnPesos(cotizacion);
            if (precioHistorico.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                    "El producto '" + repuesto.getNombre() + "' tiene precio $0 o inválido. " +
                    "Actualice el precio en la sección Productos antes de vender."
                );
            }
            builder.agregarLinea(repuesto.getId(), repuesto.getSku(), repuesto.getNombre(),
                                 item.cantidad(), precioHistorico);

            repuesto.descontarStock(item.cantidad());
            productoRepository.save(repuesto);

            if (repuesto.estaBajoMinimo()) {
                notificationPort.enviarAlertaStockBajo(
                    repuesto.getId(), repuesto.getSku(), repuesto.getNombre(), repuesto.getStockActual());
            }
        }

        for (PagoRequest p : request.pagos()) {
            builder.agregarPago(buildPago(p));
        }

        Venta venta = builder.build(fiscalPort);
        ventaRepository.save(venta);
        syncLogRepository.save(new SyncLogEntity(UUID.randomUUID(), venta.getId(), "VENTA"));

        log.info("Venta {} guardada como PENDIENTE", venta.getId());
        return venta;
    }

    @Transactional
    public VentaResponse confirmarFiscal(UUID ventaId, FiscalPort.FiscalResponse fiscalResponse) {
        var totalRef = new java.math.BigDecimal[]{java.math.BigDecimal.ZERO};
        var fechaRef = new java.time.LocalDateTime[]{java.time.LocalDateTime.now()};

        ventaRepository.findById(ventaId).ifPresent(v -> {
            v.confirmarDatosFiscales(fiscalResponse.cae(), fiscalResponse.numeroTicket());
            ventaRepository.save(v);
            totalRef[0] = v.totalLineas();
            fechaRef[0] = v.getFechaEmision();
        });

        syncLogRepository.findByEntidadId(ventaId)
            .ifPresent(s -> { s.marcarSynced(); syncLogRepository.save(s); });

        log.info("Venta {} confirmada. Ticket: {}, CAE: {}", ventaId,
            fiscalResponse.numeroTicket(), fiscalResponse.cae());

        return new VentaResponse(ventaId, fiscalResponse.numeroTicket(), fiscalResponse.cae(),
            EstadoFiscal.APROBADO.name(), SyncStatus.SYNCED.name(), totalRef[0], fechaRef[0]);
    }

    @Transactional
    public void marcarError(UUID ventaId) {
        ventaRepository.findById(ventaId).ifPresent(v -> {
            v.marcarErrorHardware();
            ventaRepository.save(v);
        });
    }

    private Pago buildPago(PagoRequest p) {
        MetodoPago metodo = MetodoPago.valueOf(p.metodo().toUpperCase());
        return switch (metodo) {
            case TARJETA_DEBITO, TARJETA_CREDITO ->
                new PagoTarjeta(metodo, p.monto(),
                    p.numeroCupon() != null ? p.numeroCupon() : "",
                    p.cuotas() != null ? p.cuotas() : 1);
            case TRANSFERENCIA  -> new PagoTransferencia(p.monto(), p.cbuOrigen());
            case MERCADO_PAGO   -> new PagoMercadoPago(p.monto(), p.referenciaPago());
            default             -> new PagoEfectivo(p.monto());
        };
    }
}
