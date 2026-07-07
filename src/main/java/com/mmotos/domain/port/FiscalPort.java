package com.mmotos.domain.port;

import com.mmotos.domain.model.Venta;

public interface FiscalPort {

    boolean isHardwareReady();

    FiscalResponse emitir(Venta venta);

    record FiscalResponse(String cae, String numeroTicket) {}
}
