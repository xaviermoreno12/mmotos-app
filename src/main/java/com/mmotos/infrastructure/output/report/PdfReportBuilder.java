package com.mmotos.infrastructure.output.report;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Component("pdfReportBuilder")
public class PdfReportBuilder implements ReportBuilder {

    private static final Color HEADER_BG  = new Color(28, 37, 65);
    private static final Color ALT_BG     = new Color(240, 240, 245);
    private static final Color TOTAL_BG   = new Color(220, 230, 255);

    @Override
    public byte[] build(String titulo, String subtitulo, String[] headers,
                        List<Object[]> filas, String[] totalesLabels, Object[] totalesValores) {
        var out = new ByteArrayOutputStream();
        var doc = new Document(PageSize.A4.rotate(), 30, 30, 40, 40);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            // Título
            var fontTitulo = new Font(Font.HELVETICA, 16, Font.BOLD, Color.BLACK);
            var pTitulo = new Paragraph(titulo, fontTitulo);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            doc.add(pTitulo);

            if (subtitulo != null && !subtitulo.isBlank()) {
                var fontSub = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.GRAY);
                var pSub = new Paragraph(subtitulo, fontSub);
                pSub.setAlignment(Element.ALIGN_CENTER);
                doc.add(pSub);
            }
            doc.add(new Paragraph(" "));

            // Tabla de datos
            var table = new PdfPTable(headers.length);
            table.setWidthPercentage(100);

            // Headers
            var fontHeader = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
            for (String h : headers) {
                var cell = new PdfPCell(new Phrase(h, fontHeader));
                cell.setBackgroundColor(HEADER_BG);
                cell.setPadding(5);
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);
            }

            // Datos
            var fontData = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.BLACK);
            for (int i = 0; i < filas.size(); i++) {
                var fila = filas.get(i);
                var bg = (i % 2 == 1) ? ALT_BG : Color.WHITE;
                for (Object val : fila) {
                    var cell = new PdfPCell(new Phrase(val != null ? val.toString() : "", fontData));
                    cell.setBackgroundColor(bg);
                    cell.setPadding(4);
                    cell.setBorderColor(new Color(200, 200, 210));
                    cell.setBorderWidth(0.3f);
                    table.addCell(cell);
                }
            }
            doc.add(table);

            // Totales
            if (totalesLabels != null && totalesValores != null) {
                doc.add(new Paragraph(" "));
                var totTable = new PdfPTable(2);
                totTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
                totTable.setWidthPercentage(35);
                var fontTot = new Font(Font.HELVETICA, 9, Font.BOLD, Color.BLACK);
                for (int i = 0; i < totalesLabels.length; i++) {
                    var lCell = new PdfPCell(new Phrase(totalesLabels[i], fontTot));
                    lCell.setBackgroundColor(TOTAL_BG);
                    lCell.setPadding(4);
                    lCell.setBorder(Rectangle.BOX);
                    totTable.addCell(lCell);
                    var vCell = new PdfPCell(new Phrase(
                        totalesValores[i] != null ? totalesValores[i].toString() : "", fontTot));
                    vCell.setBackgroundColor(TOTAL_BG);
                    vCell.setPadding(4);
                    vCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    vCell.setBorder(Rectangle.BOX);
                    totTable.addCell(vCell);
                }
                doc.add(totTable);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF: " + e.getMessage(), e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }
}
