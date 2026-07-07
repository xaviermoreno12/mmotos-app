package com.mmotos.infrastructure.output.persistence.jpa;

import com.mmotos.infrastructure.output.persistence.entity.CajaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CajaJpaRepository extends JpaRepository<CajaEntity, UUID> {

    /**
     * Buscar la caja abierta de un usuario específico.
     */
    @Query("SELECT c FROM CajaEntity c WHERE c.usuarioId = :usuarioId AND c.estado = 'ABIERTA'")
    Optional<CajaEntity> findCajaAbiertaByUsuario(@Param("usuarioId") UUID usuarioId);

    /**
     * Buscar cualquier caja abierta en el sistema (para validación).
     */
    @Query("SELECT c FROM CajaEntity c WHERE c.estado = 'ABIERTA'")
    List<CajaEntity> findCajasAbiertas();

    /**
     * Buscar la caja activa (abierta) — solo debería haber una en un negocio chico.
     */
    @Query("SELECT c FROM CajaEntity c WHERE c.estado = 'ABIERTA' ORDER BY c.fechaApertura DESC")
    Optional<CajaEntity> findCajaActiva();

    /**
     * Historial de cajas por rango de fechas.
     */
    @Query("SELECT c FROM CajaEntity c WHERE c.fechaApertura BETWEEN :desde AND :hasta ORDER BY c.fechaApertura DESC")
    List<CajaEntity> findByFechaAperturaBetween(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
