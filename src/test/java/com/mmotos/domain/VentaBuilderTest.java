package com.mmotos.domain;

import com.mmotos.domain.exception.FiscalValidationException;
import com.mmotos.domain.exception.HardwareFailureException;
import com.mmotos.domain.exception.PaymentInconsistencyException;
import com.mmotos.domain.model.*;
import com.mmotos.domain.port.FiscalPort;
import com.mmotos.domain.service.VentaBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class VentaBuilderTest {

    private final FiscalPort fiscalListo    = mock(FiscalPort.class, withSettings().name("fiscal-listo"));
    private final FiscalPort fiscalNoListo  = mock(FiscalPort.class, withSettings().name("fiscal-no-listo"));

    {
        when(fiscalListo.isHardwareReady()).thenReturn(true);
        when(fiscalNoListo.isHardwareReady()).thenReturn(false);
    }

    @Test
    @DisplayName("Venta B balanceada se construye correctamente")
    void ventaValidaSeConstuyeBien() {
        Venta v = new VentaBuilder()
            .conFacturaB()
            .agregarLinea(UUID.randomUUID(), "SKU-01", "Aceite", 2, new BigDecimal("500"))
            .agregarPago(new PagoEfectivo(new BigDecimal("1000")))
            .build(fiscalListo);

        assertThat(v).isNotNull();
        assertThat(v.estaBalanceada()).isTrue();
        assertThat(v.getTipoFactura()).isEqualTo(TipoFactura.B);
    }

    @Test
    @DisplayName("Factura A sin CUIT lanza FiscalValidationException")
    void facturaASinCuitLanzaExcepcion() {
        assertThatExceptionOfType(FiscalValidationException.class)
            .isThrownBy(() -> new VentaBuilder().conFacturaA(null));
    }

    @Test
    @DisplayName("Factura A con CUIT de longitud incorrecta lanza excepción")
    void facturaAConCuitInvalidoLanzaExcepcion() {
        assertThatExceptionOfType(FiscalValidationException.class)
            .isThrownBy(() -> new VentaBuilder().conFacturaA("1234")); // < 11 dígitos
    }

    @Test
    @DisplayName("Hardware no listo lanza HardwareFailureException en build()")
    void hardwareNoListoLanzaExcepcion() {
        assertThatExceptionOfType(HardwareFailureException.class)
            .isThrownBy(() -> new VentaBuilder()
                .conFacturaB()
                .agregarLinea(UUID.randomUUID(), "SKU-01", "Aceite", 1, new BigDecimal("500"))
                .agregarPago(new PagoEfectivo(new BigDecimal("500")))
                .build(fiscalNoListo));
    }

    @Test
    @DisplayName("Venta NO_FISCAL no requiere hardware listo")
    void ventaNoFiscalNoRequiereHardware() {
        Venta v = new VentaBuilder()
            .sinFactura()
            .agregarLinea(UUID.randomUUID(), "SKU-01", "Tornillo", 1, new BigDecimal("50"))
            .agregarPago(new PagoEfectivo(new BigDecimal("50")))
            .build(fiscalNoListo);   // hardware down pero NO_FISCAL no importa

        assertThat(v.getTipoFactura()).isEqualTo(TipoFactura.NO_FISCAL);
    }

    @Test
    @DisplayName("Pagos no balanceados lanzan PaymentInconsistencyException")
    void pagosDesbalanceadosLanzanExcepcion() {
        assertThatExceptionOfType(PaymentInconsistencyException.class)
            .isThrownBy(() -> new VentaBuilder()
                .conFacturaB()
                .agregarLinea(UUID.randomUUID(), "SKU-01", "Aceite", 1, new BigDecimal("500"))
                .agregarPago(new PagoEfectivo(new BigDecimal("200")))  // falta $300
                .build(fiscalListo));
    }

    @Test
    @DisplayName("Pago mixto balanceado (efectivo + transferencia) se acepta")
    void pagoMixtoBalanceado() {
        Venta v = new VentaBuilder()
            .conFacturaB()
            .agregarLinea(UUID.randomUUID(), "SKU-01", "Neumático", 1, new BigDecimal("30000"))
            .agregarPago(new PagoEfectivo(new BigDecimal("15000")))
            .agregarPago(new PagoTransferencia(new BigDecimal("15000"), "0720000000000000000001"))
            .build(fiscalListo);

        assertThat(v.estaBalanceada()).isTrue();
        assertThat(v.getPagos()).hasSize(2);
    }

    @Test
    @DisplayName("Venta sin líneas lanza IllegalStateException")
    void ventaSinLineasLanzaExcepcion() {
        assertThatIllegalStateException()
            .isThrownBy(() -> new VentaBuilder()
                .conFacturaB()
                .agregarPago(new PagoEfectivo(new BigDecimal("100")))
                .build(fiscalListo));
    }
}
