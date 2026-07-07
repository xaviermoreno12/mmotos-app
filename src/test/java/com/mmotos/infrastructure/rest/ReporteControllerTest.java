package com.mmotos.infrastructure.rest;

import com.mmotos.application.usecase.GenerarReporteUseCase;
import com.mmotos.infrastructure.config.JwtService;
import com.mmotos.infrastructure.input.rest.ReporteController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReporteController.class)
@Import({com.mmotos.infrastructure.config.SecurityConfig.class,
         com.mmotos.infrastructure.config.JwtAuthFilter.class})
class ReporteControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean GenerarReporteUseCase useCase;
    @MockBean JwtService jwtService;

    private static final byte[] FAKE_BYTES = new byte[]{1, 2, 3};

    @Test
    @DisplayName("GET /api/reportes/ventas con CAJERO devuelve 200 Content-Type xlsx")
    @WithMockUser(roles = "CAJERO")
    void ventasDevuelve200Xlsx() throws Exception {
        when(useCase.reporteVentas(any(), any(), any())).thenReturn(FAKE_BYTES);

        mockMvc.perform(get("/api/reportes/ventas")
                .param("desde", "2026-05-01")
                .param("hasta", "2026-05-31"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @DisplayName("GET /api/reportes/stock-bajo con CAJERO devuelve 200 Content-Type xlsx")
    @WithMockUser(roles = "CAJERO")
    void stockBajoDevuelve200Xlsx() throws Exception {
        when(useCase.reporteStockBajo(any())).thenReturn(FAKE_BYTES);

        mockMvc.perform(get("/api/reportes/stock-bajo"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @DisplayName("GET /api/reportes/caja con CAJERO devuelve 200 Content-Type xlsx")
    @WithMockUser(roles = "CAJERO")
    void cajaDevuelve200Xlsx() throws Exception {
        when(useCase.reporteCaja(any(), any())).thenReturn(FAKE_BYTES);

        mockMvc.perform(get("/api/reportes/caja"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @DisplayName("GET /api/reportes/ventas/preview con CAJERO devuelve 200 con headers y rows")
    @WithMockUser(roles = "CAJERO")
    void ventasPreviewDevuelve200ConJson() throws Exception {
        Map<String, Object> preview = Map.of(
            "headers", List.of("Ticket", "Total"),
            "rows", List.of(List.of("INT-001", 5000))
        );
        when(useCase.previewVentas(any(), any())).thenReturn(preview);

        mockMvc.perform(get("/api/reportes/ventas/preview")
                .param("desde", "2026-05-01")
                .param("hasta", "2026-05-31"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.headers").isArray())
            .andExpect(jsonPath("$.rows").isArray());
    }

    @Test
    @DisplayName("GET /api/reportes/ventas sin desde/hasta devuelve 400")
    @WithMockUser(roles = "CAJERO")
    void ventasSinParamsDevuelve400() throws Exception {
        mockMvc.perform(get("/api/reportes/ventas"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/reportes/ventas sin autenticación devuelve 401")
    void sinAutenticacionDevuelve401() throws Exception {
        mockMvc.perform(get("/api/reportes/ventas")
                .param("desde", "2026-05-01")
                .param("hasta", "2026-05-31"))
            .andExpect(status().isUnauthorized());
    }
}
