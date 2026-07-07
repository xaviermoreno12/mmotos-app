package com.mmotos.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmotos.application.dto.*;
import com.mmotos.application.usecase.AnularVentaUseCase;
import com.mmotos.application.usecase.ConsultarVentasUseCase;
import com.mmotos.application.usecase.RealizarVentaUseCase;
import com.mmotos.domain.exception.InsufficientStockException;
import com.mmotos.infrastructure.config.JwtService;
import com.mmotos.infrastructure.input.rest.VentaController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VentaController.class)
@Import({com.mmotos.infrastructure.config.SecurityConfig.class,
         com.mmotos.infrastructure.config.JwtAuthFilter.class})
class VentaControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean RealizarVentaUseCase realizarVentaUseCase;
    @MockBean ConsultarVentasUseCase consultarVentasUseCase;
    @MockBean AnularVentaUseCase anularVentaUseCase;
    @MockBean JwtService jwtService;

    private VentaRequest requestValido() {
        return new VentaRequest(
            "B", null,
            List.of(new LineaVentaRequest(UUID.randomUUID(), 2)),
            List.of(new PagoRequest("EFECTIVO", new BigDecimal("1000"), null, null, null, null)),
            UUID.randomUUID()
        );
    }

    @Test
    @DisplayName("POST /api/ventas con CAJERO devuelve 201 con datos fiscales")
    @WithMockUser(roles = "CAJERO")
    void realizarVentaExitosa() throws Exception {
        VentaResponse respuesta = new VentaResponse(
            UUID.randomUUID(), "INT-20260509-00001", null,
            "APROBADO", "SYNCED", new BigDecimal("1000"), LocalDateTime.now()
        );
        when(realizarVentaUseCase.ejecutar(any())).thenReturn(respuesta);

        mockMvc.perform(post("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestValido())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.estadoFiscal").value("APROBADO"))
            .andExpect(jsonPath("$.numeroTicket").value("INT-20260509-00001"));
    }

    @Test
    @DisplayName("POST /api/ventas sin autenticación devuelve 401")
    void sinAutenticacionDevuelve401() throws Exception {
        mockMvc.perform(post("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestValido())))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/ventas con stock insuficiente devuelve 409")
    @WithMockUser(roles = "CAJERO")
    void stockInsuficienteDevuelve409() throws Exception {
        when(realizarVentaUseCase.ejecutar(any()))
            .thenThrow(new InsufficientStockException("Stock insuficiente para SKU-001"));

        mockMvc.perform(post("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestValido())))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.title").value("Stock insuficiente"));
    }

    @Test
    @DisplayName("POST /api/ventas con body inválido (lineas vacías) devuelve 400 con detalle de campos")
    @WithMockUser(roles = "CAJERO")
    void bodyInvalidoDevuelve400() throws Exception {
        VentaRequest invalido = new VentaRequest("B", null, List.of(), List.of(), null);

        mockMvc.perform(post("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalido)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errores").exists());
    }
}
