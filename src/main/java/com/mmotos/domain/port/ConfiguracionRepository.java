package com.mmotos.domain.port;

import java.math.BigDecimal;
import java.util.Optional;

public interface ConfiguracionRepository {

    Optional<BigDecimal> getCotizacionDolar();

    void setCotizacionDolar(BigDecimal valor, String usuarioId);
}
