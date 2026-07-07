package com.mmotos.infrastructure.output.report;

import java.util.List;

public interface ReportBuilder {
    byte[] build(String titulo, String subtitulo, String[] headers,
                 List<Object[]> filas, String[] totalesLabels, Object[] totalesValores);
}