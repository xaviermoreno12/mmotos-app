package com.mmotos.infrastructure.output.persistence.jpa;

import com.mmotos.infrastructure.output.persistence.entity.VentaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface VentaJpaRepository extends JpaRepository<VentaEntity, UUID> {

    // Carga eager de detalles y pagos solo cuando se necesita para sincronización
    @Query("SELECT v FROM VentaEntity v WHERE v.syncStatus IN :estados")
    List<VentaEntity> findBySyncStatusIn(@Param("estados") List<String> estados);

    @Query("SELECT v FROM VentaEntity v WHERE v.fechaEmision BETWEEN :desde AND :hasta ORDER BY v.fechaEmision DESC")
    List<VentaEntity> findByFechaEmisionBetween(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("SELECT COUNT(v), COALESCE(SUM(v.totalVenta), 0) FROM VentaEntity v WHERE v.fechaEmision BETWEEN :desde AND :hasta AND v.anulada = false")
    List<Object[]> resumenDiario(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
