package com.mmotos.infrastructure.output.persistence.jpa;

import com.mmotos.infrastructure.output.persistence.entity.PresupuestoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PresupuestoJpaRepository extends JpaRepository<PresupuestoEntity, UUID> {
    List<PresupuestoEntity> findAllByOrderByFechaDesc();
    List<PresupuestoEntity> findByEstadoOrderByFechaDesc(String estado);
}
