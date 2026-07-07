package com.mmotos.application.usecase;

import com.mmotos.infrastructure.output.persistence.entity.ProductoEntity;
import com.mmotos.infrastructure.output.persistence.entity.VentaDetalleEntity;
import com.mmotos.infrastructure.output.persistence.entity.VentaEntity;
import com.mmotos.infrastructure.output.persistence.jpa.ProductoJpaRepository;
import com.mmotos.infrastructure.output.persistence.jpa.VentaDetalleJpaRepository;
import com.mmotos.infrastructure.output.persistence.jpa.VentaJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnularVentaUseCaseTest {

    VentaJpaRepository ventaRepo = mock(VentaJpaRepository.class);
    VentaDetalleJpaRepository detalleRepo = mock(VentaDetalleJpaRepository.class);
    ProductoJpaRepository productoRepo = mock(ProductoJpaRepository.class);

    AnularVentaUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AnularVentaUseCase(ventaRepo, detalleRepo, productoRepo);
    }

    @Test
    @DisplayName("Venta existente no anulada → se anula y stock se restaura")
    void ventaExistenteSeAnulaCorrectamente() {
        UUID ventaId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();

        VentaEntity venta = new VentaEntity(ventaId, "B", java.time.LocalDateTime.now(),
            null, BigDecimal.valueOf(3000), "APROBADO", "SYNCED", null);

        VentaDetalleEntity detalle = new VentaDetalleEntity(
            UUID.randomUUID(), ventaId, productoId, "FLT-001", "Filtro", 3, BigDecimal.valueOf(1000));

        ProductoEntity producto = new ProductoEntity(productoId, "FLT-001", "Filtro",
            BigDecimal.valueOf(1000), "ARS", 2, 5, null, "A1", false);

        when(ventaRepo.findById(ventaId)).thenReturn(Optional.of(venta));
        when(detalleRepo.findByVentaId(ventaId)).thenReturn(List.of(detalle));
        when(productoRepo.findByIdWithLock(productoId)).thenReturn(Optional.of(producto));
        when(ventaRepo.save(any())).thenReturn(venta);
        when(productoRepo.save(any())).thenReturn(producto);

        useCase.ejecutar(ventaId, "Error de precio");

        assertThat(venta.isAnulada()).isTrue();
        assertThat(venta.getMotivoAnulacion()).isEqualTo("Error de precio");
        assertThat(venta.getFechaAnulacion()).isNotNull();
        assertThat(producto.getStockActual()).isEqualTo(5); // 2 + 3
        verify(ventaRepo).save(venta);
    }

    @Test
    @DisplayName("Venta ya anulada → lanza CONFLICT")
    void ventaYaAnuladaLanzaConflict() {
        UUID ventaId = UUID.randomUUID();
        VentaEntity venta = new VentaEntity(ventaId, "B", java.time.LocalDateTime.now(),
            null, BigDecimal.valueOf(1000), "APROBADO", "SYNCED", null);
        venta.setAnulada(true);

        when(ventaRepo.findById(ventaId)).thenReturn(Optional.of(venta));

        assertThatThrownBy(() -> useCase.ejecutar(ventaId, "motivo"))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("ya fue anulada");
    }

    @Test
    @DisplayName("Venta inexistente → lanza NOT_FOUND")
    void ventaInexistenteLanzaNotFound() {
        when(ventaRepo.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.ejecutar(UUID.randomUUID(), "motivo"))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("no encontrada");
    }

    @Test
    @DisplayName("Múltiples líneas → stock restaurado en todas")
    void multiplesLineasStockRestaurado() {
        UUID ventaId = UUID.randomUUID();
        UUID prod1 = UUID.randomUUID();
        UUID prod2 = UUID.randomUUID();

        VentaEntity venta = new VentaEntity(ventaId, "B", java.time.LocalDateTime.now(),
            null, BigDecimal.valueOf(5000), "APROBADO", "SYNCED", null);

        VentaDetalleEntity det1 = new VentaDetalleEntity(UUID.randomUUID(), ventaId, prod1, "A", "P1", 2, BigDecimal.TEN);
        VentaDetalleEntity det2 = new VentaDetalleEntity(UUID.randomUUID(), ventaId, prod2, "B", "P2", 5, BigDecimal.TEN);

        ProductoEntity p1 = new ProductoEntity(prod1, "A", "P1", BigDecimal.TEN, "ARS", 0, 5, null, "", false);
        ProductoEntity p2 = new ProductoEntity(prod2, "B", "P2", BigDecimal.TEN, "ARS", 1, 5, null, "", false);

        when(ventaRepo.findById(ventaId)).thenReturn(Optional.of(venta));
        when(detalleRepo.findByVentaId(ventaId)).thenReturn(List.of(det1, det2));
        when(productoRepo.findByIdWithLock(prod1)).thenReturn(Optional.of(p1));
        when(productoRepo.findByIdWithLock(prod2)).thenReturn(Optional.of(p2));
        when(ventaRepo.save(any())).thenReturn(venta);
        when(productoRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        useCase.ejecutar(ventaId, "test");

        assertThat(p1.getStockActual()).isEqualTo(2);  // 0 + 2
        assertThat(p2.getStockActual()).isEqualTo(6);  // 1 + 5
    }
}
