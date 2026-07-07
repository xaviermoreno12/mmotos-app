package com.mmotos.infrastructure.output.persistence.jpa;

import com.mmotos.infrastructure.output.persistence.entity.CompraBorradorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompraBorradorJpaRepository extends JpaRepository<CompraBorradorEntity, UUID> {
    List<CompraBorradorEntity> findByEstadoOrderByFechaRecepcionDesc(String estado);
}
