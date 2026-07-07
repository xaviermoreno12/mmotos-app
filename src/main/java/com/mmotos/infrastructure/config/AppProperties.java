package com.mmotos.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
    Fiscal fiscal,
    Jwt jwt,
    N8n n8n,
    Telegram telegram,
    OpenAi openai,
    Cors cors
) {
    public record Fiscal(String modo, Afip afip, Hasar hasar) {
        public record Afip(String baseUrl, long timeoutMs, String cuitEmpresa, int puntoVenta) {}
        public record Hasar(String puertoSerie, int baudRate) {}
    }

    public record Jwt(String secret, long expirationMs) {}

    public record N8n(String webhookUrl, boolean enabled) {}

    public record Telegram(String botToken, String chatId, boolean enabled) {}

    public record OpenAi(String apiKey, boolean enabled) {}

    public record Cors(java.util.List<String> allowedOrigins) {}
}
