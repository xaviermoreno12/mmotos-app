package com.mmotos.infrastructure.output.fiscal;

import com.mmotos.domain.model.Venta;
import com.mmotos.domain.port.FiscalPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public class NoFiscalAdapter implements FiscalPort {

    private static final Logger log = LoggerFactory.getLogger(NoFiscalAdapter.class);
    private static final AtomicLong contador = new AtomicLong(1);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public boolean isHardwareReady() {
        return true;
    }

    @Override
    public FiscalResponse emitir(Venta venta) {
        String fecha = LocalDateTime.now().format(FMT);
        String ticket = "INT-%s-%05d".formatted(fecha, contador.getAndIncrement());
        log.info("Ticket interno generado: {} para venta {}", ticket, venta.getId());
        return new FiscalResponse(null, ticket);
    }
}
