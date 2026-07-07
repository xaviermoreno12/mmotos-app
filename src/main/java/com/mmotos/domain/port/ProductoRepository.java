package com.mmotos.domain.port;

import com.mmotos.domain.model.Repuesto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductoRepository {

    Optional<Repuesto> findById(UUID id);

    Optional<Repuesto> findByIdWithLock(UUID id);

    Optional<Repuesto> findBySku(String sku);

    List<Repuesto> findBajoMinimo();

    Repuesto save(Repuesto repuesto);
}
