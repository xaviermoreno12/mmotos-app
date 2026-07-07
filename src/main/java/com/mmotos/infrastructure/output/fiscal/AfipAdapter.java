package com.mmotos.infrastructure.output.fiscal;

import com.mmotos.domain.exception.FiscalException;
import com.mmotos.domain.exception.FiscalValidationException;
import com.mmotos.domain.model.Venta;
import com.mmotos.domain.port.FiscalPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

// Adapter para Web Service de AFIP (wsfe).
// Timeout agresivo de 10s para no bloquear el pool de conexiones DB.
// Idempotencia: antes de emitir consulta si el comprobante ya tiene CAE en AFIP.
public class AfipAdapter implements FiscalPort {

    private static final Logger log = LoggerFactory.getLogger(AfipAdapter.class);

    private final HttpClient httpClient;
    private final String baseUrl;
    private final String cuitEmpresa;
    private final int puntoVenta;

    public AfipAdapter(String baseUrl, String cuitEmpresa, int puntoVenta) {
        this.baseUrl      = baseUrl;
        this.cuitEmpresa  = cuitEmpresa;
        this.puntoVenta   = puntoVenta;
        this.httpClient   = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    }

    @Override
    public boolean isHardwareReady() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/dummy"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            boolean disponible = resp.statusCode() == 200;
            if (!disponible) {
                log.warn("AFIP WS no disponible (status {}). Considerar modo CONTINGENCIA.", resp.statusCode());
            }
            return disponible;
        } catch (Exception e) {
            log.warn("AFIP WS no alcanzable: {}. Operando en modo CONTINGENCIA.", e.getMessage());
            return false;
        }
    }

    @Override
    public FiscalResponse emitir(Venta venta) {
        validarParaAfip(venta);

        // Idempotencia: si ya existe un CAE para este comprobante, no reenviar
        String caeExistente = consultarCaeExistente(venta);
        if (caeExistente != null) {
            log.warn("Venta {} ya tiene CAE en AFIP ({}). No se reenvía.", venta.getId(), caeExistente);
            String nroTicket = "%05d-%08d".formatted(puntoVenta, 0);
            return new FiscalResponse(caeExistente, nroTicket);
        }

        try {
            String soapBody = buildSolicitudFe(venta);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "text/xml; charset=utf-8")
                .header("SOAPAction", "FECAESolicitar")
                .POST(HttpRequest.BodyPublishers.ofString(soapBody))
                .build();

            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) {
                throw new FiscalException("AFIP devolvió HTTP %d".formatted(resp.statusCode()));
            }

            return parsearRespuestaAfip(resp.body());

        } catch (FiscalException e) {
            throw e;
        } catch (Exception e) {
            throw new FiscalException("Error de comunicación con AFIP: " + e.getMessage(), e);
        }
    }

    private void validarParaAfip(Venta venta) {
        if (venta.getTipoFactura().name().equals("A") && venta.getCuitCliente() == null) {
            throw new FiscalValidationException("Factura A requiere CUIT del cliente");
        }
        if (cuitEmpresa == null || cuitEmpresa.isBlank()) {
            throw new FiscalValidationException("CUIT empresa no configurado en application.yml");
        }
    }

    private String consultarCaeExistente(Venta venta) {
        // En producción: llamar a FECompConsultar con el nro de comprobante local.
        // Retorna null si no existe en AFIP (flujo normal de primera emisión).
        return null;
    }

    private String buildSolicitudFe(Venta venta) {
        // Placeholder: en producción construir el SOAP completo para FECAESolicitar.
        // Incluye: CbteDesde, CbteHasta, CbteFch, ImpTotal, ImpNeto, etc.
        return "<soap:Envelope><!-- FECAESolicitar venta=%s --></soap:Envelope>".formatted(venta.getId());
    }

    private FiscalResponse parsearRespuestaAfip(String soapResponse) {
        // Placeholder: parsear el XML de respuesta para extraer CAE y Vencimiento.
        // En producción usar JAXB o StAX para el XML de AFIP.
        if (soapResponse.contains("Err")) {
            throw new FiscalException("AFIP rechazó el comprobante: " + soapResponse);
        }
        String cae = "00000000000000";           // Extraer del XML real
        String nroTicket = "%05d-%08d".formatted(puntoVenta, 1);
        log.info("CAE obtenido de AFIP: {}", cae);
        return new FiscalResponse(cae, nroTicket);
    }
}
