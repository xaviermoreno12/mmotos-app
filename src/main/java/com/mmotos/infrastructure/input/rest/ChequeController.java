package com.mmotos.infrastructure.input.rest;

import com.mmotos.application.dto.ChequeDTO;
import com.mmotos.application.dto.CrearChequeRequest;
import com.mmotos.application.usecase.GestionarChequesUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cheques")
public class ChequeController {

    private final GestionarChequesUseCase useCase;

    public ChequeController(GestionarChequesUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public List<ChequeDTO> listar(@RequestParam(required = false) String tipo) { return useCase.listar(tipo); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChequeDTO crear(@Valid @RequestBody CrearChequeRequest req) { return useCase.crear(req); }

    @PatchMapping("/{id}/estado")
    public ChequeDTO cambiarEstado(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return useCase.cambiarEstado(id, body.get("estado"));
    }
}
