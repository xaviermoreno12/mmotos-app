package com.mmotos.infrastructure.output.persistence.jpa;

import com.mmotos.infrastructure.output.persistence.entity.SyncLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SyncLogJpaRepository extends JpaRepository<SyncLogEntity, UUID> {

    @Query("SELECT s FROM SyncLogEntity s WHERE s.estado IN ('PENDING', 'FAILED') AND s.proximaSincronizacion <= :ahora")
    List<SyncLogEntity> findPendientesParaSincronizar(@Param("ahora") LocalDateTime ahora);

    Optional<SyncLogEntity> findByEntidadId(UUID entidadId);
}
