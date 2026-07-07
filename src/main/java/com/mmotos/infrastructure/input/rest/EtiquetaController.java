package com.mmotos.infrastructure.input.rest;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.mmotos.infrastructure.output.persistence.jpa.ProductoJpaRepository;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.UUID;

@RestController
@RequestMapping("/api/etiquetas")
public class EtiquetaController {

    private final ProductoJpaRepository productoRepo;

    public EtiquetaController(ProductoJpaRepository productoRepo) {
        this.productoRepo = productoRepo;
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> generarEtiquetas(@RequestParam String ids) {
        var uuids = Arrays.stream(ids.split(","))
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .map(UUID::fromString)
            .toList();

        var productos = productoRepo.findAllById(uuids);

        var out = new ByteArrayOutputStream();
        // A4 apaisado, 4 columnas x 8 filas = 32 etiquetas por página
        float etqW = 140f;
        float etqH = 100f;
        var doc = new Document(PageSize.A4, 10, 10, 15, 15);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();
            var table = new PdfPTable(4);
            table.setWidthPercentage(100);

            var fontNombre = new Font(Font.HELVETICA, 9, Font.BOLD, Color.BLACK);
            var fontSku    = new Font(Font.HELVETICA, 7, Font.NORMAL, Color.DARK_GRAY);
            var fontPrecio = new Font(Font.HELVETICA, 14, Font.BOLD, new Color(200, 0, 0));

            for (var p : productos) {
                var cell = new PdfPCell();
                cell.setFixedHeight(etqH);
                cell.setPadding(6);
                cell.setBorderColor(new Color(180, 180, 180));
                cell.setBorderWidth(0.5f);

                var nombre = new Paragraph(p.getNombre(), fontNombre);
                nombre.setAlignment(Element.ALIGN_CENTER);
                cell.addElement(nombre);

                var sku = new Paragraph("SKU: " + p.getSku(), fontSku);
                sku.setAlignment(Element.ALIGN_CENTER);
                cell.addElement(sku);

                var precio = new Paragraph("$" + p.getPrecioBase(), fontPrecio);
                precio.setAlignment(Element.ALIGN_CENTER);
                precio.setSpacingBefore(4);
                cell.addElement(precio);

                table.addCell(cell);
            }
            // Rellena celdas vacías si la última fila no está completa
            int resto = productos.size() % 4;
            if (resto > 0) {
                for (int i = resto; i < 4; i++) {
                    var empty = new PdfPCell();
                    empty.setFixedHeight(etqH);
                    empty.setBorder(Rectangle.NO_BORDER);
                    table.addCell(empty);
                }
            }
            doc.add(table);
        } catch (Exception e) {
            throw new RuntimeException("Error generando etiquetas: " + e.getMessage(), e);
        } finally {
            doc.close();
        }

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("etiquetas.pdf").build());
        return ResponseEntity.ok().headers(headers).body(out.toByteArray());
    }
}
