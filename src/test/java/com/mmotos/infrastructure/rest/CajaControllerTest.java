package com.mmotos.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmotos.application.dto.*;
import com.mmotos.application.usecase.ConsultarVentasUseCase;
import com.mmotos.application.usecase.GestionarCajaUseCase;
import com.mmotos.infrastructure.config.JwtService;
import com.mmotos.infrastructure.input.rest.CajaController;
import com.mmotos.infrastructure.output.persistence.jpa.UsuarioJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CajaController.class)
@Import({com.mmotos.infrastructure.config.SecurityConfig.class,
         com.mmotos.infrastructure.config.JwtAuthFilter.class})
class CajaControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean ConsultarVentasUseCase consultarVentasUseCase;
    @MockBean GestionarCajaUseCase gestionarCajaUseCase;
    @MockBean JwtService jwtService;
    @MockBean UsuarioJpaRepository usuarioRepo;

    private static final String FAKE_USUARIO_ID = UUID.randomUUID().toString();

    private CajaResumenDTO resumenVacio() {
        return new CajaResumenDTO(
            0L, BigDecimal.ZERO,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ZERO, BigDecimal.ZERO
        );
    }

    private CajaDTO cajaAbierta() {
        return new CajaDTO(
            UUID.randomUUID(), "Juan Pérez", "juanp",
            LocalDateTime.now(), null,
            new BigDecimal("5000"), null, null, null,
            null, "ABIERTA", resumenVacio()
        );
    }

    // ==================== RESUMEN ====================

    @Test
    @DisplayName("GET /api/caja/resumen con CAJERO devuelve 200 con resumen")
    @WithMockUser(roles = "CAJERO")
    void resumenConCajeroDevuelve200() throws Exception {
        CajaResumenDTO resumen = new CajaResumenDTO(
            5L, new BigDecimal("25000"),
            new BigDecimal("10000"), new BigDecimal("8000"), new BigDecimal("5000"),
            new BigDecimal("2000"), BigDecimal.ZERO
        );
        when(consultarVentasUseCase.resumenCaja(any())).thenReturn(resumen);

        mockMvc.perform(get("/api/caja/resumen"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cantidadVentas").value(5))
            .andExpect(jsonPath("$.totalVentas").value(25000));
    }

    // ==================== ACTIVA ====================

    @Test
    @DisplayName("GET /api/caja/activa sin caja abierta devuelve 204 No Content")
    @WithMockUser(roles = "CAJERO")
    void cajaActivaSinCajaDevuelve204() throws Exception {
        when(gestionarCajaUseCase.obtenerCajaActiva()).thenReturn(null);

        mockMvc.perform(get("/api/caja/activa"))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/caja/activa con caja abierta devuelve 200 con CajaDTO")
    @WithMockUser(roles = "CAJERO")
    void cajaActivaConCajaDevuelve200() throws Exception {
        when(gestionarCajaUseCase.obtenerCajaActiva()).thenReturn(cajaAbierta());

        mockMvc.perform(get("/api/caja/activa"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("ABIERTA"))
            .andExpect(jsonPath("$.cajeroUsername").value("juanp"));
    }

    // ==================== ABRIR ====================

    @Test
    @DisplayName("POST /api/caja/abrir con CAJERO devuelve 201 con CajaDTO")
    @WithMockUser(roles = "CAJERO")
    void abrirCajaConCajeroDevuelve201() throws Exception {
        doReturn(FAKE_USUARIO_ID).when(jwtService).extractClaim(any(), any());
        when(gestionarCajaUseCase.abrirCaja(any(), any())).thenReturn(cajaAbierta());

        AbrirCajaRequest req = new AbrirCajaRequest(new BigDecimal("5000"));

        mockMvc.perform(post("/api/caja/abrir")
                .header("Authorization", "Bearer fake-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.estado").value("ABIERTA"))
            .andExpect(jsonPath("$.montoInicial").value(5000));
    }

    // ==================== CERRAR ====================

    @Test
    @DisplayName("POST /api/caja/cerrar con CAJERO devuelve 200 con CajaDTO cerrada")
    @WithMockUser(roles = "CAJERO")
    void cerrarCajaConCajeroDevuelve200() throws Exception {
        doReturn(FAKE_USUARIO_ID).when(jwtService).extractClaim(any(), any());

        CajaDTO cajaCerrada = new CajaDTO(
            UUID.randomUUID(), "Juan Pérez", "juanp",
            LocalDateTime.now().minusHours(4), LocalDateTime.now(),
            new BigDecimal("5000"), new BigDecimal("12000"),
            new BigDecimal("11800"), new BigDecimal("-200"),
            "Cierre del día", "CERRADA", resumenVacio()
        );
        when(gestionarCajaUseCase.cerrarCaja(any(), any())).thenReturn(cajaCerrada);

        CerrarCajaRequest req = new CerrarCajaRequest(new BigDecimal("11800"), "Cierre del día");

        mockMvc.perform(post("/api/caja/cerrar")
                .header("Authorization", "Bearer fake-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("CERRADA"))
            .andExpect(jsonPath("$.montoFinalContado").value(11800));
    }

    // ==================== HISTORIAL ====================

    @Test
    @DisplayName("GET /api/caja/historial con CAJERO devuelve 403")
    @WithMockUser(roles = "CAJERO")
    void historialConCajeroDevuelve403() throws Exception {
        mockMvc.perform(get("/api/caja/historial"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/caja/historial con DUENO devuelve 200 con lista")
    @WithMockUser(roles = "DUENO")
    void historialConDuenoDevuelve200() throws Exception {
        when(gestionarCajaUseCase.historial(any(Integer.class))).thenReturn(List.of(cajaAbierta()));

        mockMvc.perform(get("/api/caja/historial"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].cajeroUsername").value("juanp"));
    }

    // ==================== SIN AUTH ====================

    @Test
    @DisplayName("GET /api/caja/resumen sin autenticación devuelve 401")
    void sinAutenticacionDevuelve401() throws Exception {
        mockMvc.perform(get("/api/caja/resumen"))
            .andExpect(status().isUnauthorized());
    }
}
