package com.mmotos.infrastructure.persistence;

import com.mmotos.infrastructure.output.persistence.entity.SyncLogEntity;
import com.mmotos.infrastructure.output.persistence.jpa.SyncLogJpaRepository;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SyncLogJpaRepositoryTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("mmotos_test")
        .withUsername("test")
        .withPassword("test");

    @BeforeAll
    static void startContainer() {
        Assumptions.assumeTrue(
            DockerClientFactory.instance().isDockerAvailable(),
            "Docker no disponible — se omiten tests de Testcontainers"
        );
        postgres.start();
    }

    @Autowired
    SyncLogJpaRepository repository;

    @Test
    @DisplayName("findPendientesParaSincronizar devuelve PENDING cuya proxima_sync ya llegó")
    void findPendientesSoloLosQueCorresponden() {
        UUID ventaId = UUID.randomUUID();
        SyncLogEntity pendiente = new SyncLogEntity(UUID.randomUUID(), ventaId, "VENTA");
        repository.save(pendiente);

        // Sincronizado — no debe aparecer
        SyncLogEntity synced = new SyncLogEntity(UUID.randomUUID(), UUID.randomUUID(), "VENTA");
        synced.marcarSynced();
        repository.save(synced);

        List<SyncLogEntity> resultado = repository.findPendientesParaSincronizar(LocalDateTime.now().plusSeconds(1));

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getEntidadId()).isEqualTo(ventaId);
    }

    @Test
    @DisplayName("findByEntidadId devuelve el SyncLog correcto")
    void findByEntidadIdDevuelveElCorrecto() {
        UUID ventaId = UUID.randomUUID();
        repository.save(new SyncLogEntity(UUID.randomUUID(), ventaId, "VENTA"));
        repository.save(new SyncLogEntity(UUID.randomUUID(), UUID.randomUUID(), "VENTA"));

        assertThat(repository.findByEntidadId(ventaId))
            .isPresent()
            .get()
            .extracting(SyncLogEntity::getEntidadId)
            .isEqualTo(ventaId);
    }

    @Test
    @DisplayName("Log con estado FAILED y proxima_sync en el futuro NO aparece en pendientes")
    void failedEnFuturoNoAparece() {
        SyncLogEntity failed = new SyncLogEntity(UUID.randomUUID(), UUID.randomUUID(), "VENTA");
        failed.registrarFallo("timeout");  // proxima_sync = now + 1min (backoff)
        repository.save(failed);

        // Consultamos con "ahora" — el log tiene proxima_sync en el futuro
        List<SyncLogEntity> resultado = repository.findPendientesParaSincronizar(LocalDateTime.now());

        assertThat(resultado).isEmpty();
    }
}
