package com.mmotos.infrastructure.output.persistence.jpa;

import com.mmotos.infrastructure.output.persistence.entity.MetodoPagoConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MetodoPagoConfigJpaRepository extends JpaRepository<MetodoPagoConfigEntity, UUID> {
    List<MetodoPagoConfigEntity> findAllByOrderByOrdenAsc();
}
