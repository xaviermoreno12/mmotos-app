package com.mmotos.infrastructure.output.persistence.jpa;

import com.mmotos.infrastructure.output.persistence.entity.ChequeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChequeJpaRepository extends JpaRepository<ChequeEntity, UUID> {
    List<ChequeEntity> findAllByOrderByFechaCobro();
    List<ChequeEntity> findByTipoOrderByFechaCobro(String tipo);
    List<ChequeEntity> findByEstadoOrderByFechaCobro(String estado);
}
