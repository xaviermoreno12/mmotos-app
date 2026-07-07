package com.mmotos.infrastructure.input.rest;

import com.mmotos.application.dto.*;
import com.mmotos.application.usecase.GestionarFacturaBorradorUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/facturas-ia")
@PreAuthorize("hasAnyRole('CAJERO', 'DUENO')")
public class FacturaIAController {

    private final GestionarFacturaBorradorUseCase useCase;

    public FacturaIAController(GestionarFacturaBorradorUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/procesar")
    @ResponseStatus(HttpStatus.CREATED)
    public FacturaBorradorDTO procesar(@Valid @RequestBody ProcesarFacturaRequest request) {
        return useCase.procesarFoto(request.imagenBase64());
    }

    @GetMapping("/borradores")
    public List<FacturaBorradorDTO> listarBorradores() {
        return useCase.listarPendientes();
    }

    @PutMapping("/borradores/{id}")
    public FacturaBorradorDTO actualizarBorrador(@PathVariable UUID id,
                                                  @Valid @RequestBody ActualizarFacturaBorradorRequest request) {
        return useCase.actualizar(id, request);
    }

    @PostMapping("/borradores/{id}/confirmar")
    public ConfirmarFacturaResultDTO confirmarBorrador(@PathVariable UUID id,
                                                        @RequestParam String usuarioId) {
        return useCase.confirmar(id, usuarioId);
    }

    @DeleteMapping("/borradores/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rechazarBorrador(@PathVariable UUID id) {
        useCase.rechazar(id);
    }
}
