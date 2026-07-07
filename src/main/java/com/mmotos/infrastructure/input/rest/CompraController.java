package com.mmotos.infrastructure.input.rest;

import com.mmotos.application.dto.*;
import com.mmotos.application.usecase.GestionarCompraBorradorUseCase;
import com.mmotos.application.usecase.RegistrarCompraUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

    private final RegistrarCompraUseCase useCase;
    private final GestionarCompraBorradorUseCase borradorUseCase;

    public CompraController(RegistrarCompraUseCase useCase,
                            GestionarCompraBorradorUseCase borradorUseCase) {
        this.useCase         = useCase;
        this.borradorUseCase = borradorUseCase;
    }

    @GetMapping
    public List<CompraDTO> listar() {
        return useCase.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompraDTO registrar(@Valid @RequestBody CrearCompraRequest request) {
        return useCase.registrar(request);
    }

    // --- Borradores (remitos vía foto/Telegram/IA) ---

    @GetMapping("/borradores")
    public List<CompraBorradorDTO> listarBorradores() {
        return borradorUseCase.listarPendientes();
    }

    @PostMapping("/borrador")
    @ResponseStatus(HttpStatus.CREATED)
    public CompraBorradorDTO crearBorrador(@Valid @RequestBody CrearCompraBorradorRequest request) {
        return borradorUseCase.crear(request);
    }

    @PutMapping("/borradores/{id}")
    public CompraBorradorDTO actualizarBorrador(@PathVariable UUID id,
                                                 @Valid @RequestBody CrearCompraBorradorRequest request) {
        return borradorUseCase.actualizar(id, request);
    }

    @PostMapping("/borradores/{id}/confirmar")
    public CompraDTO confirmarBorrador(@PathVariable UUID id,
                                       @RequestParam(required = false) String usuarioId) {
        return borradorUseCase.confirmar(id, usuarioId);
    }

    @DeleteMapping("/borradores/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rechazarBorrador(@PathVariable UUID id) {
        borradorUseCase.rechazar(id);
    }
}
