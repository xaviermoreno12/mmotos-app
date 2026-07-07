package com.mmotos.infrastructure.output.persistence.adapter;

import com.mmotos.domain.model.Repuesto;
import com.mmotos.domain.model.RepuestoSimple;
import com.mmotos.domain.port.ProductoRepository;
import com.mmotos.infrastructure.output.persistence.jpa.ProductoJpaRepository;
import com.mmotos.infrastructure.output.persistence.mapper.ProductoMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ProductoRepositoryAdapter implements ProductoRepository {

    private final ProductoJpaRepository jpaRepository;
    private final ProductoMapper mapper;

    public ProductoRepositoryAdapter(ProductoJpaRepository jpaRepository, ProductoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Repuesto> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Optional<Repuesto> findByIdWithLock(UUID id) {
        return jpaRepository.findByIdWithLock(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Repuesto> findBySku(String sku) {
        return jpaRepository.findBySkuAndActivoTrue(sku).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Repuesto> findBajoMinimo() {
        return jpaRepository.findBajoMinimo().stream()
            .map(e -> (Repuesto) mapper.toDomain(e))
            .toList();
    }

    @Override
    @Transactional
    public Repuesto save(Repuesto repuesto) {
        if (repuesto instanceof RepuestoSimple simple) {
            var entity = jpaRepository.findById(repuesto.getId())
                .orElse(mapper.toEntity(simple));
            mapper.syncStock(entity, repuesto.getStockActual());
            return mapper.toDomain(jpaRepository.save(entity));
        }
        throw new UnsupportedOperationException("Solo RepuestoSimple es persistible directamente. Los kits se gestionan por sus componentes.");
    }
}
