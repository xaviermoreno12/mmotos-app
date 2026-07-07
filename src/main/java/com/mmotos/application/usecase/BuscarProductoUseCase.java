package com.mmotos.application.usecase;

import com.mmotos.application.dto.ProductoDTO;
import com.mmotos.application.dto.ProductoFiltroDTO;
import com.mmotos.domain.port.ConfiguracionRepository;
import com.mmotos.infrastructure.output.persistence.entity.ProductoEntity;
import com.mmotos.infrastructure.output.persistence.jpa.ProductoJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BuscarProductoUseCase {

    private final ProductoJpaRepository productoJpaRepository;
    private final ConfiguracionRepository configuracionRepository;

    public BuscarProductoUseCase(ProductoJpaRepository productoJpaRepository,
                                  ConfiguracionRepository configuracionRepository) {
        this.productoJpaRepository = productoJpaRepository;
        this.configuracionRepository = configuracionRepository;
    }

    @Transactional(readOnly = true)
    public Optional<ProductoDTO> porId(UUID id) {
        BigDecimal cotizacion = getCotizacion();
        return productoJpaRepository.findById(id).map(e -> toDTO(e, cotizacion));
    }

    @Transactional(readOnly = true)
    public Optional<ProductoDTO> porSku(String sku) {
        BigDecimal cotizacion = getCotizacion();
        return productoJpaRepository.findBySkuAndActivoTrue(sku).map(e -> toDTO(e, cotizacion));
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> buscar(ProductoFiltroDTO filtro) {
        BigDecimal cotizacion = getCotizacion();

        if (filtro.sku() != null && !filtro.sku().isBlank()) {
            return productoJpaRepository.findBySkuAndActivoTrue(filtro.sku())
                .map(e -> List.of(toDTO(e, cotizacion)))
                .orElse(List.of());
        }

        if (filtro.atributoJson() != null && !filtro.atributoJson().isBlank()) {
            // Usa operador @> con índice GIN — búsqueda exacta de atributos
            return productoJpaRepository.buscarPorAtributos(filtro.atributoJson())
                .stream().map(e -> toDTO(e, cotizacion)).toList();
        }

        if (filtro.terminoParcial() != null && !filtro.terminoParcial().isBlank()) {
            // Búsqueda combinada: nombre fuzzy + atributos parciales (pg_trgm)
            List<ProductoEntity> porNombre = productoJpaRepository.buscarPorNombreFuzzy(filtro.terminoParcial());
            List<ProductoEntity> porAtributo = productoJpaRepository.buscarPorAtributoParcial(filtro.terminoParcial());

            return java.util.stream.Stream.concat(porNombre.stream(), porAtributo.stream())
                .distinct()
                .map(e -> toDTO(e, cotizacion))
                .toList();
        }

        if (filtro.nombre() != null && !filtro.nombre().isBlank()) {
            return productoJpaRepository.buscarPorNombreFuzzy(filtro.nombre())
                .stream().map(e -> toDTO(e, cotizacion)).toList();
        }

        return List.of();
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> todos() {
        BigDecimal cotizacion = getCotizacion();
        return productoJpaRepository.findAllByActivoTrueOrderByStockActualAsc()
            .stream().map(e -> toDTO(e, cotizacion)).toList();
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO> todos(int pagina, int tamano) {
        if (tamano > 200) tamano = 200;
        BigDecimal cotizacion = getCotizacion();
        Pageable pageable = PageRequest.of(pagina, tamano, Sort.by("stockActual").ascending());
        return productoJpaRepository.findAllByActivoTrue(pageable)
            .map(e -> toDTO(e, cotizacion));
    }

    private ProductoDTO toDTO(ProductoEntity e, BigDecimal cotizacion) {
        BigDecimal precioEnPesos = "USD".equals(e.getMoneda())
            ? e.getPrecioBase().multiply(cotizacion).setScale(2, RoundingMode.HALF_UP)
            : e.getPrecioBase();

        return new ProductoDTO(
            e.getId(),
            e.getSku(),
            e.getNombre(),
            e.getPrecioBase(),
            e.getMoneda(),
            precioEnPesos,
            e.getStockActual(),
            e.getStockMinimo(),
            e.getStockActual() <= e.getStockMinimo(),
            e.getUbicacionFisica(),
            e.isEsKit(),
            e.isActivo(),
            e.getPrecioCompra()
        );
    }

    private BigDecimal getCotizacion() {
        return configuracionRepository.getCotizacionDolar().orElse(BigDecimal.ONE);
    }
}
