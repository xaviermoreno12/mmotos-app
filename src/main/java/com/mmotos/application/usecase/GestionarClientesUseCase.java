package com.mmotos.application.usecase;

import com.mmotos.application.dto.ClienteDTO;
import com.mmotos.application.dto.CrearClienteRequest;
import com.mmotos.infrastructure.output.persistence.entity.ClienteEntity;
import com.mmotos.infrastructure.output.persistence.jpa.ClienteJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GestionarClientesUseCase {

    private final ClienteJpaRepository repo;

    public GestionarClientesUseCase(ClienteJpaRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<ClienteDTO> listar(String termino) {
        var lista = (termino != null && !termino.isBlank())
            ? repo.buscar(termino)
            : repo.findAllByActivoTrueOrderByNombreAsc();
        return lista.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public ClienteDTO obtener(UUID id) {
        return toDTO(repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado")));
    }

    @Transactional
    public ClienteDTO crear(CrearClienteRequest req) {
        if (req.cuit() != null && !req.cuit().isBlank()) {
            repo.findByCuit(req.cuit()).ifPresent(c -> {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un cliente con ese CUIT");
            });
        }
        var entity = new ClienteEntity(UUID.randomUUID(), req.cuit(), req.nombre(),
                                       req.direccion(), req.telefono(), req.email());
        return toDTO(repo.save(entity));
    }

    @Transactional
    public ClienteDTO actualizar(UUID id, Map<String, Object> campos) {
        var entity = repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        if (campos.containsKey("nombre") && campos.get("nombre") != null)
            entity.setNombre((String) campos.get("nombre"));
        if (campos.containsKey("cuit"))
            entity.setCuit((String) campos.get("cuit"));
        if (campos.containsKey("direccion"))
            entity.setDireccion((String) campos.get("direccion"));
        if (campos.containsKey("telefono"))
            entity.setTelefono((String) campos.get("telefono"));
        if (campos.containsKey("email"))
            entity.setEmail((String) campos.get("email"));
        if (campos.containsKey("activo") && campos.get("activo") != null)
            entity.setActivo((Boolean) campos.get("activo"));

        return toDTO(repo.save(entity));
    }

    private ClienteDTO toDTO(ClienteEntity e) {
        return new ClienteDTO(e.getId().toString(), e.getCuit(), e.getNombre(),
                              e.getDireccion(), e.getTelefono(), e.getEmail(),
                              e.getSaldo(), e.isActivo());
    }
}
