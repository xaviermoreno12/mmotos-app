package com.mmotos.domain.service;

import com.mmotos.domain.exception.FiscalValidationException;
import com.mmotos.domain.exception.HardwareFailureException;
import com.mmotos.domain.exception.PaymentInconsistencyException;
import com.mmotos.domain.model.*;
import com.mmotos.domain.port.FiscalPort;

import java.math.BigDecimal;
import java.util.UUID;

public class VentaBuilder {

    private final Venta venta = new Venta();

    public VentaBuilder conUsuario(UUID usuarioId) {
        venta.setUsuarioId(usuarioId);
        return this;
    }

    public VentaBuilder conFacturaA(String cuit) {
        if (cuit == null || cuit.length() != 11 || !cuit.matches("\\d+")) {
            throw new FiscalValidationException("CUIT inválido para Factura A: debe tener 11 dígitos numéricos");
        }
        venta.setCuitCliente(cuit);
        venta.setTipoFactura(TipoFactura.A);
        return this;
    }

    public VentaBuilder conFacturaB() {
        venta.setTipoFactura(TipoFactura.B);
        return this;
    }

    public VentaBuilder conFacturaC() {
        venta.setTipoFactura(TipoFactura.C);
        return this;
    }

    public VentaBuilder sinFactura() {
        venta.setTipoFactura(TipoFactura.NO_FISCAL);
        return this;
    }

    public VentaBuilder agregarLinea(UUID productoId, String sku, String nombre,
                                     int cantidad, BigDecimal precioHistorico) {
        venta.agregarLinea(new LineaVenta(productoId, sku, nombre, cantidad, precioHistorico));
        return this;
    }

    public VentaBuilder agregarPago(Pago pago) {
        venta.agregarPago(pago);
        return this;
    }

    /**
     * Valida la lógica de negocio y el estado del hardware antes de construir la venta.
     * El FiscalPort se consulta aquí para evitar que una venta "nazca" con el hardware desconectado.
     * Las ventas NO_FISCAL no necesitan hardware listo.
     */
    public Venta build(FiscalPort fiscalPort) {
        if (venta.getLineas().isEmpty()) {
            throw new IllegalStateException("La venta debe tener al menos una línea de producto");
        }

        if (!venta.estaBalanceada()) {
            throw new PaymentInconsistencyException(
                "La suma de pagos (%.2f) no coincide con el total de la venta (%.2f)"
                    .formatted(venta.totalPagos(), venta.totalLineas())
            );
        }

        if (venta.getTipoFactura() != TipoFactura.NO_FISCAL && !fiscalPort.isHardwareReady()) {
            throw new HardwareFailureException(
                "La impresora fiscal no responde o no tiene papel. Operación abortada para evitar descuento de stock sin comprobante."
            );
        }

        return venta;
    }
}
