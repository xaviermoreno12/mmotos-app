package com.mmotos.application.usecase;

import com.mmotos.application.dto.CotizacionRequest;
import com.mmotos.domain.port.ConfiguracionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

// Actualiza la cotización del dólar en configuracion_sistema.
// El cálculo de precios en pesos es dinámico (Precio.calcularEnPesos) —
// no requiere UPDATE masivo de productos, solo actualizar este valor global.
@Service
public class ActualizarCotizacionUseCase {

    private static final Logger log = LoggerFactory.getLogger(ActualizarCotizacionUseCase.class);

    private final ConfiguracionRepository configuracionRepository;

    public ActualizarCotizacionUseCase(ConfiguracionRepository configuracionRepository) {
        this.configuracionRepository = configuracionRepository;
    }

    @Transactional
    public BigDecimal ejecutar(CotizacionRequest request) {
        BigDecimal anterior = configuracionRepository.getCotizacionDolar().orElse(BigDecimal.ZERO);

        configuracionRepository.setCotizacionDolar(request.valorDolar(), request.usuarioId());

        log.info("Cotización dólar actualizada: {} → {} (por usuario: {})",
            anterior, request.valorDolar(), request.usuarioId());

        return request.valorDolar();
    }
}
