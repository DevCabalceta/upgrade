package com.upgrade.app.repository;

import com.upgrade.app.domain.EstadoInventario;
import com.upgrade.app.domain.Inventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    @EntityGraph(attributePaths = "categoria")
    List<Inventario> findAllByActivoTrueAndCantidadDisponibleGreaterThanOrderByNombreAsc(Integer cantidad);

    @EntityGraph(attributePaths = "categoria")
    List<Inventario> findAllByActivoTrueOrderByNombreAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventario i WHERE i.id = :id AND i.activo = true")
    Optional<Inventario> buscarActivoParaActualizar(@Param("id") Long id);

    @EntityGraph(attributePaths = "categoria")
    @Query("""
            SELECT i
            FROM Inventario i
            WHERE i.activo = true
              AND (
                  :buscar IS NULL
                  OR LOWER(i.codigo) LIKE LOWER(CONCAT('%', :buscar, '%'))
                  OR LOWER(i.nombre) LIKE LOWER(CONCAT('%', :buscar, '%'))
                  OR LOWER(COALESCE(i.marca, '')) LIKE LOWER(CONCAT('%', :buscar, '%'))
                  OR LOWER(COALESCE(i.modelo, '')) LIKE LOWER(CONCAT('%', :buscar, '%'))
                  OR LOWER(COALESCE(i.numeroSerie, '')) LIKE LOWER(CONCAT('%', :buscar, '%'))
                  OR LOWER(COALESCE(i.bodega, '')) LIKE LOWER(CONCAT('%', :buscar, '%'))
                  OR LOWER(COALESCE(i.ubicacion, '')) LIKE LOWER(CONCAT('%', :buscar, '%'))
              )
              AND (:estado IS NULL OR i.estado = :estado)
              AND (:categoriaId IS NULL OR i.categoria.id = :categoriaId)
            """)
    Page<Inventario> buscar(
            @Param("buscar") String buscar,
            @Param("estado") EstadoInventario estado,
            @Param("categoriaId") Long categoriaId,
            Pageable pageable
    );

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    boolean existsByNumeroSerieIgnoreCase(String numeroSerie);

    boolean existsByNumeroSerieIgnoreCaseAndIdNot(String numeroSerie, Long id);

    long countByActivoTrue();

    long countByActivoTrueAndEstado(EstadoInventario estado);

    long countByActivoTrueAndCategoriaId(Long categoriaId);

    @Query(value = "SELECT COALESCE(SUM(valor_unitario * cantidad_total), 0) FROM inventario WHERE activo = TRUE", nativeQuery = true)
    BigDecimal sumarValorInventarioActivo();
}
