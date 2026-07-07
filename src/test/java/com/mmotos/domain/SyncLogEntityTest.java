package com.mmotos.domain;

import com.mmotos.infrastructure.output.persistence.entity.SyncLogEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class SyncLogEntityTest {

    @Test
    @DisplayName("Backoff exponencial: el primer fallo reintenta en 1 minuto")
    void primerFalloBackoffUnMinuto() {
        SyncLogEntity log = new SyncLogEntity(UUID.randomUUID(), UUID.randomUUID(), "VENTA");
        LocalDateTime antes = LocalDateTime.now();

        log.registrarFallo("AFIP timeout");

        assertThat(log.getEstado()).isEqualTo("FAILED");
        assertThat(log.getIntentos()).isEqualTo(1);
        // 1^2 = 1 minuto de espera
        assertThat(log.getProximaSincronizacion()).isAfterOrEqualTo(antes.plusSeconds(50));
    }

    @Test
    @DisplayName("Backoff exponencial: el tercer fallo espera 9 minutos")
    void tercerFalloBackoffNueveMinutos() {
        SyncLogEntity log = new SyncLogEntity(UUID.randomUUID(), UUID.randomUUID(), "VENTA");
        log.registrarFallo("error 1");
        log.registrarFallo("error 2");

        LocalDateTime antes = LocalDateTime.now();
        log.registrarFallo("error 3");

        // 3^2 = 9 minutos
        assertThat(log.getProximaSincronizacion()).isAfterOrEqualTo(antes.plusMinutes(8));
        assertThat(log.getIntentos()).isEqualTo(3);
    }

    @Test
    @DisplayName("marcarSynced limpia el error y cambia estado")
    void marcarSyncedLimpiaEstado() {
        SyncLogEntity log = new SyncLogEntity(UUID.randomUUID(), UUID.randomUUID(), "VENTA");
        log.registrarFallo("timeout");

        log.marcarSynced();

        assertThat(log.getEstado()).isEqualTo("SYNCED");
        assertThat(log.getUltimoError()).isNull();
    }

    @Test
    @DisplayName("marcarConflicto guarda el mensaje y cambia a CONFLICT")
    void marcarConflictoGuardaMensaje() {
        SyncLogEntity log = new SyncLogEntity(UUID.randomUUID(), UUID.randomUUID(), "VENTA");

        log.marcarConflicto("CUIT inválido para AFIP");

        assertThat(log.getEstado()).isEqualTo("CONFLICT");
        assertThat(log.getUltimoError()).contains("CUIT inválido");
    }
}
