package com.mmotos.infrastructure.output.persistence.jpa;

import com.mmotos.infrastructure.output.persistence.entity.CobranzaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CobranzaJpaRepository extends JpaRepository<CobranzaEntity, UUID> {
    List<CobranzaEntity> findAllByOrderByFechaDesc();
    List<CobranzaEntity> findByClienteIdOrderByFechaDesc(UUID clienteId);
}
