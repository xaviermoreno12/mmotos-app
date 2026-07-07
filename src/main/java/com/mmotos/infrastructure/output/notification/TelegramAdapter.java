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

public class TelegramAdapter implements NotificationPort {

    private static final Logger log = LoggerFactory.getLogger(TelegramAdapter.class);

    private final HttpClient httpClient;
    private final String botToken;
    private final String chatId;

    public TelegramAdapter(String botToken, String chatId) {
        this.botToken = botToken;
        this.chatId = chatId;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    }

    @Override
    public void enviarAlertaStockBajo(UUID productoId, String sku, String nombre, int stockActual) {
        String texto = "⚠️ Stock bajo: %s (%s) — quedan %d".formatted(nombre, sku, stockActual);
        enviarMensaje(texto, null);
    }

    @Override
    public void enviarAlertaCritica(String mensaje) {
        enviarMensaje("🚨 " + mensaje, null);
    }

    @Override
    public void enviarVentaAnulada(String numeroTicket, BigDecimal total, String usuarioNombre, String motivo) {
        String texto = "❌ Venta %s anulada por %s\nTotal: $%s\nMotivo: %s"
            .formatted(numeroTicket, usuarioNombre, total, motivo);
        enviarMensaje(texto, null);
    }

    @Override
    public void preguntarResumenStock() {
        String texto = "📦 ¿Querés un resumen de stock?";
        String inlineKeyboard = """
            {"inline_keyboard":[[{"text":"Sí","callback_data":"resumen_stock_si"},{"text":"No","callback_data":"resumen_stock_no"}]]}
            """.trim();
        enviarMensaje(texto, inlineKeyboard);
    }

    private void enviarMensaje(String texto, String replyMarkupJson) {
        if (botToken == null || botToken.isBlank() || chatId == null || chatId.isBlank()) {
            log.debug("Telegram deshabilitado (bot-token o chat-id vacío). Mensaje descartado: {}", texto);
            return;
        }
        String textoEscapado = texto.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        StringBuilder payload = new StringBuilder("{\"chat_id\":\"")
            .append(chatId)
            .append("\",\"text\":\"")
            .append(textoEscapado)
            .append("\"");
        if (replyMarkupJson != null) {
            payload.append(",\"reply_markup\":").append(replyMarkupJson);
        }
        payload.append("}");

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.telegram.org/bot" + botToken + "/sendMessage"))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();
            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 400) {
                log.warn("Telegram respondió con error {}: {}", resp.statusCode(), resp.body());
            }
        } catch (Exception e) {
            // Las notificaciones no deben interrumpir el flujo de negocio
            log.warn("No se pudo enviar notificación a Telegram: {}", e.getMessage());
        }
    }
}
