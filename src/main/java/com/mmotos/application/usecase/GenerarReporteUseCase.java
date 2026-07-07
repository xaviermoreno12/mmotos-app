package com.mmotos.application.usecase;

import com.mmotos.application.dto.RentabilidadDTO;
import com.mmotos.infrastructure.output.persistence.entity.VentaDetalleEntity;
import com.mmotos.infrastructure.output.persistence.jpa.PagoJpaRepository;
import com.mmotos.infrastructure.output.persistence.jpa.ProductoJpaRepository;
import com.mmotos.infrastructure.output.persistence.jpa.VentaDetalleJpaRepository;
import com.mmotos.infrastructure.output.persistence.jpa.VentaJpaRepository;
import com.mmotos.infrastructure.output.report.ExcelReportBuilder;
import com.mmotos.infrastructure.output.report.PdfReportBuilder;
import com.mmotos.infrastructure.output.report.ReportBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GenerarReporteUseCase {

    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_HORA  = DateTimeFormatter.ofPattern("HH:mm");

    private final VentaJpaRepository ventaRepo;
    private final VentaDetalleJpaRepository detalleRepo;
    private final PagoJpaRepository  pagoRepo;
    private final ProductoJpaRepository productoRepo;
    private final ExcelReportBuilder excel;
    private final PdfReportBuilder   pdf;

    public GenerarReporteUseCase(VentaJpaRepository ventaRepo,
                                  VentaDetalleJpaRepository detalleRepo,
                                  PagoJpaRepository pagoRepo,
                                  ProductoJpaRepository productoRepo,
                                  ExcelReportBuilder excel,
                                  PdfReportBuilder pdf) {
        this.ventaRepo    = ventaRepo;
        this.detalleRepo  = detalleRepo;
        this.pagoRepo     = pagoRepo;
        this.productoRepo = productoRepo;
        this.excel = excel;
        this.pdf   = pdf;
    }

    @Transactional(readOnly = true)
    public byte[] reporteVentas(LocalDate desde, LocalDate hasta, String formato) {
        var desdeTs = desde.atStartOfDay();
        var hastaTs = hasta.plusDays(1).atStartOfDay().minusSeconds(1);
        var ventas  = ventaRepo.findByFechaEmisionBetween(desdeTs, hastaTs);

        String[] headers = { "Fecha", "Hora", "N° Ticket", "Tipo Factura",
                             "Estado Fiscal", "CUIT Cliente", "Métodos de Pago", "Total" };

        var filas = new ArrayList<Object[]>();
        var totalGeneral = BigDecimal.ZERO;
        for (var v : ventas) {
            var pagos = pagoRepo.findByVentaId(v.getId());
            var metodos = pagos.stream()
                .map(p -> p.getMetodo() + " $" + p.getMonto())
                .reduce((a, b) -> a + " | " + b).orElse("");
            filas.add(new Object[]{
                v.getFechaEmision().format(FMT_FECHA),
                v.getFechaEmision().format(FMT_HORA),
                v.getNumeroTicket(),
                v.getTipoFactura(),
                v.getEstadoFiscal(),
                v.getClienteCuit() != null ? v.getClienteCuit() : "Consumidor Final",
                metodos,
                "$" + v.getTotalVenta()
            });
            totalGeneral = totalGeneral.add(v.getTotalVenta());
        }

        String subtitulo = "Del " + desde.format(FMT_FECHA) + " al " + hasta.format(FMT_FECHA)
            + " — " + ventas.size() + " ventas";
        String[] totLabels = { "Cantidad de ventas:", "Total general:" };
        Object[] totVals   = { ventas.size(), "$" + totalGeneral };

        return builder(formato).build("Reporte de Ventas", subtitulo, headers, filas, totLabels, totVals);
    }

    @Transactional(readOnly = true)
    public byte[] reporteStockBajo(String formato) {
        var productos = productoRepo.findBajoMinimo();

        String[] headers = { "SKU", "Nombre", "Stock Actual", "Stock Mínimo", "Faltante", "Precio Base" };

        var filas = new ArrayList<Object[]>();
        for (var p : productos) {
            int faltante = p.getStockMinimo() - p.getStockActual();
            filas.add(new Object[]{
                p.getSku(),
                p.getNombre(),
                p.getStockActual(),
                p.getStockMinimo(),
                faltante,
                "$" + p.getPrecioBase() + " " + p.getMoneda()
            });
        }
        // Ordenar por faltante desc
        filas.sort((a, b) -> Integer.compare((int) b[4], (int) a[4]));

        String subtitulo = LocalDate.now().format(FMT_FECHA) + " — " + productos.size() + " productos bajo mínimo";
        String[] totLabels = { "Total productos críticos:" };
        Object[] totVals   = { productos.size() };

        return builder(formato).build("Reporte de Stock Bajo", subtitulo, headers, filas, totLabels, totVals);
    }

    @Transactional(readOnly = true)
    public byte[] reporteCaja(LocalDate fecha, String formato) {
        var desdeTs = fecha.atStartOfDay();
        var hastaTs = fecha.plusDays(1).atStartOfDay().minusSeconds(1);
        var ventas  = ventaRepo.findByFechaEmisionBetween(desdeTs, hastaTs);

        // KPI por método de pago
        var totalEfectivo    = BigDecimal.ZERO;
        var totalTarjeta     = BigDecimal.ZERO;
        var totalTransf      = BigDecimal.ZERO;
        var totalMp          = BigDecimal.ZERO;
        var totalGeneral     = BigDecimal.ZERO;

        var filas = new ArrayList<Object[]>();
        for (var v : ventas) {
            var pagos = pagoRepo.findByVentaId(v.getId());
            for (var p : pagos) {
                var m = p.getMonto();
                switch (p.getMetodo()) {
                    case "EFECTIVO"        -> totalEfectivo = totalEfectivo.add(m);
                    case "TARJETA_DEBITO",
                         "TARJETA_CREDITO" -> totalTarjeta = totalTarjeta.add(m);
                    case "TRANSFERENCIA"   -> totalTransf = totalTransf.add(m);
                    case "MERCADO_PAGO"    -> totalMp = totalMp.add(m);
                }
            }
            totalGeneral = totalGeneral.add(v.getTotalVenta());
            filas.add(new Object[]{
                v.getFechaEmision().format(FMT_HORA),
                v.getNumeroTicket(),
                v.getTipoFactura(),
                v.getEstadoFiscal(),
                "$" + v.getTotalVenta()
            });
        }

        String[] headers = { "Hora", "N° Ticket", "Tipo Factura", "Estado Fiscal", "Total" };
        String subtitulo  = fecha.format(FMT_FECHA) + " — " + ventas.size() + " ventas";
        String[] totLabels = { "Efectivo:", "Tarjeta:", "Transferencia:", "MercadoPago:", "TOTAL:" };
        Object[] totVals   = {
            "$" + totalEfectivo, "$" + totalTarjeta,
            "$" + totalTransf, "$" + totalMp, "$" + totalGeneral
        };

        return builder(formato).build("Reporte de Caja", subtitulo, headers, filas, totLabels, totVals);
    }

    @Transactional(readOnly = true)
    public byte[] reporteListaProductos(String formato) {
        var productos = productoRepo.findAllByActivoTrueOrderByStockActualAsc();
        String[] headers = { "SKU", "Nombre", "Stock Actual", "Stock Mínimo", "Ubicación", "Precio Base", "Moneda", "Precio ARS" };
        var filas = new ArrayList<Object[]>();
        for (var p : productos) {
            filas.add(new Object[]{
                p.getSku(), p.getNombre(), p.getStockActual(), p.getStockMinimo(),
                p.getUbicacionFisica() != null ? p.getUbicacionFisica() : "",
                "$" + p.getPrecioBase(), p.getMoneda(), "$" + p.getPrecioBase()
            });
        }
        String[] totLabels = { "Total productos:" };
        Object[] totVals = { productos.size() };
        return builder(formato).build("Lista de Productos", LocalDate.now().format(FMT_FECHA), headers, filas, totLabels, totVals);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> previewVentas(LocalDate desde, LocalDate hasta) {
        var desdeTs = desde.atStartOfDay();
        var hastaTs = hasta.plusDays(1).atStartOfDay().minusSeconds(1);
        var ventas  = ventaRepo.findByFechaEmisionBetween(desdeTs, hastaTs);
        var headers = List.of("Fecha", "Hora", "N° Ticket", "Tipo", "Estado", "Métodos", "Total");
        var rows = new ArrayList<List<Object>>();
        var totalGeneral = BigDecimal.ZERO;
        for (var v : ventas) {
            var pagos = pagoRepo.findByVentaId(v.getId());
            var metodos = pagos.stream().map(p -> p.getMetodo()).reduce((a,b) -> a+"|"+b).orElse("");
            rows.add(List.of(v.getFechaEmision().format(FMT_FECHA), v.getFechaEmision().format(FMT_HORA),
                v.getNumeroTicket() != null ? v.getNumeroTicket() : "", v.getTipoFactura(),
                v.getEstadoFiscal(), metodos, v.getTotalVenta()));
            totalGeneral = totalGeneral.add(v.getTotalVenta());
        }
        var result = new LinkedHashMap<String, Object>();
        result.put("titulo", "Ventas " + desde.format(FMT_FECHA) + " - " + hasta.format(FMT_FECHA));
        result.put("headers", headers);
        result.put("rows", rows);
        result.put("totalGeneral", totalGeneral);
        result.put("cantidadVentas", ventas.size());
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> previewStockBajo() {
        var productos = productoRepo.findBajoMinimo();
        var headers = List.of("SKU", "Nombre", "Stock Actual", "Stock Mínimo", "Faltante");
        var rows = new ArrayList<List<Object>>();
        for (var p : productos) {
            rows.add(List.of(p.getSku(), p.getNombre(), p.getStockActual(), p.getStockMinimo(),
                p.getStockMinimo() - p.getStockActual()));
        }
        var result = new LinkedHashMap<String, Object>();
        result.put("titulo", "Stock Bajo — " + LocalDate.now().format(FMT_FECHA));
        result.put("headers", headers);
        result.put("rows", rows);
        result.put("totalProductos", productos.size());
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> previewCaja(LocalDate fecha) {
        var desdeTs = fecha.atStartOfDay();
        var hastaTs = fecha.plusDays(1).atStartOfDay().minusSeconds(1);
        var ventas  = ventaRepo.findByFechaEmisionBetween(desdeTs, hastaTs);
        var headers = List.of("Hora", "N° Ticket", "Tipo", "Estado", "Total");
        var rows = new ArrayList<List<Object>>();
        var totalGeneral = BigDecimal.ZERO;
        for (var v : ventas) {
            rows.add(List.of(v.getFechaEmision().format(FMT_HORA),
                v.getNumeroTicket() != null ? v.getNumeroTicket() : "",
                v.getTipoFactura(), v.getEstadoFiscal(), v.getTotalVenta()));
            totalGeneral = totalGeneral.add(v.getTotalVenta());
        }
        var result = new LinkedHashMap<String, Object>();
        result.put("titulo", "Caja " + fecha.format(FMT_FECHA));
        result.put("headers", headers);
        result.put("rows", rows);
        result.put("totalGeneral", totalGeneral);
        result.put("cantidadVentas", ventas.size());
        return result;
    }

    private ReportBuilder builder(String formato) {
        return "pdf".equalsIgnoreCase(formato) ? pdf : excel;
    }

    @Transactional(readOnly = true)
    public RentabilidadDTO reporteRentabilidad(LocalDate desde, LocalDate hasta) {
        var desdeTs = desde.atStartOfDay();
        var hastaTs = hasta.plusDays(1).atStartOfDay().minusSeconds(1);
        var ventas  = ventaRepo.findByFechaEmisionBetween(desdeTs, hastaTs);

        var totalVentas = BigDecimal.ZERO;
        var totalCostos = BigDecimal.ZERO;
        long cantidadVentas = 0;

        for (var v : ventas) {
            if (v.isAnulada()) continue;
            totalVentas = totalVentas.add(v.getTotalVenta());
            cantidadVentas++;

            var detalles = detalleRepo.findByVentaId(v.getId());
            for (var d : detalles) {
                if (d.getProductoId() != null) {
                    var producto = productoRepo.findById(d.getProductoId());
                    if (producto.isPresent() && producto.get().getPrecioCompra() != null) {
                        totalCostos = totalCostos.add(
                            producto.get().getPrecioCompra().multiply(BigDecimal.valueOf(d.getCantidad()))
                        );
                    }
                }
            }
        }

        var beneficio = totalVentas.subtract(totalCostos);
        return new RentabilidadDTO(totalVentas, totalCostos, beneficio, cantidadVentas);
    }
}
