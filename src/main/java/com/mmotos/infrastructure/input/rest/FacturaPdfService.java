package com.mmotos.infrastructure.input.rest;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.mmotos.application.dto.LineaTicketDTO;
import com.mmotos.application.dto.PagoTicketDTO;
import com.mmotos.application.dto.VentaDetalleCompletoDTO;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class FacturaPdfService {

    private static final Color COL_DARK  = new Color(15, 15, 25);
    private static final Color COL_GRAY  = new Color(80, 80, 90);
    private static final Color COL_BG    = new Color(245, 245, 250);
    private static final Color COL_BLUE  = new Color(59, 130, 246);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat MONEY_FMT = NumberFormat.getNumberInstance(new Locale("es", "AR"));

    static {
        MONEY_FMT.setMinimumFractionDigits(2);
        MONEY_FMT.setMaximumFractionDigits(2);
    }

    public byte[] generar(VentaDetalleCompletoDTO v, String tipoFactura) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();
            buildContent(doc, v, tipoFactura);
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF: " + e.getMessage(), e);
        } finally {
            if (doc.isOpen()) doc.close();
        }
        return out.toByteArray();
    }

    private void buildContent(Document doc, VentaDetalleCompletoDTO v, String tipoFactura) throws Exception {

        Font fTitle   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, COL_DARK);
        Font fSub     = FontFactory.getFont(FontFactory.HELVETICA, 10, COL_GRAY);
        Font fHead    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        Font fBodyB   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COL_DARK);
        Font fBody    = FontFactory.getFont(FontFactory.HELVETICA, 9, COL_DARK);
        Font fSmall   = FontFactory.getFont(FontFactory.HELVETICA, 8, COL_GRAY);
        Font fCae     = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COL_BLUE);

        // ── Header ──────────────────────────────────────────────────────
        PdfPTable hdrTable = new PdfPTable(new float[]{2f, 1f});
        hdrTable.setWidthPercentage(100);
        hdrTable.setSpacingAfter(12);

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.addElement(new Phrase("MMotos", fTitle));
        Phrase subPhrase = new Phrase();
        String tipoLabel = (tipoFactura != null && !tipoFactura.equals("NO_FISCAL"))
                ? "Factura " + tipoFactura
                : "Ticket de Venta";
        subPhrase.add(new Chunk(tipoLabel, fSub));
        logoCell.addElement(subPhrase);
        hdrTable.addCell(logoCell);

        PdfPCell metaCell = new PdfPCell();
        metaCell.setBorder(Rectangle.NO_BORDER);
        metaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        metaCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        if (v.numeroTicket() != null) {
            Paragraph tktPara = new Paragraph("# " + v.numeroTicket(), fBodyB);
            tktPara.setAlignment(Element.ALIGN_RIGHT);
            metaCell.addElement(tktPara);
        }
        if (v.fechaEmision() != null) {
            Paragraph fechaPara = new Paragraph(v.fechaEmision().format(FMT), fSub);
            fechaPara.setAlignment(Element.ALIGN_RIGHT);
            metaCell.addElement(fechaPara);
        }
        hdrTable.addCell(metaCell);
        doc.add(hdrTable);

        // Divider
        PdfPTable div = new PdfPTable(1);
        div.setWidthPercentage(100);
        div.setSpacingAfter(10);
        PdfPCell divCell = new PdfPCell(new Phrase(""));
        divCell.setBackgroundColor(COL_DARK);
        divCell.setFixedHeight(2);
        divCell.setBorder(Rectangle.NO_BORDER);
        div.addCell(divCell);
        doc.add(div);

        // ── Items table ─────────────────────────────────────────────────
        PdfPTable items = new PdfPTable(new float[]{4f, 1f, 1.8f, 1.8f});
        items.setWidthPercentage(100);
        items.setSpacingAfter(8);

        for (String h : new String[]{"Producto", "Cant.", "Precio unit.", "Subtotal"}) {
            PdfPCell c = new PdfPCell(new Phrase(h, fHead));
            c.setBackgroundColor(COL_DARK);
            c.setPadding(5);
            c.setBorder(Rectangle.NO_BORDER);
            c.setHorizontalAlignment(h.equals("Producto") ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            items.addCell(c);
        }

        boolean alt = false;
        for (LineaTicketDTO ln : v.lineas()) {
            Color rowBg = alt ? COL_BG : Color.WHITE;
            PdfPCell nmCell = itemCell(ln.nombreHistorico(), fBody, rowBg, Element.ALIGN_LEFT);
            PdfPCell qtyCell = itemCell(String.valueOf(ln.cantidad()), fBody, rowBg, Element.ALIGN_RIGHT);
            PdfPCell puCell  = itemCell(fmt(ln.precioUnitario()), fBody, rowBg, Element.ALIGN_RIGHT);
            PdfPCell stCell  = itemCell(fmt(ln.subtotal()), fBodyB, rowBg, Element.ALIGN_RIGHT);
            items.addCell(nmCell);
            items.addCell(qtyCell);
            items.addCell(puCell);
            items.addCell(stCell);
            alt = !alt;
        }
        doc.add(items);

        // ── Totals & Payments ─────────────────────────────────────────
        PdfPTable totals = new PdfPTable(new float[]{3f, 1.8f});
        totals.setWidthPercentage(50);
        totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totals.setSpacingAfter(12);

        for (PagoTicketDTO p : v.pagos()) {
            PdfPCell pm = new PdfPCell(new Phrase(metodoLabel(p.metodo()), fSmall));
            pm.setBorder(Rectangle.TOP);
            pm.setPaddingTop(4);
            PdfPCell pv = new PdfPCell(new Phrase(fmt(p.monto()), fSmall));
            pv.setBorder(Rectangle.TOP);
            pv.setHorizontalAlignment(Element.ALIGN_RIGHT);
            pv.setPaddingTop(4);
            totals.addCell(pm);
            totals.addCell(pv);
        }

        PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL", fHead));
        totalLabel.setBackgroundColor(COL_DARK);
        totalLabel.setPadding(6);
        totalLabel.setBorder(Rectangle.NO_BORDER);
        PdfPCell totalValue = new PdfPCell(new Phrase(fmt(v.total()), fHead));
        totalValue.setBackgroundColor(COL_DARK);
        totalValue.setPadding(6);
        totalValue.setBorder(Rectangle.NO_BORDER);
        totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totals.addCell(totalLabel);
        totals.addCell(totalValue);
        doc.add(totals);

        // ── CAE Block ────────────────────────────────────────────────
        if (v.cae() != null && !v.cae().isBlank()) {
            PdfPTable caeBox = new PdfPTable(1);
            caeBox.setWidthPercentage(100);
            caeBox.setSpacingBefore(8);

            PdfPCell caeCell = new PdfPCell();
            caeCell.setBorderColor(COL_BLUE);
            caeCell.setBorderWidth(1.5f);
            caeCell.setPadding(10);

            Paragraph caeTitle = new Paragraph("Comprobante Autorizado por ARCA/AFIP", fBodyB);
            caeTitle.setSpacingAfter(4);
            caeCell.addElement(caeTitle);

            caeCell.addElement(new Phrase("CAE: " + v.cae(), fCae));
            caeCell.addElement(new Phrase("\n" + v.estadoFiscal(), fSmall));

            // QR placeholder text (URL-style for AFIP validation)
            String qrUrl = "https://www.afip.gob.ar/fe/qr/";
            caeCell.addElement(new Phrase("\nVerificar: " + qrUrl, fSmall));

            caeBox.addCell(caeCell);
            doc.add(caeBox);
        }

        // ── Footer ────────────────────────────────────────────────────
        doc.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph("Generado por MMotos — Sistema de Gestión", fSmall);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);
    }

    private PdfPCell itemCell(String text, Font font, Color bg, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBackgroundColor(bg);
        c.setPadding(5);
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColor(new Color(220, 220, 225));
        c.setHorizontalAlignment(align);
        return c;
    }

    private String fmt(BigDecimal n) {
        if (n == null) return "$ 0,00";
        return "$ " + MONEY_FMT.format(n);
    }

    private String metodoLabel(String metodo) {
        return switch (metodo.toUpperCase()) {
            case "EFECTIVO"        -> "Efectivo";
            case "TARJETA_DEBITO"  -> "Débito";
            case "TARJETA_CREDITO" -> "Crédito";
            case "TRANSFERENCIA"   -> "Transferencia";
            case "MERCADO_PAGO"    -> "Mercado Pago";
            default                -> metodo;
        };
    }
}
