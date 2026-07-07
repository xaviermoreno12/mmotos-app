package com.mmotos.infrastructure.output.persistence.jpa;

import com.mmotos.infrastructure.output.persistence.entity.CompraDetalleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompraDetalleJpaRepository extends JpaRepository<CompraDetalleEntity, UUID> {
    List<CompraDetalleEntity> findByCompraId(UUID compraId);
}
