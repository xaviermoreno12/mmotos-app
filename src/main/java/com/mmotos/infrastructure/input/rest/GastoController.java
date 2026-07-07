package com.mmotos.infrastructure.input.rest;

import com.mmotos.application.dto.CrearGastoRequest;
import com.mmotos.application.dto.GastoDTO;
import com.mmotos.application.usecase.GestionarGastosUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gastos")
public class GastoController {

    private final GestionarGastosUseCase useCase;

    public GastoController(GestionarGastosUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public List<GastoDTO> listar() { return useCase.listar(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GastoDTO crear(@Valid @RequestBody CrearGastoRequest req) { return useCase.crear(req); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable UUID id) { useCase.eliminar(id); }
}
