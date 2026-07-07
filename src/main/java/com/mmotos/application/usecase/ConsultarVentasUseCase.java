package com.mmotos.application.usecase;

import com.mmotos.application.dto.*;
import com.mmotos.infrastructure.output.persistence.entity.PagoEntity;
import com.mmotos.infrastructure.output.persistence.entity.VentaDetalleEntity;
import com.mmotos.infrastructure.output.persistence.entity.VentaEntity;
import com.mmotos.infrastructure.output.persistence.entity.VentaModificacionEntity;
import com.mmotos.infrastructure.output.persistence.jpa.PagoJpaRepository;
import com.mmotos.infrastructure.output.persistence.jpa.UsuarioJpaRepository;
import com.mmotos.infrastructure.output.persistence.jpa.VentaDetalleJpaRepository;
import com.mmotos.infrastructure.output.persistence.jpa.VentaJpaRepository;
import com.mmotos.infrastructure.output.persistence.jpa.VentaModificacionJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Collections;

@Service
@Transactional(readOnly = true)
public class ConsultarVentasUseCase {

    private final VentaJpaRepository ventaRepo;
    private final VentaDetalleJpaRepository detalleRepo;
    private final PagoJpaRepository pagoRepo;
    private final UsuarioJpaRepository usuarioRepo;
    private final VentaModificacionJpaRepository modRepo;

    public ConsultarVentasUseCase(VentaJpaRepository ventaRepo,
                                  VentaDetalleJpaRepository detalleRepo,
                                  PagoJpaRepository pagoRepo,
                                  UsuarioJpaRepository usuarioRepo,
                                  VentaModificacionJpaRepository modRepo) {
        this.ventaRepo   = ventaRepo;
        this.detalleRepo = detalleRepo;
        this.pagoRepo    = pagoRepo;
        this.usuarioRepo = usuarioRepo;
        this.modRepo     = modRepo;
    }

    public VentaDetalleCompletoDTO porId(UUID id) {
        VentaEntity v = ventaRepo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta no encontrada"));

        List<LineaTicketDTO> lineas = detalleRepo.findByVentaId(id).stream()
            .map(d -> new LineaTicketDTO(
                d.getNombreHistorico(), d.getSkuHistorico(), d.getCantidad(),
                d.getPrecioUnitarioHistorico(),
                d.getPrecioUnitarioHistorico().multiply(BigDecimal.valueOf(d.getCantidad()))
            )).toList();

        List<PagoTicketDTO> pagos = pagoRepo.findByVentaId(id).stream()
            .map(p -> new PagoTicketDTO(p.getMetodo(), p.getMonto()))
            .toList();

        List<VentaModificacionDTO> mods = modRepo.findByVentaIdOrderByFechaDesc(id).stream()
            .map(m -> new VentaModificacionDTO(
                m.getId(), m.getUsuarioNombre(), m.getFecha(),
                m.getCampo(), m.getValorAnterior(), m.getValorNuevo(), m.getMotivo()
            )).toList();

        return new VentaDetalleCompletoDTO(
            v.getId(), v.getNumeroTicket(), v.getCae(),
            v.getEstadoFiscal(), v.getFechaEmision(), v.getTotalVenta(),
            lineas, pagos, v.isModificada(), mods
        );
    }

    /**
     * List ventas for a specific date range.
     */
    public List<VentaListDTO> listarVentas(LocalDate desde, LocalDate hasta) {
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin    = hasta.atTime(LocalTime.MAX);

        List<VentaEntity> ventas = ventaRepo.findByFechaEmisionBetween(inicio, fin);

        return ventas.stream().map(v -> {
            List<PagoEntity> pagos = pagoRepo.findByVentaId(v.getId());
            String cajero = v.getUsuarioId() != null
                ? usuarioRepo.findById(v.getUsuarioId())
                    .map(u -> u.getNombre())
                    .orElse("—")
                : "—";

            List<VentaListDTO.PagoResumenDTO> pagosDTO = pagos.stream()
                .map(p -> new VentaListDTO.PagoResumenDTO(p.getMetodo(), p.getMonto()))
                .toList();

            return new VentaListDTO(
                v.getId(),
                v.getNumeroTicket(),
                v.getTipoFactura(),
                v.getFechaEmision(),
                v.getTotalVenta(),
                v.getEstadoFiscal(),
                cajero,
                v.getClienteCuit(),
                pagosDTO,
                v.isAnulada(),
                v.isModificada()
            );
        }).toList();
    }

    /**
     * Get cash register summary for a specific date.
     */
    public CajaResumenDTO resumenCaja(LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin    = fecha.atTime(LocalTime.MAX);

        List<Object[]> resumenList = ventaRepo.resumenDiario(inicio, fin);
        Object[] resumen = resumenList.isEmpty() ? new Object[]{0L, BigDecimal.ZERO} : resumenList.get(0);
        long cantidadVentas = resumen[0] != null ? ((Number) resumen[0]).longValue() : 0;
        BigDecimal totalVentas = resumen[1] != null ? (BigDecimal) resumen[1] : BigDecimal.ZERO;

        // Aggregate payments by method
        List<VentaEntity> ventas = ventaRepo.findByFechaEmisionBetween(inicio, fin);
        BigDecimal totalEfectivo     = BigDecimal.ZERO;
        BigDecimal totalTarjeta      = BigDecimal.ZERO;
        BigDecimal totalTransferencia= BigDecimal.ZERO;
        BigDecimal totalMercadoPago  = BigDecimal.ZERO;
        BigDecimal totalOtros        = BigDecimal.ZERO;

        Set<String> EFECTIVO     = Set.of("EFECTIVO");
        Set<String> TARJETA      = Set.of("TARJETA", "TARJETA_CREDITO", "TARJETA_DEBITO");
        Set<String> TRANSFERENCIA= Set.of("TRANSFERENCIA");
        Set<String> MP           = Set.of("MERCADO_PAGO", "MERCADOPAGO");

        for (VentaEntity v : ventas) {
            List<PagoEntity> pagos = pagoRepo.findByVentaId(v.getId());
            for (PagoEntity p : pagos) {
                String metodo = p.getMetodo().toUpperCase();
                if (EFECTIVO.contains(metodo))           totalEfectivo     = totalEfectivo.add(p.getMonto());
                else if (TARJETA.contains(metodo))       totalTarjeta      = totalTarjeta.add(p.getMonto());
                else if (TRANSFERENCIA.contains(metodo)) totalTransferencia= totalTransferencia.add(p.getMonto());
                else if (MP.contains(metodo))            totalMercadoPago  = totalMercadoPago.add(p.getMonto());
                else                                     totalOtros        = totalOtros.add(p.getMonto());
            }
        }

        return new CajaResumenDTO(
            cantidadVentas, totalVentas,
            totalEfectivo, totalTarjeta, totalTransferencia, totalMercadoPago, totalOtros
        );
    }
}
