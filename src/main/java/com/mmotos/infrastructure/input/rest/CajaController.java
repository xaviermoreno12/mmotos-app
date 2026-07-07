package com.mmotos.infrastructure.input.rest;

import com.mmotos.application.dto.*;
import com.mmotos.application.usecase.ConsultarVentasUseCase;
import com.mmotos.application.usecase.GestionarCajaUseCase;
import com.mmotos.infrastructure.config.JwtService;
import com.mmotos.infrastructure.output.persistence.jpa.UsuarioJpaRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/caja")
public class CajaController {

    private final ConsultarVentasUseCase consultarVentasUseCase;
    private final GestionarCajaUseCase gestionarCajaUseCase;
    private final JwtService jwtService;
    private final UsuarioJpaRepository usuarioRepo;

    public CajaController(ConsultarVentasUseCase consultarVentasUseCase,
                          GestionarCajaUseCase gestionarCajaUseCase,
                          JwtService jwtService,
                          UsuarioJpaRepository usuarioRepo) {
        this.consultarVentasUseCase = consultarVentasUseCase;
        this.gestionarCajaUseCase   = gestionarCajaUseCase;
        this.jwtService             = jwtService;
        this.usuarioRepo            = usuarioRepo;
    }

    /**
     * GET /api/caja/resumen — resumen diario (legacy, basado en fecha)
     */
    @GetMapping("/resumen")
    @PreAuthorize("hasAnyRole('CAJERO', 'DUENO')")
    public CajaResumenDTO resumenDiario(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        if (fecha == null) fecha = LocalDate.now();
        return consultarVentasUseCase.resumenCaja(fecha);
    }

    /**
     * GET /api/caja/activa — devuelve la caja abierta actual con resumen
     */
    @GetMapping("/activa")
    @PreAuthorize("hasAnyRole('CAJERO', 'DUENO')")
    public ResponseEntity<CajaDTO> cajaActiva() {
        CajaDTO caja = gestionarCajaUseCase.obtenerCajaActiva();
        if (caja == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(caja);
    }

    /**
     * POST /api/caja/abrir — abre una nueva caja
     */
    @PostMapping("/abrir")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CAJERO', 'DUENO')")
    public CajaDTO abrirCaja(@Valid @RequestBody AbrirCajaRequest request,
                              HttpServletRequest httpRequest) {
        UUID usuarioId = extractUsuarioId(httpRequest);
        return gestionarCajaUseCase.abrirCaja(usuarioId, request);
    }

    /**
     * POST /api/caja/cerrar — cierra la caja activa
     */
    @PostMapping("/cerrar")
    @PreAuthorize("hasAnyRole('CAJERO', 'DUENO')")
    public CajaDTO cerrarCaja(@Valid @RequestBody CerrarCajaRequest request,
                               HttpServletRequest httpRequest) {
        UUID usuarioId = extractUsuarioId(httpRequest);
        return gestionarCajaUseCase.cerrarCaja(usuarioId, request);
    }

    /**
     * GET /api/caja/historial — últimas cajas cerradas
     */
    @GetMapping("/historial")
    @PreAuthorize("hasRole('DUENO')")
    public List<CajaDTO> historial(@RequestParam(defaultValue = "30") int dias) {
        return gestionarCajaUseCase.historial(dias);
    }

    // ======================== HELPER ========================

    private UUID extractUsuarioId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            // Intentar obtener userId del claim
            try {
                String userId = jwtService.extractClaim(token, claims -> claims.get("userId", String.class));
                if (userId != null) {
                    return UUID.fromString(userId);
                }
            } catch (Exception ignored) {}
            // Fallback: buscar por username
            String username = jwtService.extractUsername(token);
            return usuarioRepo.findByUsernameAndActivoTrue(username)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + username))
                .getId();
        }
        throw new IllegalStateException("No se pudo determinar el usuario del token JWT.");
    }
}
