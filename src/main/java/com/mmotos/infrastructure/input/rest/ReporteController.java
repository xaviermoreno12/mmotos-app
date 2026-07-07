package com.mmotos.infrastructure.input.rest;

import com.mmotos.application.usecase.GenerarReporteUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final GenerarReporteUseCase useCase;

    public ReporteController(GenerarReporteUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/ventas")
    public ResponseEntity<byte[]> ventas(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
        @RequestParam(defaultValue = "xlsx") String formato
    ) {
        var bytes = useCase.reporteVentas(desde, hasta, formato);
        return buildResponse(bytes, "ventas-" + desde.format(FMT) + "-" + hasta.format(FMT), formato);
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<byte[]> stockBajo(
        @RequestParam(defaultValue = "xlsx") String formato
    ) {
        var bytes = useCase.reporteStockBajo(formato);
        return buildResponse(bytes, "stock-bajo-" + LocalDate.now().format(FMT), formato);
    }

    @GetMapping("/lista-productos")
    public ResponseEntity<byte[]> listaProductos(
        @RequestParam(defaultValue = "xlsx") String formato
    ) {
        var bytes = useCase.reporteListaProductos(formato);
        return buildResponse(bytes, "lista-productos-" + LocalDate.now().format(FMT), formato);
    }

    @GetMapping("/ventas/preview")
    public Map<String, Object> ventasPreview(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        return useCase.previewVentas(desde, hasta);
    }

    @GetMapping("/stock-bajo/preview")
    public Map<String, Object> stockBajoPreview() {
        return useCase.previewStockBajo();
    }

    @GetMapping("/caja/preview")
    public Map<String, Object> cajaPreview(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return useCase.previewCaja(fecha != null ? fecha : LocalDate.now());
    }

    @GetMapping("/caja")
    public ResponseEntity<byte[]> caja(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
        @RequestParam(defaultValue = "xlsx") String formato
    ) {
        var fechaReporte = fecha != null ? fecha : LocalDate.now();
        var bytes = useCase.reporteCaja(fechaReporte, formato);
        return buildResponse(bytes, "caja-" + fechaReporte.format(FMT), formato);
    }

    private ResponseEntity<byte[]> buildResponse(byte[] bytes, String nombre, String formato) {
        boolean isPdf = "pdf".equalsIgnoreCase(formato);
        String ext = isPdf ? ".pdf" : ".xlsx";
        MediaType type = isPdf
            ? MediaType.APPLICATION_PDF
            : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        var headers = new HttpHeaders();
        headers.setContentType(type);
        headers.setContentDisposition(
            ContentDisposition.attachment().filename(nombre + ext).build()
        );
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
