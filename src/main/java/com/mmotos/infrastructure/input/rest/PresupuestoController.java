package com.mmotos.infrastructure.input.rest;

import com.mmotos.application.dto.CrearPresupuestoRequest;
import com.mmotos.application.dto.PresupuestoDTO;
import com.mmotos.application.usecase.GestionarPresupuestosUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/presupuestos")
public class PresupuestoController {

    private final GestionarPresupuestosUseCase useCase;

    public PresupuestoController(GestionarPresupuestosUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public List<PresupuestoDTO> listar() { return useCase.listar(); }

    @GetMapping("/{id}")
    public PresupuestoDTO obtener(@PathVariable UUID id) { return useCase.obtener(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PresupuestoDTO crear(@Valid @RequestBody CrearPresupuestoRequest req) { return useCase.crear(req); }

    @PatchMapping("/{id}/estado")
    public PresupuestoDTO cambiarEstado(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return useCase.cambiarEstado(id, body.get("estado"));
    }
}
