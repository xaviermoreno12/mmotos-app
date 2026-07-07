package com.mmotos.application.usecase;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmotos.application.dto.*;
import com.mmotos.application.enums.EstadoBorrador;
import com.mmotos.infrastructure.output.ai.OpenAiExtractionService;
import com.mmotos.infrastructure.output.persistence.entity.FacturaBorradorEntity;
import com.mmotos.infrastructure.output.persistence.jpa.FacturaBorradorJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class GestionarFacturaBorradorUseCase {

    private static final String CATEGORIA_DEFAULT = "Otros";
    private static final String MONEDA_DEFAULT = "ARS";

    private final FacturaBorradorJpaRepository repo;
    private final OpenAiExtractionService openAiService;
    private final BuscarProductoUseCase buscarProductoUseCase;
    private final CrearProductoUseCase crearProductoUseCase;
    private final RegistrarCompraUseCase registrarCompraUseCase;
    private final GestionarGastosUseCase gestionarGastosUseCase;
    private final ObjectMapper mapper;

    public GestionarFacturaBorradorUseCase(FacturaBorradorJpaRepository repo,
                                            OpenAiExtractionService openAiService,
                                            BuscarProductoUseCase buscarProductoUseCase,
                                            CrearProductoUseCase crearProductoUseCase,
                                            RegistrarCompraUseCase registrarCompraUseCase,
                                            GestionarGastosUseCase gestionarGastosUseCase,
                                            ObjectMapper mapper) {
        this.repo = repo;
        this.openAiService = openAiService;
        this.buscarProductoUseCase = buscarProductoUseCase;
        this.crearProductoUseCase = crearProductoUseCase;
        this.registrarCompraUseCase = registrarCompraUseCase;
        this.gestionarGastosUseCase = gestionarGastosUseCase;
        this.mapper = mapper;
    }

    @Transactional
    public FacturaBorradorDTO procesarFoto(String imagenBase64) {
        try {
            Base64.getDecoder().decode(imagenBase64);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen no es un base64 válido");
        }

        FacturaExtraidaDTO extraida = openAiService.extraerDesdeFoto(imagenBase64);

        List<LineaFacturaDTO> lineas = new ArrayList<>();
        for (var lineaExtraida : extraida.lineas()) {
            lineas.add(resolverLinea(lineaExtraida));
        }

        String categoria = extraida.categoria() != null && !extraida.categoria().isBlank()
            ? extraida.categoria() : CATEGORIA_DEFAULT;
        String estadoPago = extraida.estadoPago() != null && !extraida.estadoPago().isBlank()
            ? extraida.estadoPago() : "Pendiente";

        try {
            var entity = new FacturaBorradorEntity(
                extraida.fecha(), extraida.proveedor(), extraida.cuit(), extraida.numeroFactura(),
                imagenBase64, null, extraida.total(), categoria, estadoPago,
                mapper.writeValueAsString(lineas), null
            );
            return toDTO(repo.save(entity));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al guardar el borrador de factura");
        }
    }

    private LineaFacturaDTO resolverLinea(FacturaExtraidaDTO.LineaExtraidaDTO linea) {
        int cantidad = linea.cantidad() > 0 ? linea.cantidad() : 1;
        double precioUnitario = linea.precioUnitario() != null ? linea.precioUnitario().doubleValue() : 0.0;

        List<ProductoDTO> matches = buscarProductoUseCase.buscar(
            new ProductoFiltroDTO(null, null, null, linea.nombre()));

        if (!matches.isEmpty()) {
            ProductoDTO producto = matches.get(0);
            return new LineaFacturaDTO(
                producto.id().toString(), false, producto.sku(), linea.nombre(),
                cantidad, precioUnitario, producto.ubicacionFisica(),
                producto.stockMinimo(), producto.moneda()
            );
        }

        return new LineaFacturaDTO(
            null, true, generarSkuSugerido(linea.nombre()), linea.nombre(),
            cantidad, precioUnitario, null, 2, MONEDA_DEFAULT
        );
    }

    private String generarSkuSugerido(String nombre) {
        String base = nombre == null ? "" : nombre.toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (base.length() > 8) base = base.substring(0, 8);
        if (base.isBlank()) base = "PROD";
        // C3: UUID garantiza unicidad (16^6 ~ 16M combinaciones vs 9000 de Math.random)
        String sufijo = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "NUEVO-" + base + "-" + sufijo;
    }

    @Transactional(readOnly = true)
    public List<FacturaBorradorDTO> listarPendientes() {
        return repo.findByEstadoOrderByFechaRecepcionDesc(EstadoBorrador.PENDIENTE.name())
            .stream().map(this::toDTO).toList();
    }

    @Transactional
    public FacturaBorradorDTO actualizar(UUID id, ActualizarFacturaBorradorRequest req) {
        var entity = repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Borrador no encontrado"));
        if (!EstadoBorrador.PENDIENTE.name().equals(entity.getEstado()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El borrador ya fue procesado");

        // M4: Solo sobreescribir si el valor del request no es null (edicion parcial segura)
        if (req.fechaFactura()    != null) entity.setFechaFactura(req.fechaFactura());
        if (req.proveedorNombre() != null) entity.setProveedorNombre(req.proveedorNombre());
        if (req.cuit()            != null) entity.setCuit(req.cuit());
        if (req.numeroFactura()   != null) entity.setNumeroFactura(req.numeroFactura());
        if (req.montoTotal()      != null) entity.setMontoTotal(req.montoTotal());
        if (req.categoriaGasto()  != null) entity.setCategoriaGasto(req.categoriaGasto());
        if (req.estadoPago()      != null) entity.setEstadoPago(req.estadoPago());
        if (req.observaciones()   != null) entity.setObservaciones(req.observaciones());
        try {
            entity.setLineas(mapper.writeValueAsString(req.lineas()));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al actualizar borrador");
        }

        return toDTO(repo.save(entity));
    }

    @Transactional
    public ConfirmarFacturaResultDTO confirmar(UUID id, String usuarioId) {
        var entity = repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Borrador no encontrado"));
        if (!EstadoBorrador.PENDIENTE.name().equals(entity.getEstado()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El borrador ya fue procesado");

        List<LineaFacturaDTO> lineas = parseLineas(entity.getLineas());

        int productosCreados = 0;
        int productosActualizados = 0;
        List<LineaCompraRequest> lineasCompra = new ArrayList<>();

        for (var linea : lineas) {
            UUID productoId;
            String sku;
            if (linea.productoId() == null || linea.esNuevo()) {
                // C2: No forzar precio a $0.01 — exigir corrección manual
                if (linea.precioUnitario() <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La linea '" + linea.nombre() + "' tiene precio 0. Edita el borrador antes de confirmar.");
                }
                BigDecimal precio = BigDecimal.valueOf(linea.precioUnitario());
                String moneda = "USD".equals(linea.moneda()) ? "USD" : "ARS";
                var creado = crearProductoUseCase.crear(new CrearProductoRequest(
                    linea.sku(), linea.nombre(), precio, moneda, 0,
                    Math.max(linea.stockMinimo(), 0), linea.ubicacionFisica(), precio
                ));
                productoId = creado.id();
                sku = creado.sku();
                productosCreados++;
            } else {
                // A7: Capturar UUID malformado explicitamente
                try {
                    productoId = UUID.fromString(linea.productoId());
                } catch (IllegalArgumentException ex) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "ID de producto invalido: " + linea.productoId());
                }
                sku = linea.sku() != null ? linea.sku() : linea.nombre();
                productosActualizados++;
            }

            lineasCompra.add(new LineaCompraRequest(
                productoId, sku, linea.nombre(), linea.cantidad(),
                BigDecimal.valueOf(linea.precioUnitario())
            ));
        }

        var compraRequest = new CrearCompraRequest(
            null,
            entity.getProveedorNombre() != null ? entity.getProveedorNombre() : "Sin proveedor",
            entity.getNumeroFactura(),
            "EFECTIVO",
            lineasCompra,
            entity.getObservaciones(),
            usuarioId
        );
        var compra = registrarCompraUseCase.registrar(compraRequest);

        BigDecimal monto = entity.getMontoTotal() != null ? entity.getMontoTotal()
            : lineasCompra.stream()
                .map(l -> l.precioUnitario().multiply(BigDecimal.valueOf(l.cantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // C2: Si el monto sigue siendo 0, rechazar con error claro
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "El monto total de la factura es 0. Edita el campo 'Monto Total' antes de confirmar.");
        }

        String categoria = entity.getCategoriaGasto() != null && !entity.getCategoriaGasto().isBlank()
            ? entity.getCategoriaGasto() : CATEGORIA_DEFAULT;

        StringBuilder observaciones = new StringBuilder("Factura ");
        if (entity.getNumeroFactura() != null) observaciones.append(entity.getNumeroFactura()).append(" ");
        if (entity.getCuit() != null) observaciones.append("- CUIT ").append(entity.getCuit()).append(" ");
        if (entity.getFechaFactura() != null) observaciones.append("- Fecha ").append(entity.getFechaFactura()).append(" ");
        observaciones.append("- Estado de pago: ").append(entity.getEstadoPago());

        var gasto = gestionarGastosUseCase.crear(new CrearGastoRequest(
            "Factura " + (entity.getNumeroFactura() != null ? entity.getNumeroFactura() : "") +
                " - " + (entity.getProveedorNombre() != null ? entity.getProveedorNombre() : "Sin proveedor"),
            categoria, monto, "EFECTIVO", observaciones.toString(), usuarioId
        ));

        entity.setEstado(EstadoBorrador.CONFIRMADO.name());
        repo.save(entity);

        return new ConfirmarFacturaResultDTO(compra.id(), gasto.id(), productosCreados, productosActualizados);
    }

    @Transactional
    public void rechazar(UUID id) {
        var entity = repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Borrador no encontrado"));
        entity.setEstado(EstadoBorrador.RECHAZADO.name());
        repo.save(entity);
    }

    private List<LineaFacturaDTO> parseLineas(String json) {
        try {
            return mapper.readValue(json, new TypeReference<List<LineaFacturaDTO>>() {});
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al parsear líneas del borrador");
        }
    }

    private FacturaBorradorDTO toDTO(FacturaBorradorEntity e) {
        // C4: No ignorar el error — JSON corrupto en BD debe ser visible, no silencioso
        List<LineaFacturaDTO> lineas = parseLineas(e.getLineas());

        return new FacturaBorradorDTO(
            e.getId().toString(),
            e.getFechaRecepcion().toString(),
            e.getFechaFactura(),
            e.getProveedorNombre(),
            e.getCuit(),
            e.getNumeroFactura(),
            e.getImagenBase64(),
            e.getTextoOcr(),
            e.getMontoTotal(),
            e.getCategoriaGasto(),
            e.getEstadoPago(),
            lineas,
            e.getEstado(),
            e.getObservaciones()
        );
    }
}
