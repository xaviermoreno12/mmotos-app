package com.mmotos.infrastructure.output.fiscal;

import com.mmotos.domain.exception.HardwareFailureException;
import com.mmotos.domain.model.Venta;
import com.mmotos.domain.port.FiscalPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

// Adapter para controladores fiscales Hasar (protocolo serial).
// Toda comunicación con JSSC está detrás de SerialDriver para poder intercambiar la librería nativa sin tocar este adapter.
public class HasarAdapter implements FiscalPort {

    private static final Logger log = LoggerFactory.getLogger(HasarAdapter.class);

    private static final int TIMEOUT_RESPUESTA_MS = 5000;
    private static final byte STX = 0x02;
    private static final byte ETX = 0x03;

    private final SerialDriver serial;
    private final String puerto;
    private final int baudRate;

    public HasarAdapter(SerialDriver serial, String puerto, int baudRate) {
        this.serial   = serial;
        this.puerto   = puerto;
        this.baudRate = baudRate;
    }

    @Override
    public boolean isHardwareReady() {
        try {
            if (!serial.estaConectado()) {
                serial.abrir(puerto, baudRate);
            }
            // Status command Hasar: STX + "S1" + ETX
            byte[] statusCmd = buildCommand("S1");
            serial.enviar(statusCmd);
            byte[] respuesta = serial.recibir(TIMEOUT_RESPUESTA_MS);
            return respuesta != null && respuesta.length > 0;
        } catch (HardwareFailureException e) {
            log.warn("Hasar no responde en {}: {}", puerto, e.getMessage());
            return false;
        }
    }

    @Override
    public FiscalResponse emitir(Venta venta) {
        if (!serial.estaConectado()) {
            serial.abrir(puerto, baudRate);
        }

        try {
            // Apertura de comprobante fiscal (comando simplificado — expandir con protocolo Hasar completo)
            String tipoComprobante = mapearTipoFactura(venta.getTipoFactura().name());
            serial.enviar(buildCommand("FC" + tipoComprobante));
            serial.recibir(TIMEOUT_RESPUESTA_MS);

            // Impresión de líneas
            for (var linea : venta.getLineas()) {
                String cmd = "FP%s%012d%012d".formatted(
                    linea.nombreHistorico().substring(0, Math.min(20, linea.nombreHistorico().length())),
                    linea.cantidad(),
                    linea.precioUnitarioHistorico().movePointRight(2).longValue()
                );
                serial.enviar(buildCommand(cmd));
                serial.recibir(TIMEOUT_RESPUESTA_MS);
            }

            // Cierre — Hasar devuelve el número de comprobante en la respuesta
            serial.enviar(buildCommand("FT"));
            byte[] respuestaCierre = serial.recibir(TIMEOUT_RESPUESTA_MS);
            String numeroTicket = extraerNumeroTicket(respuestaCierre);

            log.info("Ticket fiscal Hasar emitido: {}", numeroTicket);
            return new FiscalResponse(null, numeroTicket);

        } finally {
            serial.cerrar();
        }
    }

    private byte[] buildCommand(String comando) {
        byte[] body = comando.getBytes(StandardCharsets.US_ASCII);
        byte[] frame = new byte[body.length + 2];
        frame[0] = STX;
        System.arraycopy(body, 0, frame, 1, body.length);
        frame[frame.length - 1] = ETX;
        return frame;
    }

    private String mapearTipoFactura(String tipo) {
        return switch (tipo) {
            case "A" -> "A";
            case "B" -> "B";
            case "C" -> "C";
            default  -> "X"; // No fiscal en controlador
        };
    }

    private String extraerNumeroTicket(byte[] respuesta) {
        if (respuesta == null || respuesta.length < 4) return "DESCONOCIDO";
        // El controlador Hasar devuelve el número en los últimos 8 bytes antes de ETX
        String raw = new String(respuesta, StandardCharsets.US_ASCII).trim();
        return raw.replaceAll("[^\\d]", "");
    }
}
