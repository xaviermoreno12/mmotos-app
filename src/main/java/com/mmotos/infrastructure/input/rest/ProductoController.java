package com.mmotos.infrastructure.input.rest;

import com.mmotos.application.dto.ActualizarProductoRequest;
import com.mmotos.application.dto.CrearProductoRequest;
import com.mmotos.application.dto.HistorialVentaDTO;
import com.mmotos.application.dto.PaginaProductosDTO;
import com.mmotos.application.dto.ProductoDTO;
import com.mmotos.application.dto.ProductoFiltroDTO;
import com.mmotos.application.usecase.ActualizarProductoUseCase;
import com.mmotos.application.usecase.BuscarProductoUseCase;
import com.mmotos.application.usecase.CrearProductoUseCase;
import com.mmotos.infrastructure.output.persistence.entity.VentaDetalleEntity;
import com.mmotos.infrastructure.output.persistence.entity.VentaEntity;
import com.mmotos.infrastructure.output.persistence.jpa.VentaDetalleJpaRepository;
import com.mmotos.infrastructure.output.persistence.jpa.VentaJpaRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final BuscarProductoUseCase buscarProductoUseCase;
    private final CrearProductoUseCase crearProductoUseCase;
    private final ActualizarProductoUseCase actualizarProductoUseCase;
    private final VentaDetalleJpaRepository ventaDetalleRepo;
    private final VentaJpaRepository ventaRepo;

    public ProductoController(BuscarProductoUseCase buscarProductoUseCase,
                              CrearProductoUseCase crearProductoUseCase,
                              ActualizarProductoUseCase actualizarProductoUseCase,
                              VentaDetalleJpaRepository ventaDetalleRepo,
                              VentaJpaRepository ventaRepo) {
        this.buscarProductoUseCase     = buscarProductoUseCase;
        this.crearProductoUseCase      = crearProductoUseCase;
        this.actualizarProductoUseCase = actualizarProductoUseCase;
        this.ventaDetalleRepo          = ventaDetalleRepo;
        this.ventaRepo                 = ventaRepo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('DUENO')")
    public ProductoDTO crear(@Valid @RequestBody CrearProductoRequest request) {
        return crearProductoUseCase.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DUENO')")
    public ProductoDTO actualizar(@PathVariable UUID id,
                                  @RequestBody ActualizarProductoRequest request) {
        return actualizarProductoUseCase.actualizar(id, request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CAJERO', 'DUENO')")
    public ProductoDTO porId(@PathVariable UUID id) {
        return buscarProductoUseCase.porId(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    @GetMapping("/sku/{sku}")
    @PreAuthorize("hasAnyRole('CAJERO', 'DUENO')")
    public ProductoDTO porSku(@PathVariable String sku) {
        return buscarProductoUseCase.porSku(sku)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SKU no encontrado: " + sku));
    }

    // Búsqueda flexible: por SKU exacto | atributo JSON @> | término fuzzy (pg_trgm)
    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('CAJERO', 'DUENO')")
    public List<ProductoDTO> buscar(
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String atributo,   // ej: {"viscosidad":"20W50"}
            @RequestParam(required = false) String termino     // ej: "110" — busca en nombre+atributos
    ) {
        return buscarProductoUseCase.buscar(new ProductoFiltroDTO(sku, nombre, atributo, termino));
    }

    @GetMapping("/todos")
    @PreAuthorize("hasAnyRole('CAJERO', 'DUENO')")
    public PaginaProductosDTO todos(
        @RequestParam(defaultValue = "0")  int pagina,
        @RequestParam(defaultValue = "50") int tamano,
        @RequestParam(defaultValue = "")   String busqueda
    ) {
        if (!busqueda.isBlank()) {
            List<ProductoDTO> lista = buscarProductoUseCase.buscar(
                new ProductoFiltroDTO(null, null, null, busqueda.trim())
            );
            return new PaginaProductosDTO(lista, 0, 1, lista.size(), true);
        }
        var page = buscarProductoUseCase.todos(pagina, tamano);
        return new PaginaProductosDTO(
            page.getContent(),
            page.getNumber(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.isLast()
        );
    }

    // ── Importacion masiva desde CSV ────────────────────────────────────────────

    @GetMapping("/plantilla-importacion")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<byte[]> descargarPlantilla() {
        String csv = "sku,nombre,precioBase,moneda,stockActual,stockMinimo,ubicacionFisica,precioCompra\n" +
                     "FILT-001,Filtro de aceite Honda 110,1250.00,ARS,10,2,Estante A1,800.00\n" +
                     "CADEN-001,Cadena transmision 428H,3800.00,ARS,5,2,Estante B3,2500.00\n";
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"plantilla-productos.csv\"")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    @PostMapping(value = "/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('DUENO')")
    public Map<String, Object> importarCsv(@RequestParam("archivo") MultipartFile archivo) {
        if (archivo.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo está vacío");

        int creados = 0, omitidos = 0;
        List<String> errores = new ArrayList<>();

        try (var reader = new BufferedReader(new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8))) {
            String linea;
            int nro = 0;
            while ((linea = reader.readLine()) != null) {
                nro++;
                if (nro == 1 || linea.isBlank()) continue; // saltar encabezado y líneas vacías
                String[] cols = linea.split(",", -1);
                if (cols.length < 4) { errores.add("Fila " + nro + ": formato incorrecto"); omitidos++; continue; }
                try {
                    String sku     = cols[0].trim();
                    String nombre  = cols[1].trim();
                    BigDecimal precio = new BigDecimal(cols[2].trim().isEmpty() ? "0.01" : cols[2].trim());
                    String moneda  = cols[3].trim().equalsIgnoreCase("USD") ? "USD" : "ARS";
                    int stock      = cols.length > 4 && !cols[4].trim().isEmpty() ? Integer.parseInt(cols[4].trim()) : 0;
                    int stockMin   = cols.length > 5 && !cols[5].trim().isEmpty() ? Integer.parseInt(cols[5].trim()) : 2;
                    String ubic    = cols.length > 6 ? cols[6].trim() : null;
                    BigDecimal pc  = cols.length > 7 && !cols[7].trim().isEmpty() ? new BigDecimal(cols[7].trim()) : precio;

                    if (sku.isEmpty() || nombre.isEmpty()) { errores.add("Fila " + nro + ": SKU o nombre vacío"); omitidos++; continue; }

                    crearProductoUseCase.crear(new CrearProductoRequest(sku, nombre, precio, moneda, stock, stockMin, ubic, pc));
                    creados++;
                } catch (Exception e) {
                    errores.add("Fila " + nro + ": " + e.getMessage());
                    omitidos++;
                }
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer el archivo: " + e.getMessage());
        }

        return Map.of("creados", creados, "omitidos", omitidos, "errores", errores);
    }

    /**
     * Actualiza PRECIO y COSTO de productos existentes a partir de un archivo XLSX
     * con columnas: ID | NOMBRE | CÓDIGO | COSTO | PRECIO | STOCK | VER
     * Matching por nombre (case-insensitive, búsqueda fuzzy).
     */
    /**
     * Columnas esperadas en el XLSX: ID | NOMBRE | CÓDIGO | COSTO | PRECIO | STOCK | VER
     * - Filas con precio <= 0 se saltan silenciosamente (el proveedor usa -1 como "sin precio").
     * - Si el producto no existe por nombre, se crea automáticamente con el CÓDIGO como SKU.
     */
    @PostMapping(value = "/actualizar-precios", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('DUENO')")
    public Map<String, Object> actualizarPrecios(@RequestParam("archivo") MultipartFile archivo) {
        if (archivo.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo está vacío");

        int actualizados = 0, creados = 0, saltados = 0;
        List<String> errores = new ArrayList<>();

        try {
            List<String[]> filas = leerXlsx(archivo.getBytes());

            for (int i = 1; i < filas.size(); i++) {
                String[] cols = filas.get(i);
                if (cols.length < 5) continue;

                String nombre    = cols[1].trim();
                String costoStr  = cols[3].trim();
                String precioStr = cols[4].trim();
                if (nombre.isEmpty() || precioStr.isEmpty()) continue;

                try {
                    BigDecimal precio = new BigDecimal(precioStr.replace(",", "."));
                    BigDecimal costo  = costoStr.isEmpty() ? null : new BigDecimal(costoStr.replace(",", "."));

                    // Saltar silenciosamente filas sin precio válido (el proveedor usa -1 como centinela)
                    if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                        saltados++;
                        continue;
                    }

                    var matches = buscarProductoUseCase.buscar(new ProductoFiltroDTO(null, null, null, nombre));

                    if (matches.isEmpty()) {
                        // Crear producto nuevo con los datos del Excel
                        String codigoRaw = cols.length > 2 ? cols[2].trim() : "";
                        String colA      = cols[0].trim();
                        // "NAN" / "nan" es el valor que Excel exporta para celdas numéricas vacías
                        boolean codigoValido = !codigoRaw.isEmpty()
                            && !codigoRaw.equalsIgnoreCase("NAN")
                            && !codigoRaw.equalsIgnoreCase("#N/A");
                        boolean colAValida = !colA.isEmpty()
                            && !colA.equalsIgnoreCase("NAN")
                            && !colA.equalsIgnoreCase("#N/A");
                        String sku = codigoValido ? codigoRaw
                                   : colAValida   ? colA
                                   : "AUTO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                        crearProductoUseCase.crear(new CrearProductoRequest(
                            sku, nombre, precio, "ARS", 0, 0, null, costo
                        ));
                        creados++;
                    } else {
                        var prod = matches.get(0);
                        actualizarProductoUseCase.actualizar(prod.id(), new ActualizarProductoRequest(
                            null, precio, null, null, null, null, null, costo
                        ));
                        actualizados++;
                    }
                } catch (Exception e) {
                    errores.add("Fila " + (i + 1) + " (" + nombre + "): " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error al procesar el archivo: " + e.getMessage());
        }

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("actualizados", actualizados);
        resultado.put("creados", creados);
        resultado.put("saltados", saltados);
        resultado.put("errores", errores.stream().limit(20).toList());
        return resultado;
    }

    /** Lee un archivo XLSX (ZIP con XML) y devuelve filas como arrays de strings. */
    private List<String[]> leerXlsx(byte[] bytes) throws Exception {
        Map<Integer, String> sharedStrings = new HashMap<>();
        byte[] sharedStringsXml = null;
        byte[] sheet1 = null;

        // Leer TODOS los bytes de cada entry antes de parsear (parse() cierra el stream)
        try (var zis = new ZipInputStream(new java.io.ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals("xl/sharedStrings.xml")) {
                    sharedStringsXml = zis.readAllBytes();
                } else if (entry.getName().equals("xl/worksheets/sheet1.xml")) {
                    sheet1 = zis.readAllBytes();
                }
                zis.closeEntry();
            }
        }

        // Parsear sharedStrings
        if (sharedStringsXml != null) {
            var dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            var doc = dbf.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(sharedStringsXml));
            var sis = doc.getElementsByTagName("si");
            for (int i = 0; i < sis.getLength(); i++) {
                var t = ((Element) sis.item(i)).getElementsByTagName("t");
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < t.getLength(); j++) sb.append(t.item(j).getTextContent());
                sharedStrings.put(i, sb.toString());
            }
        }

        if (sheet1 == null) throw new IllegalArgumentException("No se encontró sheet1 en el archivo XLSX");

        var dbf2 = DocumentBuilderFactory.newInstance();
        dbf2.setNamespaceAware(false);
        var doc = dbf2.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(sheet1));
        NodeList rows = doc.getElementsByTagName("row");

        List<String[]> result = new ArrayList<>();
        for (int r = 0; r < rows.getLength(); r++) {
            Element row = (Element) rows.item(r);
            NodeList cells = row.getElementsByTagName("c");
            // Determinar columna máxima para rellenar huecos
            int maxCol = 0;
            for (int c = 0; c < cells.getLength(); c++) {
                String ref = ((Element) cells.item(c)).getAttribute("r");
                int col = colIndex(ref);
                if (col > maxCol) maxCol = col;
            }
            String[] rowData = new String[maxCol + 1];
            Arrays.fill(rowData, "");
            for (int c = 0; c < cells.getLength(); c++) {
                Element cell = (Element) cells.item(c);
                int col = colIndex(cell.getAttribute("r"));
                NodeList vNodes = cell.getElementsByTagName("v");
                if (vNodes.getLength() == 0) continue;
                String raw = vNodes.item(0).getTextContent();
                rowData[col] = "s".equals(cell.getAttribute("t"))
                    ? sharedStrings.getOrDefault(Integer.parseInt(raw), "")
                    : raw;
            }
            result.add(rowData);
        }
        return result;
    }

    /** Convierte referencia de columna Excel (A=0, B=1, AA=26...) a índice numérico. */
    private int colIndex(String cellRef) {
        int idx = 0;
        for (char ch : cellRef.toCharArray()) {
            if (!Character.isLetter(ch)) break;
            idx = idx * 26 + (ch - 'A' + 1);
        }
        return idx - 1;
    }

    @GetMapping("/{id}/historial")
    @PreAuthorize("hasAnyRole('CAJERO', 'DUENO')")
    public List<HistorialVentaDTO> historial(@PathVariable UUID id) {
        List<VentaDetalleEntity> detalles = ventaDetalleRepo.findByProductoId(id);
        if (detalles.isEmpty()) return List.of();

        List<UUID> ventaIds = detalles.stream().map(VentaDetalleEntity::getVentaId).toList();
        Map<UUID, VentaEntity> ventas = ventaRepo.findAllById(ventaIds)
            .stream().collect(Collectors.toMap(VentaEntity::getId, v -> v));

        return detalles.stream()
            .filter(d -> ventas.containsKey(d.getVentaId()))
            .map(d -> {
                VentaEntity v = ventas.get(d.getVentaId());
                return new HistorialVentaDTO(
                    v.getId().toString(),
                    v.getNumeroTicket(),
                    v.getFechaEmision(),
                    d.getCantidad(),
                    d.getPrecioUnitarioHistorico()
                );
            })
            .sorted(Comparator.comparing(HistorialVentaDTO::fechaEmision).reversed())
            .toList();
    }
}
