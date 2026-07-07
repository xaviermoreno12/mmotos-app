package com.mmotos.infrastructure.output.persistence.jpa;

import com.mmotos.infrastructure.output.persistence.entity.PagoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PagoJpaRepository extends JpaRepository<PagoEntity, UUID> {

    List<PagoEntity> findByVentaId(UUID ventaId);
}
