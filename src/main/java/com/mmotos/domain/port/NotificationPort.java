package com.mmotos.domain.port;

import java.math.BigDecimal;
import java.util.UUID;

public interface NotificationPort {

    void enviarAlertaStockBajo(UUID productoId, String sku, String nombre, int stockActual);

    void enviarAlertaCritica(String mensaje);

    void enviarVentaAnulada(String numeroTicket, BigDecimal total, String usuarioNombre, String motivo);

    void preguntarResumenStock();
}
