package com.mmotos.infrastructure.output.persistence.jpa;

import com.mmotos.infrastructure.output.persistence.entity.ProductoEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductoJpaRepository extends JpaRepository<ProductoEntity, UUID> {

    Optional<ProductoEntity> findBySkuAndActivoTrue(String sku);

    // Pessimistic lock para evitar colisiones al vender en modo multi-puesto
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductoEntity p WHERE p.id = :id AND p.activo = true")
    Optional<ProductoEntity> findByIdWithLock(@Param("id") UUID id);

    // Busca por atributos JSONB exactos usando el operador @> de PostgreSQL (usa índice GIN)
    @Query(value = "SELECT * FROM productos WHERE atributos_extra @> :jsonConsulta::jsonb AND activo = true",
           nativeQuery = true)
    List<ProductoEntity> buscarPorAtributos(@Param("jsonConsulta") String jsonConsulta);

    // Búsqueda por nombre: ILIKE para coincidencia parcial + fuzzy fallback con pg_trgm
    @Query(value = "SELECT * FROM productos WHERE (nombre ILIKE '%' || :termino || '%' OR nombre % :termino) AND activo = true ORDER BY similarity(nombre, :termino) DESC",
           nativeQuery = true)
    List<ProductoEntity> buscarPorNombreFuzzy(@Param("termino") String termino);

    // Productos bajo stock mínimo — usa el índice parcial idx_productos_stock_bajo
    @Query(value = "SELECT * FROM productos WHERE stock_actual <= stock_minimo AND activo = true",
           nativeQuery = true)
    List<ProductoEntity> findBajoMinimo();

    // Búsqueda parcial en atributos JSONB usando pg_trgm (ej: "110" encuentra "110/70-17")
    @Query(value = "SELECT * FROM productos WHERE atributos_extra::text ILIKE '%' || :termino || '%' AND activo = true",
           nativeQuery = true)
    List<ProductoEntity> buscarPorAtributoParcial(@Param("termino") String termino);

    // Todos los productos activos ordenados por stock (para Listas y Precios)
    List<ProductoEntity> findAllByActivoTrueOrderByStockActualAsc();

    // Paginado — para GET /api/productos/todos
    Page<ProductoEntity> findAllByActivoTrue(Pageable pageable);
}
