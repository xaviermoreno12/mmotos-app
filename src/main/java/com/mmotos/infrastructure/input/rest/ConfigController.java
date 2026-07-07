package com.mmotos.infrastructure.input.rest;

import com.mmotos.application.dto.CotizacionRequest;
import com.mmotos.application.usecase.ActualizarCotizacionUseCase;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final ActualizarCotizacionUseCase actualizarCotizacionUseCase;

    public ConfigController(ActualizarCotizacionUseCase actualizarCotizacionUseCase) {
        this.actualizarCotizacionUseCase = actualizarCotizacionUseCase;
    }

    @PutMapping("/cotizacion")
    @PreAuthorize("hasRole('DUENO')")
    public CotizacionResponse actualizarCotizacion(@Valid @RequestBody CotizacionRequest request) {
        BigDecimal nuevo = actualizarCotizacionUseCase.ejecutar(request);
        return new CotizacionResponse(nuevo, "Cotización actualizada correctamente");
    }

    public record CotizacionResponse(BigDecimal valorDolar, String mensaje) {}
}
