package com.mmotos.infrastructure.rest;

import com.mmotos.application.dto.ProductoDTO;
import com.mmotos.application.usecase.ActualizarProductoUseCase;
import com.mmotos.application.usecase.BuscarProductoUseCase;
import com.mmotos.application.usecase.CrearProductoUseCase;
import com.mmotos.infrastructure.config.JwtService;
import com.mmotos.infrastructure.input.rest.ProductoController;
import com.mmotos.infrastructure.output.persistence.jpa.VentaDetalleJpaRepository;
import com.mmotos.infrastructure.output.persistence.jpa.VentaJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
@Import({com.mmotos.infrastructure.config.SecurityConfig.class,
         com.mmotos.infrastructure.config.JwtAuthFilter.class})
class ProductoControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean BuscarProductoUseCase buscarProductoUseCase;
    @MockBean CrearProductoUseCase crearProductoUseCase;
    @MockBean ActualizarProductoUseCase actualizarProductoUseCase;
    @MockBean VentaDetalleJpaRepository ventaDetalleRepo;
    @MockBean VentaJpaRepository ventaRepo;
    @MockBean JwtService jwtService;

    private ProductoDTO productoEjemplo() {
        return new ProductoDTO(UUID.randomUUID(), "FLT-001", "Filtro de Aceite",
            new BigDecimal("500"), "ARS", new BigDecimal("500"), 10, 2, false, "A-4", false, true, null);
    }

    @Test
    @DisplayName("GET /api/productos/{id} con producto existente devuelve 200")
    @WithMockUser(roles = "CAJERO")
    void porIdExistenteDevuelve200() throws Exception {
        UUID id = UUID.randomUUID();
        when(buscarProductoUseCase.porId(id)).thenReturn(Optional.of(productoEjemplo()));

        mockMvc.perform(get("/api/productos/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sku").value("FLT-001"))
            .andExpect(jsonPath("$.stockActual").value(10));
    }

    @Test
    @DisplayName("GET /api/productos/{id} con producto inexistente devuelve 404")
    @WithMockUser(roles = "CAJERO")
    void porIdInexistenteDevuelve404() throws Exception {
        when(buscarProductoUseCase.porId(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/productos/{id}", UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/productos/buscar con término devuelve lista")
    @WithMockUser(roles = "CAJERO")
    void buscarConTerminoDevuelveLista() throws Exception {
        when(buscarProductoUseCase.buscar(any())).thenReturn(List.of(productoEjemplo()));

        mockMvc.perform(get("/api/productos/buscar").param("termino", "filtro"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].sku").value("FLT-001"));
    }

    @Test
    @DisplayName("GET /api/productos sin autenticación devuelve 401")
    void sinAuthDevuelve401() throws Exception {
        mockMvc.perform(get("/api/productos/buscar").param("nombre", "test"))
            .andExpect(status().isUnauthorized());
    }
}
