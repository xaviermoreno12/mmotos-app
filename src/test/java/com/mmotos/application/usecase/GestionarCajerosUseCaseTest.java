package com.mmotos.application.usecase;

import com.mmotos.application.dto.ActualizarCajeroRequest;
import com.mmotos.application.dto.CajeroDTO;
import com.mmotos.application.dto.CrearCajeroRequest;
import com.mmotos.infrastructure.output.persistence.entity.UsuarioEntity;
import com.mmotos.infrastructure.output.persistence.jpa.UsuarioJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GestionarCajerosUseCaseTest {

    UsuarioJpaRepository usuarioRepository;
    PasswordEncoder passwordEncoder;
    GestionarCajerosUseCase useCase;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioJpaRepository.class);
        passwordEncoder   = mock(PasswordEncoder.class);
        useCase = new GestionarCajerosUseCase(usuarioRepository, passwordEncoder);
    }

    private UsuarioEntity entityCajero(UUID id) {
        return new UsuarioEntity(id, "Juan Pérez", "juanp", "hashed", "CAJERO");
    }

    @Test
    @DisplayName("listar() devuelve un DTO por cada usuario en el repositorio")
    void listarDevuelveListaCompleta() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.findAll()).thenReturn(List.of(entityCajero(id)));

        List<CajeroDTO> resultado = useCase.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).username()).isEqualTo("juanp");
        assertThat(resultado.get(0).rol()).isEqualTo("CAJERO");
    }

    @Test
    @DisplayName("crear() con username disponible guarda y devuelve DTO correcto")
    void crearConUsernameDisponibleDevuelveDTO() {
        UUID id = UUID.randomUUID();
        UsuarioEntity guardado = entityCajero(id);
        when(usuarioRepository.findByUsernameAndActivoTrue("juanp")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(usuarioRepository.save(any())).thenReturn(guardado);

        CrearCajeroRequest req = new CrearCajeroRequest("Juan Pérez", "juanp", "password123", "CAJERO");
        CajeroDTO resultado = useCase.crear(req);

        assertThat(resultado.username()).isEqualTo("juanp");
        assertThat(resultado.nombre()).isEqualTo("Juan Pérez");
        verify(usuarioRepository).save(any(UsuarioEntity.class));
    }

    @Test
    @DisplayName("crear() con username existente lanza CONFLICT")
    void crearConUsernameExistenteLanzaConflict() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.findByUsernameAndActivoTrue("juanp"))
            .thenReturn(Optional.of(entityCajero(id)));

        CrearCajeroRequest req = new CrearCajeroRequest("Juan Pérez", "juanp", "password123", "CAJERO");

        assertThatThrownBy(() -> useCase.crear(req))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(ex -> {
                ResponseStatusException rse = (ResponseStatusException) ex;
                assertThat(rse.getStatusCode().value()).isEqualTo(HttpStatus.CONFLICT.value());
                assertThat(rse.getReason()).isEqualTo("El username ya existe");
            });
    }

    @Test
    @DisplayName("actualizar() con ID inexistente lanza NOT_FOUND")
    void actualizarConIdInexistenteLanzaNotFound() {
        when(usuarioRepository.findById(any())).thenReturn(Optional.empty());

        ActualizarCajeroRequest req = new ActualizarCajeroRequest("Nuevo Nombre", null, null, null);

        assertThatThrownBy(() -> useCase.actualizar(UUID.randomUUID(), req))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(ex -> {
                ResponseStatusException rse = (ResponseStatusException) ex;
                assertThat(rse.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
                assertThat(rse.getReason()).isEqualTo("Cajero no encontrado");
            });
    }

    @Test
    @DisplayName("actualizar() cambia solo los campos no nulos (nombre sí, rol no)")
    void actualizarCambiarSoloCamposNoNulos() {
        UUID id = UUID.randomUUID();
        UsuarioEntity entity = entityCajero(id);
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(entity));
        when(usuarioRepository.save(any())).thenReturn(entity);

        ActualizarCajeroRequest req = new ActualizarCajeroRequest("Nombre Nuevo", null, null, null);
        useCase.actualizar(id, req);

        assertThat(entity.getNombre()).isEqualTo("Nombre Nuevo");
        assertThat(entity.getRol()).isEqualTo("CAJERO");
        verify(usuarioRepository).save(entity);
    }
}
