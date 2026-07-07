package com.mmotos.infrastructure.output.persistence.jpa;

import com.mmotos.infrastructure.output.persistence.entity.CompraEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompraJpaRepository extends JpaRepository<CompraEntity, UUID> {
    List<CompraEntity> findAllByOrderByFechaDesc();
    List<CompraEntity> findByProveedorIdOrderByFechaDesc(UUID proveedorId);
}
