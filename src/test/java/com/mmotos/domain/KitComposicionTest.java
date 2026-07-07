package com.mmotos.domain;

import com.mmotos.domain.exception.InsufficientStockException;
import com.mmotos.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class KitComposicionTest {

    private RepuestoSimple componente(String sku, int stock) {
        return new RepuestoSimple(UUID.randomUUID(), sku, "Componente " + sku,
            new Precio(new BigDecimal("100"), Moneda.ARS, LocalDateTime.now()), stock, 1, null);
    }

    private KitComposicion kitDe(RepuestoSimple cadena, RepuestoSimple pinon, RepuestoSimple corona) {
        return new KitComposicion(UUID.randomUUID(), "KIT-001", "Kit Transmisión",
            List.of(new ComponenteKit(cadena, 1), new ComponenteKit(pinon, 1), new ComponenteKit(corona, 1)),
            1, "B-2");
    }

    @Test
    @DisplayName("Descontar kit válido reduce stock de todos los componentes")
    void descontarKitValido() {
        var cadena = componente("CAD-001", 5);
        var pinon  = componente("PIN-001", 5);
        var corona = componente("COR-001", 5);
        var kit    = kitDe(cadena, pinon, corona);

        kit.descontarStock(2);

        assertThat(cadena.getStockActual()).isEqualTo(3);
        assertThat(pinon.getStockActual()).isEqualTo(3);
        assertThat(corona.getStockActual()).isEqualTo(3);
    }

    @Test
    @DisplayName("Si un componente no tiene stock, NO se modifica ningún componente (atomicidad)")
    void falloEnUnComponenteNoModificaNinguno() {
        var cadena = componente("CAD-001", 5);
        var pinon  = componente("PIN-001", 1);   // stock insuficiente para 2
        var corona = componente("COR-001", 5);
        var kit    = kitDe(cadena, pinon, corona);

        assertThatExceptionOfType(InsufficientStockException.class)
            .isThrownBy(() -> kit.descontarStock(2))
            .withMessageContaining("PIN-001");

        // Garantía atómica del Composite Pattern: ninguno se tocó
        assertThat(cadena.getStockActual()).isEqualTo(5);
        assertThat(pinon.getStockActual()).isEqualTo(1);
        assertThat(corona.getStockActual()).isEqualTo(5);
    }

    @Test
    @DisplayName("Precio final usa precioPromocional si está seteado")
    void precioPromocionalTienePrioridad() {
        var kit = kitDe(componente("C1", 5), componente("C2", 5), componente("C3", 5));
        kit.setPrecioPromocional(new BigDecimal("250.00"));

        assertThat(kit.obtenerPrecioFinal(new BigDecimal("1000")))
            .isEqualByComparingTo("250.00");
    }

    @Test
    @DisplayName("Precio final suma componentes si no hay precio promocional")
    void precioFinalSumaComponentes() {
        var kit = kitDe(componente("C1", 5), componente("C2", 5), componente("C3", 5));
        // 3 componentes × $100 ARS = $300
        assertThat(kit.obtenerPrecioFinal(new BigDecimal("1000")))
            .isEqualByComparingTo("300.00");
    }
}
