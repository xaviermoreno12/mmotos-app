package com.mmotos.infrastructure.input.rest;

import com.mmotos.application.dto.ActualizarMetodoPagoRequest;
import com.mmotos.application.dto.CrearMetodoPagoRequest;
import com.mmotos.application.dto.MetodoPagoDTO;
import com.mmotos.infrastructure.output.persistence.entity.MetodoPagoConfigEntity;
import com.mmotos.infrastructure.output.persistence.jpa.MetodoPagoConfigJpaRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/metodos-pago")
public class MetodoPagoController {

    private final MetodoPagoConfigJpaRepository repo;

    public MetodoPagoController(MetodoPagoConfigJpaRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<MetodoPagoDTO> listar() {
        return repo.findAllByOrderByOrdenAsc().stream()
            .map(e -> new MetodoPagoDTO(
                e.getId().toString(),
                e.getCodigo(),
                e.getNombre(),
                e.isAceptaCobro(),
                e.isAceptaPago(),
                e.isHabilitado(),
                e.getOrden()
            ))
            .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('DUENO')")
    public MetodoPagoDTO agregar(@Valid @RequestBody CrearMetodoPagoRequest req) {
        var entity = new MetodoPagoConfigEntity(
            UUID.randomUUID(),
            req.codigo().toUpperCase(),
            req.nombre(),
            req.aceptaCobro(),
            req.aceptaPago(),
            true,
            repo.findAllByOrderByOrdenAsc().size() + 1
        );
        var saved = repo.save(entity);
        return new MetodoPagoDTO(saved.getId().toString(), saved.getCodigo(), saved.getNombre(),
            saved.isAceptaCobro(), saved.isAceptaPago(), saved.isHabilitado(), saved.getOrden());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('DUENO')")
    public void eliminar(@PathVariable UUID id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Método de pago no encontrado");
        }
        repo.deleteById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DUENO')")
    public MetodoPagoDTO actualizar(@PathVariable UUID id,
                                    @RequestBody ActualizarMetodoPagoRequest req) {
        var entity = repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Método de pago no encontrado"));

        if (req.nombre() != null && !req.nombre().isBlank()) entity.setNombre(req.nombre());
        if (req.aceptaCobro() != null) entity.setAceptaCobro(req.aceptaCobro());
        if (req.aceptaPago() != null) entity.setAceptaPago(req.aceptaPago());
        if (req.habilitado() != null) entity.setHabilitado(req.habilitado());

        var saved = repo.save(entity);
        return new MetodoPagoDTO(
            saved.getId().toString(),
            saved.getCodigo(),
            saved.getNombre(),
            saved.isAceptaCobro(),
            saved.isAceptaPago(),
            saved.isHabilitado(),
            saved.getOrden()
        );
    }
}
