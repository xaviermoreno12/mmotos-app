package com.mmotos.application.usecase;

import com.mmotos.application.dto.ActualizarCajeroRequest;
import com.mmotos.application.dto.CajeroDTO;
import com.mmotos.application.dto.CrearCajeroRequest;
import com.mmotos.infrastructure.output.persistence.entity.UsuarioEntity;
import com.mmotos.infrastructure.output.persistence.jpa.UsuarioJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class GestionarCajerosUseCase {

    private final UsuarioJpaRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public GestionarCajerosUseCase(UsuarioJpaRepository usuarioRepository,
                                   PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder   = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<CajeroDTO> listar() {
        return usuarioRepository.findAll().stream()
            .map(this::toDTO)
            .toList();
    }

    @Transactional
    public CajeroDTO crear(CrearCajeroRequest req) {
        if (usuarioRepository.findByUsernameAndActivoTrue(req.username()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El username ya existe");
        }
        var entity = new UsuarioEntity(
            UUID.randomUUID(),
            req.nombre(),
            req.username(),
            passwordEncoder.encode(req.password()),
            req.rol()
        );
        return toDTO(usuarioRepository.save(entity));
    }

    @Transactional
    public CajeroDTO actualizar(UUID id, ActualizarCajeroRequest req) {
        var entity = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cajero no encontrado"));

        if (req.nombre() != null && !req.nombre().isBlank()) entity.setNombre(req.nombre());
        if (req.rol() != null && !req.rol().isBlank()) entity.setRol(req.rol());
        if (req.activo() != null) entity.setActivo(req.activo());
        if (req.password() != null && !req.password().isBlank()) {
            entity.setPasswordHash(passwordEncoder.encode(req.password()));
        }
        return toDTO(usuarioRepository.save(entity));
    }

    private CajeroDTO toDTO(UsuarioEntity e) {
        return new CajeroDTO(
            e.getId().toString(),
            e.getNombre(),
            e.getUsername(),
            e.getRol(),
            e.isActivo(),
            e.getCreatedAt() != null ? e.getCreatedAt().toString() : null
        );
    }
}
