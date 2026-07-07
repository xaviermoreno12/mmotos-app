package com.mmotos.application.usecase;

import com.mmotos.application.dto.CobranzaDTO;
import com.mmotos.application.dto.CrearCobranzaRequest;
import com.mmotos.infrastructure.output.persistence.entity.CobranzaEntity;
import com.mmotos.infrastructure.output.persistence.jpa.ClienteJpaRepository;
import com.mmotos.infrastructure.output.persistence.jpa.CobranzaJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class RegistrarCobranzaUseCase {

    private final CobranzaJpaRepository cobranzaRepo;
    private final ClienteJpaRepository clienteRepo;

    public RegistrarCobranzaUseCase(CobranzaJpaRepository cobranzaRepo,
                                     ClienteJpaRepository clienteRepo) {
        this.cobranzaRepo = cobranzaRepo;
        this.clienteRepo  = clienteRepo;
    }

    @Transactional(readOnly = true)
    public List<CobranzaDTO> listar() {
        return cobranzaRepo.findAllByOrderByFechaDesc().stream().map(this::toDTO).toList();
    }

    @Transactional
    public CobranzaDTO registrar(CrearCobranzaRequest req) {
        var cliente = clienteRepo.findById(req.clienteId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        // Actualiza el saldo del cliente (reduce la deuda)
        cliente.setSaldo(cliente.getSaldo().add(req.monto()));
        clienteRepo.save(cliente);

        UUID usuarioId = req.usuarioId() != null ? UUID.fromString(req.usuarioId()) : null;
        var entity = new CobranzaEntity(UUID.randomUUID(), req.clienteId(), req.monto(),
            req.metodoPago(), req.referencia(), req.observaciones(), usuarioId);

        return toDTO(cobranzaRepo.save(entity));
    }

    private CobranzaDTO toDTO(CobranzaEntity e) {
        var cliente = clienteRepo.findById(e.getClienteId());
        String nombre = cliente.map(c -> c.getNombre()).orElse("—");
        return new CobranzaDTO(e.getId().toString(), e.getClienteId().toString(), nombre,
            e.getMonto(), e.getFecha().toString(), e.getMetodoPago(),
            e.getReferencia(), e.getObservaciones());
    }
}
