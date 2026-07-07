package com.mmotos.infrastructure.output.persistence.adapter;

import com.mmotos.domain.port.ConfiguracionRepository;
import com.mmotos.infrastructure.output.persistence.entity.ConfiguracionEntity;
import com.mmotos.infrastructure.output.persistence.jpa.ConfiguracionJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
public class ConfiguracionRepositoryAdapter implements ConfiguracionRepository {

    private static final String CLAVE_DOLAR = "COTIZACION_DOLAR";

    private final ConfiguracionJpaRepository jpaRepository;

    public ConfiguracionRepositoryAdapter(ConfiguracionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BigDecimal> getCotizacionDolar() {
        return jpaRepository.findById(CLAVE_DOLAR).map(ConfiguracionEntity::getValor);
    }

    @Override
    @Transactional
    public void setCotizacionDolar(BigDecimal valor, String usuarioIdStr) {
        UUID usuarioId = usuarioIdStr != null ? UUID.fromString(usuarioIdStr) : null;
        jpaRepository.findById(CLAVE_DOLAR).ifPresentOrElse(
            entity -> {
                entity.setValor(valor);
                entity.setUsuarioId(usuarioId);
                jpaRepository.save(entity);
            },
            () -> jpaRepository.save(new ConfiguracionEntity(CLAVE_DOLAR, valor, usuarioId))
        );
    }
}
