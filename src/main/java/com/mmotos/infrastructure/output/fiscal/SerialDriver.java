package com.mmotos.infrastructure.output.fiscal;

// Interfaz intermedia entre HasarAdapter y la librería nativa (JSSC).
// Si en el futuro se cambia JSSC por JSerialComm, solo cambia JsscSerialDriver.
public interface SerialDriver {

    void abrir(String puerto, int baudRate);

    void enviar(byte[] datos);

    byte[] recibir(int timeoutMs);

    void cerrar();

    boolean estaConectado();
}
