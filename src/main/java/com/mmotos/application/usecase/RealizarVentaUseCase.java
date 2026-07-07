package com.mmotos.application.usecase;

import com.mmotos.application.dto.VentaRequest;
import com.mmotos.application.dto.VentaResponse;
import com.mmotos.domain.exception.FiscalException;
import com.mmotos.domain.model.Venta;
import com.mmotos.domain.port.ConfiguracionRepository;
import com.mmotos.domain.port.FiscalPort;
import com.mmotos.domain.port.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class RealizarVentaUseCase {

    private static final Logger log = LoggerFactory.getLogger(RealizarVentaUseCase.class);

    private final VentaTransaccionHelper transaccionHelper;
    private final ConfiguracionRepository configuracionRepository;
    private final FiscalPort fiscalPort;
    private final NotificationPort notificationPort;

    public RealizarVentaUseCase(VentaTransaccionHelper transaccionHelper,
                                 ConfiguracionRepository configuracionRepository,
                                 FiscalPort fiscalPort,
                                 NotificationPort notificationPort) {
        this.transaccionHelper       = transaccionHelper;
        this.configuracionRepository = configuracionRepository;
        this.fiscalPort              = fiscalPort;
        this.notificationPort        = notificationPort;
    }

    // Fase 1 (tx): descuenta stock, guarda venta PENDIENTE → commit
    // Fase 2: llama AFIP/hardware FUERA de transacción — no bloquea pool DB
    // Fase 3 (tx): confirma con CAE o marca ERROR_HARDWARE → commit
    public VentaResponse ejecutar(VentaRequest request) {
        BigDecimal cotizacion = configuracionRepository.getCotizacionDolar().orElse(BigDecimal.ONE);

        Venta venta = transaccionHelper.guardarPendiente(request, cotizacion, fiscalPort);

        try {
            FiscalPort.FiscalResponse fiscalResponse = fiscalPort.emitir(venta);
            return transaccionHelper.confirmarFiscal(venta.getId(), fiscalResponse);
        } catch (FiscalException e) {
            log.error("Error fiscal venta {}: {}", venta.getId(), e.getMessage());
            transaccionHelper.marcarError(venta.getId());
            notificationPort.enviarAlertaCritica(
                "Error fiscal en venta " + venta.getId() + ": " + e.getMessage());
            throw e;
        }
    }
}
