package com.upgrade.app.repository;

import com.upgrade.app.domain.EstadoServicio;
import com.upgrade.app.domain.Servicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    @EntityGraph(attributePaths = "categoria")
    @Query("""
            SELECT s
            FROM Servicio s
            WHERE s.eliminado = false
              AND (
                  :buscar IS NULL
                  OR LOWER(s.nombre) LIKE LOWER(CONCAT('%', :buscar, '%'))
                  OR LOWER(s.descripcion) LIKE LOWER(CONCAT('%', :buscar, '%'))
                  OR LOWER(s.categoria.nombre) LIKE LOWER(CONCAT('%', :buscar, '%'))
              )
              AND (:estado IS NULL OR s.estado = :estado)
              AND (:categoriaId IS NULL OR s.categoria.id = :categoriaId)
            """)
    Page<Servicio> buscar(
            @Param("buscar") String buscar,
            @Param("estado") EstadoServicio estado,
            @Param("categoriaId") Long categoriaId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "categoria")
    List<Servicio> findAllByEliminadoFalseOrderByOrdenAscIdAsc();

    @EntityGraph(attributePaths = "categoria")
    List<Servicio> findAllByEliminadoFalseAndEstadoAndVisibleLandingTrueOrderByOrdenAscIdAsc(EstadoServicio estado);

    Optional<Servicio> findByIdAndEliminadoFalse(Long id);

    Optional<Servicio> findFirstByEliminadoFalseAndOrdenLessThanOrderByOrdenDesc(Integer orden);

    Optional<Servicio> findFirstByEliminadoFalseAndOrdenGreaterThanOrderByOrdenAsc(Integer orden);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    long countByEliminadoFalse();

    long countByEliminadoFalseAndEstado(EstadoServicio estado);

    long countByEliminadoFalseAndEstadoAndVisibleLandingTrue(EstadoServicio estado);

    @Query("SELECT COALESCE(SUM(s.precioBase), 0) FROM Servicio s WHERE s.eliminado = false AND s.estado = :estado")
    BigDecimal sumarPrecioBasePorEstado(@Param("estado") EstadoServicio estado);

    @Query("SELECT COALESCE(MAX(s.orden), 0) FROM Servicio s WHERE s.eliminado = false")
    int obtenerOrdenMaximo();
}
