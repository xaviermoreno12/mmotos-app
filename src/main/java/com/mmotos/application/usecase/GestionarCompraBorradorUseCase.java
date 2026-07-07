package com.mmotos.application.usecase;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmotos.application.dto.*;
import com.mmotos.infrastructure.output.persistence.entity.CompraBorradorEntity;
import com.mmotos.infrastructure.output.persistence.jpa.CompraBorradorJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class GestionarCompraBorradorUseCase {

    private final CompraBorradorJpaRepository repo;
    private final RegistrarCompraUseCase compraUseCase;
    private final ObjectMapper mapper;

    public GestionarCompraBorradorUseCase(CompraBorradorJpaRepository repo,
                                           RegistrarCompraUseCase compraUseCase,
                                           ObjectMapper mapper) {
        this.repo = repo;
        this.compraUseCase = compraUseCase;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<CompraBorradorDTO> listarPendientes() {
        return repo.findByEstadoOrderByFechaRecepcionDesc("PENDIENTE")
                   .stream().map(this::toDTO).toList();
    }

    @Transactional
    public CompraBorradorDTO crear(CrearCompraBorradorRequest req) {
        try {
            String lineasJson = mapper.writeValueAsString(req.lineas());
            var entity = new CompraBorradorEntity(
                req.proveedorNombre(), req.numeroRemito(),
                req.imagenBase64(), lineasJson, req.observaciones()
            );
            return toDTO(repo.save(entity));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al guardar borrador");
        }
    }

    @Transactional
    public CompraBorradorDTO actualizar(UUID id, CrearCompraBorradorRequest req) {
        var entity = repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Borrador no encontrado"));
        if (!"PENDIENTE".equals(entity.getEstado()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El borrador ya fue procesado");
        try {
            entity.setLineas(mapper.writeValueAsString(req.lineas()));
            return toDTO(repo.save(entity));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al actualizar borrador");
        }
    }

    @Transactional
    public CompraDTO confirmar(UUID id, String usuarioId) {
        var entity = repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Borrador no encontrado"));
        if (!"PENDIENTE".equals(entity.getEstado()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El borrador ya fue procesado");

        List<CrearCompraBorradorRequest.LineaBorradorRequest> lineas = parseLineas(entity.getLineas());

        var compraRequest = new CrearCompraRequest(
            null,
            entity.getProveedorNombre() != null ? entity.getProveedorNombre() : "Sin proveedor",
            entity.getNumeroRemito(),
            "EFECTIVO",
            lineas.stream().map(l -> new LineaCompraRequest(
                l.productoId() != null ? UUID.fromString(l.productoId()) : null,
                l.sku() != null ? l.sku() : l.nombre(),
                l.nombre(),
                l.cantidad(),
                BigDecimal.valueOf(l.precioUnitario())
            )).toList(),
            entity.getObservaciones(),
            usuarioId
        );

        entity.setEstado("CONFIRMADO");
        repo.save(entity);
        return compraUseCase.registrar(compraRequest);
    }

    @Transactional
    public void rechazar(UUID id) {
        var entity = repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Borrador no encontrado"));
        entity.setEstado("RECHAZADO");
        repo.save(entity);
    }

    private List<CrearCompraBorradorRequest.LineaBorradorRequest> parseLineas(String json) {
        try {
            return mapper.readValue(json,
                new TypeReference<List<CrearCompraBorradorRequest.LineaBorradorRequest>>() {});
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al parsear líneas del borrador");
        }
    }

    private CompraBorradorDTO toDTO(CompraBorradorEntity e) {
        List<CompraBorradorDTO.LineaBorradorDTO> lineas = List.of();
        try {
            List<CrearCompraBorradorRequest.LineaBorradorRequest> raw = parseLineas(e.getLineas());
            lineas = raw.stream().map(l -> new CompraBorradorDTO.LineaBorradorDTO(
                l.productoId(), l.sku(), l.nombre(), l.cantidad(), l.precioUnitario()
            )).toList();
        } catch (Exception ignored) {}

        return new CompraBorradorDTO(
            e.getId().toString(),
            e.getFechaRecepcion().toString(),
            e.getProveedorNombre(),
            e.getNumeroRemito(),
            e.getImagenBase64(),
            lineas,
            e.getEstado(),
            e.getObservaciones()
        );
    }
}
