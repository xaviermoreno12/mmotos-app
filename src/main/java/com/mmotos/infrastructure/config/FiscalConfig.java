package com.mmotos.infrastructure.config;

import com.mmotos.domain.port.FiscalPort;
import com.mmotos.domain.port.NotificationPort;
import com.mmotos.infrastructure.output.fiscal.*;
import com.mmotos.infrastructure.output.notification.N8nAdapter;
import com.mmotos.infrastructure.output.notification.TelegramAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FiscalConfig {

    private static final Logger log = LoggerFactory.getLogger(FiscalConfig.class);

    @Bean
    public FiscalPort fiscalPort(AppProperties props) {
        String modo = props.fiscal().modo().toUpperCase();
        log.info("Modo fiscal configurado: {}", modo);

        return switch (modo) {
            case "AFIP" -> {
                var afip = props.fiscal().afip();
                log.info("Usando AfipAdapter → {}", afip.baseUrl());
                yield new AfipAdapter(afip.baseUrl(), afip.cuitEmpresa(), afip.puntoVenta());
            }
            case "HASAR" -> {
                var hasar = props.fiscal().hasar();
                log.info("Usando HasarAdapter → puerto {} @ {} baud", hasar.puertoSerie(), hasar.baudRate());
                yield new HasarAdapter(new JsscSerialDriver(), hasar.puertoSerie(), hasar.baudRate());
            }
            default -> {
                log.warn("Modo '{}' no reconocido o NO_FISCAL. Usando tickets internos.", modo);
                yield new NoFiscalAdapter();
            }
        };
    }

    @Bean
    public NotificationPort notificationPort(AppProperties props) {
        if (props.telegram().enabled()) {
            log.info("Notificaciones Telegram habilitadas → chat {}", props.telegram().chatId());
            return new TelegramAdapter(props.telegram().botToken(), props.telegram().chatId());
        }
        if (props.n8n().enabled()) {
            log.info("Notificaciones n8n habilitadas → {}", props.n8n().webhookUrl());
            return new N8nAdapter(props.n8n().webhookUrl());
        }
        log.info("Notificaciones n8n/Telegram deshabilitadas. Usando logger.");
        return new LoggingNotificationAdapter();
    }

    // Adapter nulo para cuando no hay notificaciones habilitadas — logea en lugar de enviar HTTP
    private static class LoggingNotificationAdapter implements NotificationPort {
        private static final Logger log = LoggerFactory.getLogger(LoggingNotificationAdapter.class);

        @Override
        public void enviarAlertaStockBajo(java.util.UUID productoId, String sku, String nombre, int stockActual) {
            log.warn("[STOCK BAJO] SKU={} | {} | stock={}", sku, nombre, stockActual);
        }

        @Override
        public void enviarAlertaCritica(String mensaje) {
            log.error("[ALERTA CRITICA] {}", mensaje);
        }

        @Override
        public void enviarVentaAnulada(String numeroTicket, java.math.BigDecimal total, String usuarioNombre, String motivo) {
            log.warn("[VENTA ANULADA] {} | por {} | total={} | motivo={}", numeroTicket, usuarioNombre, total, motivo);
        }

        @Override
        public void preguntarResumenStock() {
            log.info("[RESUMEN STOCK] Recordatorio diario (notificaciones deshabilitadas)");
        }
    }
}
