package com.mmotos.infrastructure.output.persistence.jpa;

import com.mmotos.infrastructure.output.persistence.entity.GastoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface GastoJpaRepository extends JpaRepository<GastoEntity, UUID> {
    List<GastoEntity> findAllByOrderByFechaDesc();

    @Query("SELECT g FROM GastoEntity g WHERE g.fecha BETWEEN :desde AND :hasta ORDER BY g.fecha DESC")
    List<GastoEntity> findByPeriodo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
