package com.mmotos.infrastructure.input.rest;

import com.mmotos.application.dto.CrearProveedorRequest;
import com.mmotos.application.dto.ProveedorDTO;
import com.mmotos.application.usecase.GestionarProveedoresUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    private final GestionarProveedoresUseCase useCase;

    public ProveedorController(GestionarProveedoresUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public List<ProveedorDTO> listar(@RequestParam(required = false) String termino) {
        return useCase.listar(termino);
    }

    @GetMapping("/{id}")
    public ProveedorDTO obtener(@PathVariable UUID id) {
        return useCase.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProveedorDTO crear(@Valid @RequestBody CrearProveedorRequest request) {
        return useCase.crear(request);
    }

    @PutMapping("/{id}")
    public ProveedorDTO actualizar(@PathVariable UUID id,
                                   @RequestBody Map<String, Object> campos) {
        return useCase.actualizar(id, campos);
    }
}
