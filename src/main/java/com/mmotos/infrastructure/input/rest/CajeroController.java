package com.mmotos.infrastructure.input.rest;

import com.mmotos.application.dto.ActualizarCajeroRequest;
import com.mmotos.application.dto.CajeroDTO;
import com.mmotos.application.dto.CrearCajeroRequest;
import com.mmotos.application.usecase.GestionarCajerosUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cajeros")
public class CajeroController {

    private final GestionarCajerosUseCase useCase;

    public CajeroController(GestionarCajerosUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public List<CajeroDTO> listar() {
        return useCase.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('DUENO')")
    public CajeroDTO crear(@Valid @RequestBody CrearCajeroRequest request) {
        return useCase.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DUENO')")
    public CajeroDTO actualizar(@PathVariable UUID id,
                                @RequestBody ActualizarCajeroRequest request) {
        return useCase.actualizar(id, request);
    }
}
