package com.mmotos.application.usecase;

import com.mmotos.application.dto.CrearProveedorRequest;
import com.mmotos.application.dto.ProveedorDTO;
import com.mmotos.infrastructure.output.persistence.entity.ProveedorEntity;
import com.mmotos.infrastructure.output.persistence.jpa.ProveedorJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GestionarProveedoresUseCase {

    private final ProveedorJpaRepository repo;

    public GestionarProveedoresUseCase(ProveedorJpaRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<ProveedorDTO> listar(String termino) {
        var lista = (termino != null && !termino.isBlank())
            ? repo.buscar(termino)
            : repo.findAllByActivoTrueOrderByNombreAsc();
        return lista.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public ProveedorDTO obtener(UUID id) {
        return toDTO(repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado")));
    }

    @Transactional
    public ProveedorDTO crear(CrearProveedorRequest req) {
        if (req.cuit() != null && !req.cuit().isBlank()) {
            repo.findByCuit(req.cuit()).ifPresent(p -> {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un proveedor con ese CUIT");
            });
        }
        var entity = new ProveedorEntity(UUID.randomUUID(), req.cuit(), req.nombre(),
                                         req.contacto(), req.telefono(), req.email());
        return toDTO(repo.save(entity));
    }

    @Transactional
    public ProveedorDTO actualizar(UUID id, Map<String, Object> campos) {
        var entity = repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));

        if (campos.containsKey("nombre") && campos.get("nombre") != null)
            entity.setNombre((String) campos.get("nombre"));
        if (campos.containsKey("cuit"))
            entity.setCuit((String) campos.get("cuit"));
        if (campos.containsKey("contacto"))
            entity.setContacto((String) campos.get("contacto"));
        if (campos.containsKey("telefono"))
            entity.setTelefono((String) campos.get("telefono"));
        if (campos.containsKey("email"))
            entity.setEmail((String) campos.get("email"));
        if (campos.containsKey("activo") && campos.get("activo") != null)
            entity.setActivo((Boolean) campos.get("activo"));

        return toDTO(repo.save(entity));
    }

    private ProveedorDTO toDTO(ProveedorEntity e) {
        return new ProveedorDTO(e.getId().toString(), e.getCuit(), e.getNombre(),
                                e.getContacto(), e.getTelefono(), e.getEmail(), e.isActivo());
    }
}
