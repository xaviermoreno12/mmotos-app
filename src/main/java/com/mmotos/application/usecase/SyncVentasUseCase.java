package com.mmotos.application.usecase;

import com.mmotos.domain.port.NotificationPort;
import com.mmotos.infrastructure.output.persistence.entity.SyncLogEntity;
import com.mmotos.infrastructure.output.persistence.jpa.SyncLogJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SyncVentasUseCase {

    private static final Logger log = LoggerFactory.getLogger(SyncVentasUseCase.class);
    private static final int MAX_INTENTOS = 10;

    private final SyncLogJpaRepository syncLogRepository;
    private final SyncVentaProcessor processor;
    private final NotificationPort notificationPort;

    public SyncVentasUseCase(SyncLogJpaRepository syncLogRepository,
                              SyncVentaProcessor processor,
                              NotificationPort notificationPort) {
        this.syncLogRepository = syncLogRepository;
        this.processor         = processor;
        this.notificationPort  = notificationPort;
    }

    @Scheduled(fixedDelay = 60_000)
    public void procesarPendientes() {
        List<SyncLogEntity> pendientes = syncLogRepository.findPendientesParaSincronizar(LocalDateTime.now());
        if (pendientes.isEmpty()) return;

        log.info("SyncService: {} venta(s) pendiente(s)", pendientes.size());

        for (SyncLogEntity entry : pendientes) {
            if (entry.getIntentos() >= MAX_INTENTOS) {
                entry.marcarConflicto("Superó el máximo de %d intentos".formatted(MAX_INTENTOS));
                syncLogRepository.save(entry);
                notificationPort.enviarAlertaCritica(
                    "Venta %s no pudo sincronizarse tras %d intentos".formatted(entry.getEntidadId(), MAX_INTENTOS));
                continue;
            }
            processor.procesar(entry);   // @Transactional efectivo vía proxy AOP separado
        }
    }
}
