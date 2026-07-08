package com.mmotos.infrastructure.input.rest;

import com.mmotos.application.dto.AnularVentaRequest;
import com.mmotos.application.dto.ModificarVentaRequest;
import com.mmotos.application.dto.VentaDetalleCompletoDTO;
import com.mmotos.application.dto.VentaListDTO;
import com.mmotos.application.dto.VentaRequest;
import com.mmotos.application.dto.VentaResponse;
import com.mmotos.application.usecase.AnularVentaUseCase;
import com.mmotos.application.usecase.ConsultarVentasUseCase;
import com.mmotos.application.usecase.ModificarVentaUseCase;
import com.mmotos.application.usecase.RealizarVentaUseCase;
import com.mmotos.infrastructure.output.persistence.jpa.UsuarioJpaRepository;
import com.mmotos.infrastructure.output.persistence.jpa.VentaJpaRepository;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final RealizarVentaUseCase realizarVentaUseCase;
    private final ConsultarVentasUseCase consultarVentasUseCase;
    private final AnularVentaUseCase anularVentaUseCase;
    private final ModificarVentaUseCase modificarVentaUseCase;
    private final UsuarioJpaRepository usuarioRepo;
    private final VentaJpaRepository ventaRepo;
    private final FacturaPdfService facturaPdfService;

    public VentaController(RealizarVentaUseCase realizarVentaUseCase,
                           ConsultarVentasUseCase consultarVentasUseCase,
                           AnularVentaUseCase anularVentaUseCase,
                           ModificarVentaUseCase modificarVentaUseCase,
                           UsuarioJpaRepository usuarioRepo,
                           VentaJpaRepository ventaRepo,
                           FacturaPdfService facturaPdfService) {
        this.realizarVentaUseCase   = realizarVentaUseCase;
        this.consultarVentasUseCase = consultarVentasUseCase;
        this.anularVentaUseCase     = anularVentaUseCase;
        this.modificarVentaUseCase  = modificarVentaUseCase;
        this.usuarioRepo            = usuarioRepo;
        this.ventaRepo              = ventaRepo;
        this.facturaPdfService      = facturaPdfService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CAJERO', 'DUENO')")
    public VentaResponse realizarVenta(@Valid @RequestBody VentaRequest request) {
        return realizarVentaUseCase.ejecutar(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CAJERO', 'DUENO')")
    public List<VentaListDTO> listarVentas(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        LocalDate hoy = LocalDate.now();
        if (desde == null) desde = hoy;
        if (hasta == null) hasta = hoy;
        return consultarVentasUseCase.listarVentas(desde, hasta);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CAJERO', 'DUENO')")
    public VentaDetalleCompletoDTO porId(@PathVariable UUID id) {
        return consultarVentasUseCase.porId(id);
    }

    @PatchMapping("/{id}/anular")
    @PreAuthorize("hasAnyRole('CAJERO', 'DUENO')")
    public ResponseEntity<Void> anular(@PathVariable UUID id,
                                        @Valid @RequestBody AnularVentaRequest req) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID usuarioId = usuarioRepo.findByUsernameAndActivoTrue(username)
            .map(u -> u.getId())
            .orElse(null);
        anularVentaUseCase.ejecutar(id, req.motivo(), usuarioId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/modificar")
    @PreAuthorize("hasAnyRole('CAJERO', 'DUENO')")
    public VentaDetalleCompletoDTO modificar(@PathVariable UUID id,
                                              @Valid @RequestBody ModificarVentaRequest req) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID usuarioId = usuarioRepo.findByUsernameAndActivoTrue(username)
            .map(u -> u.getId())
            .orElse(null);
        return modificarVentaUseCase.ejecutar(id, req, usuarioId);
    }

    @GetMapping(value = "/{id}/factura-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('CAJERO', 'DUENO')")
    public ResponseEntity<byte[]> facturaPdf(@PathVariable UUID id) {
        VentaDetalleCompletoDTO detalle = consultarVentasUseCase.porId(id);
        String tipoFactura = ventaRepo.findById(id)
            .map(v -> v.getTipoFactura())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta no encontrada"));
        byte[] pdf = facturaPdfService.generar(detalle, tipoFactura);
        String filename = "ticket-" + (detalle.numeroTicket() != null ? detalle.numeroTicket() : id.toString().substring(0, 8)) + ".pdf";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
