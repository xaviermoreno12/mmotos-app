package com.mmotos.infrastructure.output.persistence.jpa;

import com.mmotos.infrastructure.output.persistence.entity.VentaDetalleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VentaDetalleJpaRepository extends JpaRepository<VentaDetalleEntity, UUID> {

    List<VentaDetalleEntity> findByVentaId(UUID ventaId);

    List<VentaDetalleEntity> findByProductoId(UUID productoId);
}
