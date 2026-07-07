package com.mmotos.domain;

import com.mmotos.domain.model.Moneda;
import com.mmotos.domain.model.Precio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class PrecioTest {

    @Test
    @DisplayName("Precio en ARS se devuelve sin conversión")
    void precioEnArsSinConversion() {
        Precio precio = new Precio(new BigDecimal("1500.00"), Moneda.ARS, LocalDateTime.now());
        assertThat(precio.calcularEnPesos(new BigDecimal("1100")))
            .isEqualByComparingTo("1500.00");
    }

    @Test
    @DisplayName("Precio en USD se convierte multiplicando por cotización")
    void precioEnUsdConvierte() {
        Precio precio = new Precio(new BigDecimal("10.00"), Moneda.USD, LocalDateTime.now());
        assertThat(precio.calcularEnPesos(new BigDecimal("1100.00")))
            .isEqualByComparingTo("11000.00");
    }

    @Test
    @DisplayName("Redondeo HALF_UP a 2 decimales")
    void redondeoHalfUp() {
        Precio precio = new Precio(new BigDecimal("10.999"), Moneda.USD, LocalDateTime.now());
        assertThat(precio.calcularEnPesos(new BigDecimal("100")))
            .isEqualByComparingTo("1099.90");
    }

    @Test
    @DisplayName("Precio negativo lanza excepción en construcción")
    void precioNegativoLanzaExcepcion() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new Precio(new BigDecimal("-1"), Moneda.ARS, LocalDateTime.now()));
    }

    @Test
    @DisplayName("Cotización inválida (cero) lanza excepción al convertir")
    void cotizacionCeroLanzaExcepcion() {
        Precio precio = new Precio(new BigDecimal("10"), Moneda.USD, LocalDateTime.now());
        assertThatIllegalArgumentException()
            .isThrownBy(() -> precio.calcularEnPesos(BigDecimal.ZERO));
    }
}
