package com.mmotos.infrastructure.output.persistence.mapper;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mmotos.domain.model.*;
import com.mmotos.infrastructure.output.persistence.entity.PagoEntity;
import com.mmotos.infrastructure.output.persistence.entity.VentaDetalleEntity;
import com.mmotos.infrastructure.output.persistence.entity.VentaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class VentaMapper {

    public VentaEntity toVentaEntity(Venta domain) {
        return new VentaEntity(
            domain.getId(),
            domain.getTipoFactura().name(),
            domain.getFechaEmision(),
            domain.getCuitCliente(),
            domain.totalLineas(),
            domain.getEstadoFiscal().name(),
            domain.getSyncStatus().name(),
            domain.getUsuarioId()
        );
    }

    public List<VentaDetalleEntity> toDetalleEntities(Venta domain) {
        return domain.getLineas().stream()
            .map(l -> new VentaDetalleEntity(
                UUID.randomUUID(),
                domain.getId(),
                l.productoId(),
                l.skuHistorico(),
                l.nombreHistorico(),
                l.cantidad(),
                l.precioUnitarioHistorico()
            ))
            .toList();
    }

    public List<PagoEntity> toPagoEntities(Venta domain) {
        return domain.getPagos().stream()
            .map(p -> new PagoEntity(UUID.randomUUID(), domain.getId(), p.metodo().name(), p.monto(), buildDetallesPago(p)))
            .toList();
    }

    private ObjectNode buildDetallesPago(Pago pago) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        switch (pago) {
            case PagoTarjeta t    -> { node.put("nro_cupon", t.numeroCupon()); node.put("cuotas", t.cuotas()); }
            case PagoTransferencia t -> node.put("cbu_origen", t.cbuOrigen());
            case PagoMercadoPago mp  -> node.put("referencia_pago", mp.referenciaPago());
            default -> {}
        }
        return node;
    }

    public void aplicarDatosFiscales(VentaEntity entity, String cae, String numeroTicket) {
        entity.setCae(cae);
        entity.setNumeroTicket(numeroTicket);
        entity.setEstadoFiscal(EstadoFiscal.APROBADO.name());
        entity.setSyncStatus(SyncStatus.SYNCED.name());
    }

    public void marcarError(VentaEntity entity) {
        entity.setEstadoFiscal(EstadoFiscal.ERROR_HARDWARE.name());
        entity.setSyncStatus(SyncStatus.FAILED.name());
    }
}
