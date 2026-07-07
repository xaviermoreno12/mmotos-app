package com.mmotos.infrastructure.output.fiscal;

import com.mmotos.domain.exception.HardwareFailureException;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsscSerialDriver implements SerialDriver {

    private static final Logger log = LoggerFactory.getLogger(JsscSerialDriver.class);

    private SerialPort serialPort;

    @Override
    public void abrir(String puerto, int baudRate) {
        try {
            serialPort = new SerialPort(puerto);
            serialPort.openPort();
            serialPort.setParams(baudRate,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
            log.info("Puerto serial {} abierto a {} baud", puerto, baudRate);
        } catch (SerialPortException e) {
            throw new HardwareFailureException("No se pudo abrir el puerto serial %s: %s".formatted(puerto, e.getMessage()));
        }
    }

    @Override
    public void enviar(byte[] datos) {
        try {
            serialPort.writeBytes(datos);
        } catch (SerialPortException e) {
            throw new HardwareFailureException("Error al escribir en puerto serial: " + e.getMessage());
        }
    }

    @Override
    public byte[] recibir(int timeoutMs) {
        try {
            return serialPort.readBytes(1, timeoutMs);
        } catch (SerialPortException | SerialPortTimeoutException e) {
            throw new HardwareFailureException("Timeout o error al leer del puerto serial: " + e.getMessage());
        }
    }

    @Override
    public void cerrar() {
        if (serialPort != null && serialPort.isOpened()) {
            try {
                serialPort.closePort();
            } catch (SerialPortException e) {
                log.warn("Error al cerrar puerto serial: {}", e.getMessage());
            }
        }
    }

    @Override
    public boolean estaConectado() {
        return serialPort != null && serialPort.isOpened();
    }
}
