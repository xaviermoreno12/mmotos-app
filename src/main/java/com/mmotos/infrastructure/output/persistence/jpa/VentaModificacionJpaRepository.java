package com.mmotos.infrastructure.output.persistence.jpa;

import com.mmotos.infrastructure.output.persistence.entity.VentaModificacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VentaModificacionJpaRepository extends JpaRepository<VentaModificacionEntity, UUID> {
    List<VentaModificacionEntity> findByVentaIdOrderByFechaDesc(UUID ventaId);
}
