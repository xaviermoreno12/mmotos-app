package com.mmotos.infrastructure.input.rest;

import com.mmotos.application.dto.ClienteDTO;
import com.mmotos.application.dto.CrearClienteRequest;
import com.mmotos.application.usecase.GestionarClientesUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final GestionarClientesUseCase useCase;

    public ClienteController(GestionarClientesUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public List<ClienteDTO> listar(@RequestParam(required = false) String termino) {
        return useCase.listar(termino);
    }

    @GetMapping("/{id}")
    public ClienteDTO obtener(@PathVariable UUID id) {
        return useCase.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteDTO crear(@Valid @RequestBody CrearClienteRequest request) {
        return useCase.crear(request);
    }

    @PutMapping("/{id}")
    public ClienteDTO actualizar(@PathVariable UUID id,
                                 @RequestBody Map<String, Object> campos) {
        return useCase.actualizar(id, campos);
    }
}
