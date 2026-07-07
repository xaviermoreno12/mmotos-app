package com.mmotos.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmotos.application.dto.ActualizarCajeroRequest;
import com.mmotos.application.dto.CajeroDTO;
import com.mmotos.application.dto.CrearCajeroRequest;
import com.mmotos.application.usecase.GestionarCajerosUseCase;
import com.mmotos.infrastructure.config.JwtService;
import com.mmotos.infrastructure.input.rest.CajeroController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CajeroController.class)
@Import({com.mmotos.infrastructure.config.SecurityConfig.class,
         com.mmotos.infrastructure.config.JwtAuthFilter.class})
class CajeroControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean GestionarCajerosUseCase useCase;
    @MockBean JwtService jwtService;

    private CajeroDTO cajeroDTO() {
        return new CajeroDTO(
            UUID.randomUUID().toString(),
            "Juan Pérez", "juanp", "CAJERO", true, "2026-01-15T10:00:00"
        );
    }

    private CrearCajeroRequest requestCrear() {
        return new CrearCajeroRequest("Juan Pérez", "juanp", "password123", "CAJERO");
    }

    @Test
    @DisplayName("GET /api/cajeros con CAJERO devuelve 200 con lista")
    @WithMockUser(roles = "CAJERO")
    void listarConCajeroDevuelve200() throws Exception {
        when(useCase.listar()).thenReturn(List.of(cajeroDTO()));

        mockMvc.perform(get("/api/cajeros"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].username").value("juanp"))
            .andExpect(jsonPath("$[0].rol").value("CAJERO"));
    }

    @Test
    @DisplayName("POST /api/cajeros con CAJERO devuelve 403")
    @WithMockUser(roles = "CAJERO")
    void crearConCajeroDevuelve403() throws Exception {
        mockMvc.perform(post("/api/cajeros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestCrear())))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/cajeros con DUENO devuelve 201 con cajeroDTO")
    @WithMockUser(roles = "DUENO")
    void crearConDuenoDevuelve201() throws Exception {
        when(useCase.crear(any())).thenReturn(cajeroDTO());

        mockMvc.perform(post("/api/cajeros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestCrear())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username").value("juanp"))
            .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @DisplayName("POST /api/cajeros con username duplicado devuelve 409 con detail")
    @WithMockUser(roles = "DUENO")
    void crearUsernamesDuplicadoDevuelve409() throws Exception {
        when(useCase.crear(any()))
            .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "El username ya existe"));

        mockMvc.perform(post("/api/cajeros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestCrear())))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("El username ya existe"));
    }

    @Test
    @DisplayName("PUT /api/cajeros/{id} con DUENO devuelve 200 con cajero actualizado")
    @WithMockUser(roles = "DUENO")
    void actualizarConDuenoDevuelve200() throws Exception {
        CajeroDTO actualizado = new CajeroDTO(
            UUID.randomUUID().toString(),
            "Juan Actualizado", "juanp", "DUENO", true, "2026-01-15T10:00:00"
        );
        when(useCase.actualizar(any(), any())).thenReturn(actualizado);

        ActualizarCajeroRequest req = new ActualizarCajeroRequest("Juan Actualizado", null, "DUENO", null);

        mockMvc.perform(put("/api/cajeros/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Juan Actualizado"))
            .andExpect(jsonPath("$.rol").value("DUENO"));
    }

    @Test
    @DisplayName("GET /api/cajeros sin autenticación devuelve 401")
    void sinAutenticacionDevuelve401() throws Exception {
        mockMvc.perform(get("/api/cajeros"))
            .andExpect(status().isUnauthorized());
    }
}
