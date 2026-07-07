package com.mmotos.infrastructure.output.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mmotos.application.dto.FacturaExtraidaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

public class OpenAiExtractionService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiExtractionService.class);
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    private static final String PROMPT_SISTEMA = """
        Sos un asistente que extrae datos estructurados de facturas de compra a \
        proveedores para un comercio de repuestos de motos en Argentina. Te paso la \
        imagen de la factura directamente.

        Devolvé SOLO un objeto JSON (sin texto adicional, sin markdown) con esta forma exacta:
        {
          "fecha": "fecha de la factura tal como aparece, o null si no se ve",
          "proveedor": "nombre del proveedor",
          "cuit": "CUIT del proveedor si aparece, sino null",
          "numeroFactura": "número de factura o remito",
          "total": numero con punto decimal,
          "categoria": una de "Repuestos y Mercadería", "Herramientas y Maquinaria", \
        "Insumos de Taller", "Combustible y Flete", "Servicios y Mantenimiento", \
        "Alquiler", "Impuestos", "Otros",
          "estadoPago": "Pendiente" o "Pagado",
          "lineas": [
            { "nombre": "descripción del artículo", "cantidad": entero (1 si no se ve), \
        "precioUnitario": numero con punto decimal (si solo hay subtotal por línea, \
        calcularlo dividiendo por la cantidad) }
          ]
        }

        Si algún importe no es legible, estimalo a partir de los otros. Solo devolvé \
        el objeto JSON, sin texto adicional.
        """;

    private final HttpClient httpClient;
    private final String apiKey;
    private final ObjectMapper mapper;

    public OpenAiExtractionService(String apiKey, ObjectMapper mapper) {
        this.apiKey = apiKey;
        this.mapper = mapper;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    public FacturaExtraidaDTO extraerDesdeFoto(String imagenBase64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(imagenBase64);
            // A5: Rechazar formatos que no sean JPEG/PNG (PDF, EXE, etc. no son imagenes validas)
            String mimeType = detectarMimeType(bytes);
            if (mimeType == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Formato de imagen no soportado. Envia una foto JPEG o PNG.");
            }
            String dataUri = "data:" + mimeType + ";base64," + imagenBase64;

            ObjectNode body = mapper.createObjectNode();
            body.put("model", "gpt-4o-mini");
            body.put("max_tokens", 2000);
            ObjectNode responseFormat = mapper.createObjectNode();
            responseFormat.put("type", "json_object");
            body.set("response_format", responseFormat);

            ArrayNode messages = body.putArray("messages");

            messages.addObject()
                .put("role", "system")
                .put("content", PROMPT_SISTEMA);

            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            ArrayNode userContent = userMsg.putArray("content");

            ObjectNode imageBlock = userContent.addObject();
            imageBlock.put("type", "image_url");
            ObjectNode imageUrlNode = imageBlock.putObject("image_url");
            imageUrlNode.put("url", dataUri);
            imageUrlNode.put("detail", "high");

            ObjectNode textBlock = userContent.addObject();
            textBlock.put("type", "text");
            textBlock.put("text", "Extraé los datos de esta factura en el JSON solicitado.");

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_URL))
                .timeout(Duration.ofSeconds(90))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                log.warn("OpenAI Vision respondió con error {}: {}", response.statusCode(), response.body());
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo procesar la factura con IA. Intentá de nuevo en unos segundos.");
            }

            JsonNode root = mapper.readTree(response.body());
            String contenido = root.path("choices").path(0).path("message").path("content").asText();

            return mapper.readValue(contenido, FacturaExtraidaDTO.class);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Error al consultar OpenAI Vision: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "No se pudo procesar la factura con IA. Intentá de nuevo en unos segundos.");
        }
    }

    // A5: Retorna null si no es JPEG/PNG (el caller lanza 400 en ese caso)
    private String detectarMimeType(byte[] bytes) {
        try {
            var is = new javax.imageio.stream.MemoryCacheImageInputStream(
                new ByteArrayInputStream(bytes));
            var readers = javax.imageio.ImageIO.getImageReaders(is);
            if (readers.hasNext()) {
                String fmt = readers.next().getFormatName().toLowerCase();
                is.close();
                if ("png".equals(fmt)) return "image/png";
                if ("jpeg".equals(fmt) || "jpg".equals(fmt)) return "image/jpeg";
                return null; // formato conocido pero no soportado
            }
            is.close();
        } catch (Exception ignored) {}
        return null; // no reconocido — forzar rechazo en lugar de asumir JPEG
    }
}
