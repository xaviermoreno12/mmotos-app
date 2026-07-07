package com.mmotos.application.usecase;

import com.mmotos.application.dto.ChequeDTO;
import com.mmotos.application.dto.CrearChequeRequest;
import com.mmotos.infrastructure.output.persistence.entity.ChequeEntity;
import com.mmotos.infrastructure.output.persistence.jpa.ChequeJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class GestionarChequesUseCase {

    private final ChequeJpaRepository repo;

    public GestionarChequesUseCase(ChequeJpaRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<ChequeDTO> listar(String tipo) {
        var lista = tipo != null ? repo.findByTipoOrderByFechaCobro(tipo.toUpperCase())
                                 : repo.findAllByOrderByFechaCobro();
        return lista.stream().map(this::toDTO).toList();
    }

    @Transactional
    public ChequeDTO crear(CrearChequeRequest req) {
        var entity = new ChequeEntity(UUID.randomUUID(), req.tipo(), req.numero(), req.banco(),
            req.librador(), req.monto(), req.fechaEmision(), req.fechaCobro(),
            req.clienteId(), req.proveedorId(), req.observaciones());
        return toDTO(repo.save(entity));
    }

    @Transactional
    public ChequeDTO cambiarEstado(UUID id, String estado) {
        var entity = repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cheque no encontrado"));
        entity.setEstado(estado.toUpperCase());
        return toDTO(repo.save(entity));
    }

    private ChequeDTO toDTO(ChequeEntity e) {
        return new ChequeDTO(e.getId().toString(), e.getTipo(), e.getNumero(), e.getBanco(),
            e.getLibrador(), e.getMonto(),
            e.getFechaEmision().toString(), e.getFechaCobro().toString(),
            e.getEstado(),
            e.getClienteId() != null ? e.getClienteId().toString() : null,
            e.getProveedorId() != null ? e.getProveedorId().toString() : null,
            e.getObservaciones());
    }
}
