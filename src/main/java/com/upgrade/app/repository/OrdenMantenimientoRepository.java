package com.upgrade.app.repository;

import com.upgrade.app.domain.EstadoMantenimiento;
import com.upgrade.app.domain.OrdenMantenimiento;
import com.upgrade.app.domain.PrioridadMantenimiento;
import com.upgrade.app.domain.TipoMantenimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface OrdenMantenimientoRepository extends JpaRepository<OrdenMantenimiento, Long> {

    @EntityGraph(attributePaths = {"inventario", "inventario.categoria", "cliente", "tecnico", "creadoPor"})
    @Query("""
            SELECT o
            FROM OrdenMantenimiento o
            LEFT JOIN o.cliente c
            LEFT JOIN o.tecnico t
            WHERE o.activo = true
              AND (
                  :buscar IS NULL
                  OR LOWER(o.numero) LIKE LOWER(CONCAT('%', :buscar, '%'))
                  OR LOWER(o.inventario.codigo) LIKE LOWER(CONCAT('%', :buscar, '%'))
                  OR LOWER(o.inventario.nombre) LIKE LOWER(CONCAT('%', :buscar, '%'))
                  OR LOWER(COALESCE(c.nombre, '')) LIKE LOWER(CONCAT('%', :buscar, '%'))
                  OR LOWER(COALESCE(c.empresa, '')) LIKE LOWER(CONCAT('%', :buscar, '%'))
                  OR LOWER(COALESCE(o.ubicacion, '')) LIKE LOWER(CONCAT('%', :buscar, '%'))
                  OR LOWER(COALESCE(t.nombre, '')) LIKE LOWER(CONCAT('%', :buscar, '%'))
                  OR LOWER(COALESCE(t.apellido, '')) LIKE LOWER(CONCAT('%', :buscar, '%'))
              )
              AND (:tipo IS NULL OR o.tipo = :tipo)
              AND (:estado IS NULL OR o.estado = :estado)
              AND (:prioridad IS NULL OR o.prioridad = :prioridad)
            """)
    Page<OrdenMantenimiento> buscar(
            @Param("buscar") String buscar,
            @Param("tipo") TipoMantenimiento tipo,
            @Param("estado") EstadoMantenimiento estado,
            @Param("prioridad") PrioridadMantenimiento prioridad,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"inventario", "inventario.categoria", "cliente", "tecnico", "creadoPor"})
    @Query("SELECT o FROM OrdenMantenimiento o WHERE o.id = :id AND o.activo = true")
    Optional<OrdenMantenimiento> buscarDetallePorId(@Param("id") Long id);

    boolean existsByInventarioIdAndEstadoAndActivoTrueAndIdNot(
            Long inventarioId,
            EstadoMantenimiento estado,
            Long id
    );

    long countByActivoTrueAndEstado(EstadoMantenimiento estado);

    long countByActivoTrueAndEstadoAndFechaProgramadaBetween(
            EstadoMantenimiento estado,
            LocalDate inicio,
            LocalDate fin
    );

    @Query("""
            SELECT COUNT(o)
            FROM OrdenMantenimiento o
            WHERE o.activo = true
              AND o.estado IN :estados
              AND o.fechaProgramada < :hoy
            """)
    long contarVencidas(
            @Param("estados") Collection<EstadoMantenimiento> estados,
            @Param("hoy") LocalDate hoy
    );

    @Query("""
            SELECT COALESCE(SUM(o.costoEstimado), 0)
            FROM OrdenMantenimiento o
            WHERE o.activo = true
              AND o.estado <> com.upgrade.app.domain.EstadoMantenimiento.CANCELADO
              AND o.fechaProgramada BETWEEN :inicio AND :fin
            """)
    BigDecimal sumarCostoEntre(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
}
