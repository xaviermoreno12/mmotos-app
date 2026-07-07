package com.mmotos.infrastructure.output.report;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Component("excelReportBuilder")
public class ExcelReportBuilder implements ReportBuilder {

    @Override
    public byte[] build(String titulo, String subtitulo, String[] headers,
                        List<Object[]> filas, String[] totalesLabels, Object[] totalesValores) {
        try (var wb = new XSSFWorkbook(); var out = new ByteArrayOutputStream()) {
            var sheet = wb.createSheet("Reporte");

            // Estilos
            var styleTitulo = wb.createCellStyle();
            var fontTitulo = wb.createFont();
            fontTitulo.setBold(true);
            fontTitulo.setFontHeightInPoints((short) 14);
            styleTitulo.setFont(fontTitulo);

            var styleHeader = wb.createCellStyle();
            styleHeader.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            var fontHeader = wb.createFont();
            fontHeader.setBold(true);
            fontHeader.setColor(IndexedColors.WHITE.getIndex());
            styleHeader.setFont(fontHeader);
            styleHeader.setBorderBottom(BorderStyle.THIN);

            var styleAlt = wb.createCellStyle();
            styleAlt.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            styleAlt.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            var styleTotalLabel = wb.createCellStyle();
            var fontBold = wb.createFont();
            fontBold.setBold(true);
            styleTotalLabel.setFont(fontBold);

            // Fila título
            int row = 0;
            var rowTitulo = sheet.createRow(row++);
            var cellTitulo = rowTitulo.createCell(0);
            cellTitulo.setCellValue(titulo);
            cellTitulo.setCellStyle(styleTitulo);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.length - 1));

            // Subtítulo
            if (subtitulo != null && !subtitulo.isBlank()) {
                var rowSub = sheet.createRow(row++);
                rowSub.createCell(0).setCellValue(subtitulo);
                sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, headers.length - 1));
            }
            row++; // línea en blanco

            // Headers
            var rowHeader = sheet.createRow(row++);
            for (int i = 0; i < headers.length; i++) {
                var cell = rowHeader.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(styleHeader);
            }

            // Datos
            for (int i = 0; i < filas.size(); i++) {
                var dataRow = sheet.createRow(row++);
                var fila = filas.get(i);
                var style = (i % 2 == 1) ? styleAlt : null;
                for (int j = 0; j < fila.length; j++) {
                    var cell = dataRow.createCell(j);
                    if (fila[j] instanceof Number n) {
                        cell.setCellValue(n.doubleValue());
                    } else if (fila[j] != null) {
                        cell.setCellValue(fila[j].toString());
                    }
                    if (style != null) cell.setCellStyle(style);
                }
            }

            // Totales
            if (totalesLabels != null && totalesValores != null) {
                row++; // espacio
                for (int i = 0; i < totalesLabels.length; i++) {
                    var totalRow = sheet.createRow(row++);
                    var labelCell = totalRow.createCell(headers.length - 2);
                    labelCell.setCellValue(totalesLabels[i]);
                    labelCell.setCellStyle(styleTotalLabel);
                    var valCell = totalRow.createCell(headers.length - 1);
                    if (totalesValores[i] instanceof Number n) {
                        valCell.setCellValue(n.doubleValue());
                    } else if (totalesValores[i] != null) {
                        valCell.setCellValue(totalesValores[i].toString());
                    }
                    valCell.setCellStyle(styleTotalLabel);
                }
            }

            // Auto-size
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                if (sheet.getColumnWidth(i) > 8000) sheet.setColumnWidth(i, 8000);
            }

            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando Excel: " + e.getMessage(), e);
        }
    }
}
