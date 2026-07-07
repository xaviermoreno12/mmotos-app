package com.mmotos.infrastructure.output.notification;

import com.mmotos.domain.port.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

public class N8nAdapter implements NotificationPort {

    private static final Logger log = LoggerFactory.getLogger(N8nAdapter.class);

    private final HttpClient httpClient;
    private final String webhookUrl;

    public N8nAdapter(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    }

    @Override
    public void enviarAlertaStockBajo(UUID productoId, String sku, String nombre, int stockActual) {
        String payload = """
            {"tipo":"STOCK_BAJO","productoId":"%s","productoNombre":"%s","sku":"%s","stockActual":%d}
            """.formatted(productoId, nombre, sku, stockActual).trim();
        enviar(payload);
    }

    @Override
    public void enviarAlertaCritica(String mensaje) {
        String payload = """
            {"tipo":"ALERTA_CRITICA","mensaje":"%s"}
            """.formatted(mensaje.replace("\"", "'")).trim();
        enviar(payload);
    }

    @Override
    public void enviarVentaAnulada(String numeroTicket, BigDecimal total, String usuarioNombre, String motivo) {
        String payload = """
            {"tipo":"VENTA_ANULADA","numeroTicket":"%s","total":%s,"usuarioNombre":"%s","motivo":"%s"}
            """.formatted(numeroTicket, total, usuarioNombre.replace("\"", "'"), motivo.replace("\"", "'")).trim();
        enviar(payload);
    }

    @Override
    public void preguntarResumenStock() {
        String payload = """
            {"tipo":"PREGUNTA_RESUMEN_STOCK"}
            """.trim();
        enviar(payload);
    }

    private void enviar(String payload) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.debug("N8n deshabilitado (webhook-url vacío). Payload descartado: {}", payload);
            return;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 400) {
                log.warn("N8n respondió con error {}: {}", resp.statusCode(), resp.body());
            }
        } catch (Exception e) {
            // Las notificaciones no deben interrumpir el flujo de negocio
            log.warn("No se pudo enviar notificación a n8n: {}", e.getMessage());
        }
    }
}
