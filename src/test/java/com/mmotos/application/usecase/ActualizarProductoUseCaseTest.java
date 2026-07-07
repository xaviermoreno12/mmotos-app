package com.mmotos.application.usecase;

import com.mmotos.application.dto.ActualizarProductoRequest;
import com.mmotos.application.dto.ProductoDTO;
import com.mmotos.domain.port.ConfiguracionRepository;
import com.mmotos.infrastructure.output.persistence.entity.ProductoEntity;
import com.mmotos.infrastructure.output.persistence.jpa.ProductoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ActualizarProductoUseCaseTest {

    ProductoJpaRepository repo;
    ConfiguracionRepository configuracionRepository;
    ActualizarProductoUseCase useCase;

    @BeforeEach
    void setUp() {
        repo                    = mock(ProductoJpaRepository.class);
        configuracionRepository = mock(ConfiguracionRepository.class);
        useCase = new ActualizarProductoUseCase(repo, configuracionRepository);
        when(configuracionRepository.getCotizacionDolar())
            .thenReturn(Optional.of(new BigDecimal("1200")));
    }

    private ProductoEntity productoArs(UUID id) {
        return new ProductoEntity(
            id, "FLT-001", "Filtro de Aceite",
            new BigDecimal("1000"), "ARS",
            10, 2, null, "Estante A1", false
        );
    }

    @Test
    @DisplayName("actualizar() solo modifica campos no nulos")
    void soloModificaCamposNoNulos() {
        UUID id = UUID.randomUUID();
        ProductoEntity entity = productoArs(id);
        when(repo.findById(id)).thenReturn(Optional.of(entity));

        ActualizarProductoRequest req = new ActualizarProductoRequest(
            "Filtro Premium", null, null, null, null, null, null, null
        );
        ProductoDTO resultado = useCase.actualizar(id, req);

        assertThat(resultado.nombre()).isEqualTo("Filtro Premium");
        assertThat(resultado.precioBase()).isEqualByComparingTo("1000");
        assertThat(resultado.moneda()).isEqualTo("ARS");
    }

    @Test
    @DisplayName("actualizar() con ID inexistente lanza NOT_FOUND")
    void idInexistenteLanzaNotFound() {
        when(repo.findById(any())).thenReturn(Optional.empty());

        ActualizarProductoRequest req = new ActualizarProductoRequest(
            null, null, null, null, null, null, null, null
        );

        assertThatThrownBy(() -> useCase.actualizar(UUID.randomUUID(), req))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(ex -> {
                ResponseStatusException rse = (ResponseStatusException) ex;
                assertThat(rse.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
                assertThat(rse.getReason()).isEqualTo("Producto no encontrado");
            });
    }

    @Test
    @DisplayName("actualizar() con nuevo precio devuelve DTO con el precio actualizado")
    void nuevoPrecioSeReflejaEnDTO() {
        UUID id = UUID.randomUUID();
        ProductoEntity entity = productoArs(id);
        when(repo.findById(id)).thenReturn(Optional.of(entity));

        ActualizarProductoRequest req = new ActualizarProductoRequest(
            null, new BigDecimal("2500"), null, null, null, null, null, null
        );
        ProductoDTO resultado = useCase.actualizar(id, req);

        assertThat(resultado.precioBase()).isEqualByComparingTo("2500");
        assertThat(resultado.precioEnPesos()).isEqualByComparingTo("2500");
    }

    @Test
    @DisplayName("actualizar() con nuevo stock devuelve DTO con stockActual actualizado")
    void nuevoStockSeReflejaEnDTO() {
        UUID id = UUID.randomUUID();
        ProductoEntity entity = productoArs(id);
        when(repo.findById(id)).thenReturn(Optional.of(entity));

        ActualizarProductoRequest req = new ActualizarProductoRequest(
            null, null, null, 25, null, null, null, null
        );
        ProductoDTO resultado = useCase.actualizar(id, req);

        assertThat(resultado.stockActual()).isEqualTo(25);
        assertThat(resultado.bajominimo()).isFalse();
    }
}
