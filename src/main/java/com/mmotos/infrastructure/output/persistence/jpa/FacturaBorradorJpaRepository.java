package com.mmotos.infrastructure.output.persistence.jpa;

import com.mmotos.infrastructure.output.persistence.entity.FacturaBorradorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FacturaBorradorJpaRepository extends JpaRepository<FacturaBorradorEntity, UUID> {
    List<FacturaBorradorEntity> findByEstadoOrderByFechaRecepcionDesc(String estado);
}
