package com.mmotos.infrastructure.output.persistence.jpa;

import com.mmotos.infrastructure.output.persistence.entity.PresupuestoDetalleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PresupuestoDetalleJpaRepository extends JpaRepository<PresupuestoDetalleEntity, UUID> {
    List<PresupuestoDetalleEntity> findByPresupuestoId(UUID presupuestoId);
}
