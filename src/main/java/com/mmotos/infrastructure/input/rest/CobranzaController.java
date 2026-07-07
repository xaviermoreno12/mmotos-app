package com.mmotos.infrastructure.input.rest;

import com.mmotos.application.dto.CobranzaDTO;
import com.mmotos.application.dto.CrearCobranzaRequest;
import com.mmotos.application.usecase.RegistrarCobranzaUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cobranzas")
public class CobranzaController {

    private final RegistrarCobranzaUseCase useCase;

    public CobranzaController(RegistrarCobranzaUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public List<CobranzaDTO> listar() { return useCase.listar(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CobranzaDTO registrar(@Valid @RequestBody CrearCobranzaRequest req) { return useCase.registrar(req); }
}
