package com.mmotos.infrastructure.persistence;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mmotos.infrastructure.output.persistence.entity.ProductoEntity;
import com.mmotos.infrastructure.output.persistence.jpa.ProductoJpaRepository;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductoJpaRepositoryTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("mmotos_test")
        .withUsername("test")
        .withPassword("test");

    @BeforeAll
    static void startContainer() {
        Assumptions.assumeTrue(
            DockerClientFactory.instance().isDockerAvailable(),
            "Docker no disponible — se omiten tests de Testcontainers"
        );
        postgres.start();
    }

    @Autowired
    ProductoJpaRepository repository;

    private ProductoEntity buildProducto(String sku, int stock, int minimo, ObjectNode atributos) {
        return new ProductoEntity(
            UUID.randomUUID(), sku, "Producto " + sku,
            new BigDecimal("1000.00"), "ARS",
            stock, minimo, atributos, "A-1", false
        );
    }

    @Test
    @DisplayName("findBySkuAndActivoTrue devuelve el producto activo por SKU")
    void findBySkuDevuelveActivo() {
        repository.save(buildProducto("OIL-20W50", 10, 2, JsonNodeFactory.instance.objectNode()));

        assertThat(repository.findBySkuAndActivoTrue("OIL-20W50")).isPresent();
        assertThat(repository.findBySkuAndActivoTrue("SKU-INEXISTENTE")).isEmpty();
    }

    @Test
    @DisplayName("findBajoMinimo devuelve solo productos con stock <= mínimo")
    void findBajoMinimoDevuelveSoloLosCriteriosCumplen() {
        repository.save(buildProducto("LOW-001", 1, 5, JsonNodeFactory.instance.objectNode()));  // bajo
        repository.save(buildProducto("OK-001",  10, 5, JsonNodeFactory.instance.objectNode())); // ok

        List<ProductoEntity> bajoMinimo = repository.findBajoMinimo();

        assertThat(bajoMinimo).extracting(ProductoEntity::getSku)
            .containsExactly("LOW-001")
            .doesNotContain("OK-001");
    }

    @Test
    @DisplayName("buscarPorAtributos usa @> GIN y encuentra por atributo exacto")
    void buscarPorAtributosGin() {
        ObjectNode atributos = JsonNodeFactory.instance.objectNode();
        atributos.put("viscosidad", "20W50");
        atributos.put("litros", 1);
        repository.save(buildProducto("OIL-001", 5, 2, atributos));

        repository.save(buildProducto("OIL-002", 5, 2, JsonNodeFactory.instance.objectNode()));

        List<ProductoEntity> resultado = repository.buscarPorAtributos("{\"viscosidad\":\"20W50\"}");

        assertThat(resultado).extracting(ProductoEntity::getSku)
            .containsExactly("OIL-001");
    }
}
