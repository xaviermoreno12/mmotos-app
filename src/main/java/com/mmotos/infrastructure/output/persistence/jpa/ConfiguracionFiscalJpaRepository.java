package com.mmotos.infrastructure.output.persistence.jpa;

import com.mmotos.infrastructure.output.persistence.entity.ConfiguracionFiscalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfiguracionFiscalJpaRepository extends JpaRepository<ConfiguracionFiscalEntity, UUID> {
    Optional<ConfiguracionFiscalEntity> findByAlias(String alias);
}
