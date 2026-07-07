package com.mmotos.infrastructure.output.persistence.jpa;

import com.mmotos.infrastructure.output.persistence.entity.ProveedorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProveedorJpaRepository extends JpaRepository<ProveedorEntity, UUID> {

    Optional<ProveedorEntity> findByCuit(String cuit);

    @Query("SELECT p FROM ProveedorEntity p WHERE p.activo = true AND " +
           "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR p.cuit LIKE CONCAT('%', :termino, '%'))")
    List<ProveedorEntity> buscar(@Param("termino") String termino);

    List<ProveedorEntity> findAllByActivoTrueOrderByNombreAsc();
}
