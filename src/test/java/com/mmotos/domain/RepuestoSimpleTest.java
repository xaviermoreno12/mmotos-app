package com.mmotos.domain;

import com.mmotos.domain.exception.InsufficientStockException;
import com.mmotos.domain.model.Moneda;
import com.mmotos.domain.model.Precio;
import com.mmotos.domain.model.RepuestoSimple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class RepuestoSimpleTest {

    private RepuestoSimple repuesto(int stock, int minimo) {
        return new RepuestoSimple(
            UUID.randomUUID(), "SKU-001", "Filtro de Aceite",
            new Precio(new BigDecimal("500"), Moneda.ARS, LocalDateTime.now()),
            stock, minimo, "A-4"
        );
    }

    @Test
    @DisplayName("Descuento válido reduce el stock correctamente")
    void descontarStockValido() {
        RepuestoSimple r = repuesto(10, 2);
        r.descontarStock(3);
        assertThat(r.getStockActual()).isEqualTo(7);
    }

    @Test
    @DisplayName("Descuento exacto al stock disponible deja stock en cero")
    void descontarTodoElStock() {
        RepuestoSimple r = repuesto(5, 1);
        r.descontarStock(5);
        assertThat(r.getStockActual()).isZero();
    }

    @Test
    @DisplayName("Stock insuficiente lanza InsufficientStockException sin modificar el stock")
    void stockInsuficienteLanzaExcepcion() {
        RepuestoSimple r = repuesto(2, 1);
        assertThatExceptionOfType(InsufficientStockException.class)
            .isThrownBy(() -> r.descontarStock(5))
            .withMessageContaining("SKU-001");
        assertThat(r.getStockActual()).isEqualTo(2);
    }

    @Test
    @DisplayName("estaBajoMinimo es true cuando stock <= mínimo")
    void estaBajoMinimo() {
        assertThat(repuesto(2, 2).estaBajoMinimo()).isTrue();
        assertThat(repuesto(1, 2).estaBajoMinimo()).isTrue();
        assertThat(repuesto(3, 2).estaBajoMinimo()).isFalse();
    }
}
