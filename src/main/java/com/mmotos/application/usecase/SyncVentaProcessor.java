package com.mmotos.application.usecase;

import com.mmotos.domain.exception.FiscalException;
import com.mmotos.domain.model.Venta;
import com.mmotos.domain.port.FiscalPort;
import com.mmotos.domain.port.NotificationPort;
import com.mmotos.domain.port.VentaRepository;
import com.mmotos.infrastructure.output.persistence.entity.SyncLogEntity;
import com.mmotos.infrastructure.output.persistence.jpa.SyncLogJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Bean separado para que @Transactional de procesarUno funcione via proxy AOP.
// SyncVentasUseCase no puede llamarse a sí mismo — Spring bypasea el proxy.
@Service
public class SyncVentaProcessor {

    private static final Logger log = LoggerFactory.getLogger(SyncVentaProcessor.class);

    private final SyncLogJpaRepository syncLogRepository;
    private final VentaRepository ventaRepository;
    private final FiscalPort fiscalPort;
    private final NotificationPort notificationPort;

    public SyncVentaProcessor(SyncLogJpaRepository syncLogRepository,
                               VentaRepository ventaRepository,
                               FiscalPort fiscalPort,
                               NotificationPort notificationPort) {
        this.syncLogRepository = syncLogRepository;
        this.ventaRepository   = ventaRepository;
        this.fiscalPort        = fiscalPort;
        this.notificationPort  = notificationPort;
    }

    @Transactional
    public void procesar(SyncLogEntity syncLog) {
        ventaRepository.findById(syncLog.getEntidadId()).ifPresentOrElse(
            venta -> intentarEmision(syncLog, venta),
            () -> {
                syncLog.marcarConflicto("Venta no encontrada en DB");
                syncLogRepository.save(syncLog);
            }
        );
    }

    private void intentarEmision(SyncLogEntity syncLog, Venta venta) {
        try {
            FiscalPort.FiscalResponse response = fiscalPort.emitir(venta);
            venta.confirmarDatosFiscales(response.cae(), response.numeroTicket());
            ventaRepository.save(venta);
            syncLog.marcarSynced();
            log.info("Venta {} sincronizada. CAE: {}", venta.getId(), response.cae());
        } catch (FiscalException e) {
            syncLog.marcarConflicto(e.getMessage());
            notificationPort.enviarAlertaCritica(
                "Error de validación fiscal en venta offline %s: %s".formatted(venta.getId(), e.getMessage()));
            log.error("Conflicto fiscal en venta {}: {}", venta.getId(), e.getMessage());
        } catch (Exception e) {
            syncLog.registrarFallo(e.getMessage());
            log.warn("Fallo técnico sincronizando venta {} (intento {}): {}",
                venta.getId(), syncLog.getIntentos(), e.getMessage());
        } finally {
            syncLogRepository.save(syncLog);
        }
    }
}
