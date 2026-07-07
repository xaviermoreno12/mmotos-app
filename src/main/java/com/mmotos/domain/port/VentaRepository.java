package com.mmotos.domain.port;

import com.mmotos.domain.model.SyncStatus;
import com.mmotos.domain.model.Venta;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VentaRepository {

    Venta save(Venta venta);

    Optional<Venta> findById(UUID id);

    List<Venta> findBySyncStatus(SyncStatus status);
}
