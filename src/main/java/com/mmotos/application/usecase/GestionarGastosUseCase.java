package com.mmotos.application.usecase;

import com.mmotos.application.dto.CrearGastoRequest;
import com.mmotos.application.dto.GastoDTO;
import com.mmotos.infrastructure.output.persistence.entity.GastoEntity;
import com.mmotos.infrastructure.output.persistence.jpa.GastoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GestionarGastosUseCase {

    private final GastoJpaRepository repo;

    public GestionarGastosUseCase(GastoJpaRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<GastoDTO> listar() {
        return repo.findAllByOrderByFechaDesc().stream().map(this::toDTO).toList();
    }

    @Transactional
    public GastoDTO crear(CrearGastoRequest req) {
        UUID usuarioId = req.usuarioId() != null ? UUID.fromString(req.usuarioId()) : null;
        var entity = new GastoEntity(
            UUID.randomUUID(), null, req.descripcion(), req.categoria(),
            req.monto(), req.metodoPago(), usuarioId, req.observaciones()
        );
        return toDTO(repo.save(entity));
    }

    @Transactional
    public void eliminar(UUID id) {
        repo.deleteById(id);
    }

    private GastoDTO toDTO(GastoEntity e) {
        return new GastoDTO(e.getId().toString(), e.getFecha().toString(),
            e.getDescripcion(), e.getCategoria(), e.getMonto(),
            e.getMetodoPago(), e.getObservaciones());
    }
}
