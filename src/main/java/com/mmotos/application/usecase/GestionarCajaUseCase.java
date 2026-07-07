package com.mmotos.application.usecase;

import com.mmotos.application.dto.*;
import com.mmotos.infrastructure.output.persistence.entity.CajaEntity;
import com.mmotos.infrastructure.output.persistence.entity.PagoEntity;
import com.mmotos.infrastructure.output.persistence.entity.VentaEntity;
import com.mmotos.infrastructure.output.persistence.jpa.CajaJpaRepository;
import com.mmotos.infrastructure.output.persistence.jpa.PagoJpaRepository;
import com.mmotos.infrastructure.output.persistence.jpa.UsuarioJpaRepository;
import com.mmotos.infrastructure.output.persistence.jpa.VentaJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class GestionarCajaUseCase {

    private final CajaJpaRepository cajaRepo;
    private final VentaJpaRepository ventaRepo;
    private final PagoJpaRepository pagoRepo;
    private final UsuarioJpaRepository usuarioRepo;

    public GestionarCajaUseCase(CajaJpaRepository cajaRepo,
                                 VentaJpaRepository ventaRepo,
                                 PagoJpaRepository pagoRepo,
                                 UsuarioJpaRepository usuarioRepo) {
        this.cajaRepo    = cajaRepo;
        this.ventaRepo   = ventaRepo;
        this.pagoRepo    = pagoRepo;
        this.usuarioRepo = usuarioRepo;
    }

    /**
     * Abrir una nueva caja. Solo se permite una caja abierta a la vez.
     */
    @Transactional
    public CajaDTO abrirCaja(UUID usuarioId, AbrirCajaRequest request) {
        // Validar que no haya otra caja abierta
        cajaRepo.findCajaActiva().ifPresent(c -> {
            throw new IllegalStateException("Ya existe una caja abierta. Ciérrela antes de abrir una nueva.");
        });

        CajaEntity caja = new CajaEntity(UUID.randomUUID(), usuarioId, request.montoInicial());
        cajaRepo.save(caja);

        return toCajaDTO(caja);
    }

    /**
     * Cerrar la caja activa. Calcula el monto del sistema y la diferencia.
     */
    @Transactional
    public CajaDTO cerrarCaja(UUID usuarioId, CerrarCajaRequest request) {
        CajaEntity caja = cajaRepo.findCajaActiva()
            .orElseThrow(() -> new IllegalStateException("No hay una caja abierta para cerrar."));

        // Calcular monto del sistema: monto_inicial + total efectivo ventas del turno
        BigDecimal totalEfectivoVentas = calcularEfectivoVentas(caja);
        BigDecimal montoFinalSistema = caja.getMontoInicial().add(totalEfectivoVentas);

        caja.cerrar(montoFinalSistema, request.montoFinalContado(), request.observaciones());
        cajaRepo.save(caja);

        return toCajaDTO(caja);
    }

    /**
     * Obtener la caja activa con su resumen de ventas.
     */
    @Transactional(readOnly = true)
    public CajaDTO obtenerCajaActiva() {
        CajaEntity caja = cajaRepo.findCajaActiva()
            .orElse(null);

        if (caja == null) return null;

        return toCajaDTO(caja);
    }

    /**
     * Historial de cajas (últimas N).
     */
    @Transactional(readOnly = true)
    public List<CajaDTO> historial(int dias) {
        LocalDateTime desde = LocalDateTime.now().minusDays(dias).with(LocalTime.MIN);
        LocalDateTime hasta = LocalDateTime.now();
        return cajaRepo.findByFechaAperturaBetween(desde, hasta)
            .stream()
            .map(this::toCajaDTO)
            .toList();
    }

    // ======================== HELPERS ========================

    private CajaDTO toCajaDTO(CajaEntity caja) {
        String cajeroNombre = "—";
        String cajeroUsername = "—";
        var usuario = usuarioRepo.findById(caja.getUsuarioId());
        if (usuario.isPresent()) {
            cajeroNombre = usuario.get().getNombre();
            cajeroUsername = usuario.get().getUsername();
        }

        CajaResumenDTO resumen = calcularResumen(caja);

        return new CajaDTO(
            caja.getId(),
            cajeroNombre,
            cajeroUsername,
            caja.getFechaApertura(),
            caja.getFechaCierre(),
            caja.getMontoInicial(),
            caja.getMontoFinalSistema(),
            caja.getMontoFinalContado(),
            caja.getDiferencia(),
            caja.getObservaciones(),
            caja.getEstado(),
            resumen
        );
    }

    /**
     * Calcula el resumen de ventas realizadas durante esta caja (por fecha de apertura/cierre).
     */
    private CajaResumenDTO calcularResumen(CajaEntity caja) {
        LocalDateTime desde = caja.getFechaApertura();
        LocalDateTime hasta = caja.getFechaCierre() != null ? caja.getFechaCierre() : LocalDateTime.now();

        List<VentaEntity> ventas = ventaRepo.findByFechaEmisionBetween(desde, hasta);

        long cantidadVentas = ventas.size();
        BigDecimal totalVentas       = BigDecimal.ZERO;
        BigDecimal totalEfectivo     = BigDecimal.ZERO;
        BigDecimal totalTarjeta      = BigDecimal.ZERO;
        BigDecimal totalTransferencia = BigDecimal.ZERO;
        BigDecimal totalMercadoPago  = BigDecimal.ZERO;
        BigDecimal totalOtros        = BigDecimal.ZERO;

        Set<String> EFECTIVO      = Set.of("EFECTIVO");
        Set<String> TARJETA       = Set.of("TARJETA", "TARJETA_CREDITO", "TARJETA_DEBITO");
        Set<String> TRANSFERENCIA = Set.of("TRANSFERENCIA");
        Set<String> MP            = Set.of("MERCADO_PAGO", "MERCADOPAGO");

        for (VentaEntity v : ventas) {
            totalVentas = totalVentas.add(v.getTotalVenta());
            List<PagoEntity> pagos = pagoRepo.findByVentaId(v.getId());
            for (PagoEntity p : pagos) {
                String metodo = p.getMetodo().toUpperCase();
                if (EFECTIVO.contains(metodo))           totalEfectivo      = totalEfectivo.add(p.getMonto());
                else if (TARJETA.contains(metodo))       totalTarjeta       = totalTarjeta.add(p.getMonto());
                else if (TRANSFERENCIA.contains(metodo)) totalTransferencia = totalTransferencia.add(p.getMonto());
                else if (MP.contains(metodo))            totalMercadoPago   = totalMercadoPago.add(p.getMonto());
                else                                     totalOtros         = totalOtros.add(p.getMonto());
            }
        }

        return new CajaResumenDTO(
            cantidadVentas, totalVentas,
            totalEfectivo, totalTarjeta, totalTransferencia, totalMercadoPago, totalOtros
        );
    }

    /**
     * Calcula solo el efectivo de las ventas durante esta caja.
     */
    private BigDecimal calcularEfectivoVentas(CajaEntity caja) {
        LocalDateTime desde = caja.getFechaApertura();
        LocalDateTime hasta = LocalDateTime.now();

        List<VentaEntity> ventas = ventaRepo.findByFechaEmisionBetween(desde, hasta);
        BigDecimal totalEfectivo = BigDecimal.ZERO;

        for (VentaEntity v : ventas) {
            List<PagoEntity> pagos = pagoRepo.findByVentaId(v.getId());
            for (PagoEntity p : pagos) {
                if ("EFECTIVO".equalsIgnoreCase(p.getMetodo())) {
                    totalEfectivo = totalEfectivo.add(p.getMonto());
                }
            }
        }

        return totalEfectivo;
    }
}
