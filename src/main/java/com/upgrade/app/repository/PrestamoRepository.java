package com.upgrade.app.repository;

import com.upgrade.app.domain.EstadoPrestamo;
import com.upgrade.app.domain.Prestamo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    @EntityGraph(attributePaths = {"cliente", "inventario", "responsable"})
    @Query("""
            SELECT p
            FROM Prestamo p
            WHERE (
                :buscar IS NULL
                OR LOWER(p.folio) LIKE LOWER(CONCAT('%', :buscar, '%'))
                OR LOWER(p.cliente.nombre) LIKE LOWER(CONCAT('%', :buscar, '%'))
                OR LOWER(COALESCE(p.cliente.empresa, '')) LIKE LOWER(CONCAT('%', :buscar, '%'))
                OR LOWER(p.correoContacto) LIKE LOWER(CONCAT('%', :buscar, '%'))
                OR LOWER(p.inventario.codigo) LIKE LOWER(CONCAT('%', :buscar, '%'))
                OR LOWER(p.inventario.nombre) LIKE LOWER(CONCAT('%', :buscar, '%'))
            )
            AND (
                :estadoFiltro IS NULL
                OR (:estadoFiltro = 'ACTIVO' AND p.estado = com.upgrade.app.domain.EstadoPrestamo.ACTIVO AND p.fechaDevolucionEstimada >= CURRENT_DATE)
                OR (:estadoFiltro = 'VENCIDO' AND p.estado = com.upgrade.app.domain.EstadoPrestamo.ACTIVO AND p.fechaDevolucionEstimada < CURRENT_DATE)
                OR (:estadoFiltro = 'DEVUELTO' AND p.estado = com.upgrade.app.domain.EstadoPrestamo.DEVUELTO)
            )
            AND (
                :fechaFiltro IS NULL
                OR (:fechaFiltro = 'HOY' AND p.estado = com.upgrade.app.domain.EstadoPrestamo.ACTIVO AND p.fechaDevolucionEstimada = CURRENT_DATE)
                OR (:fechaFiltro = 'PENDIENTES' AND p.estado = com.upgrade.app.domain.EstadoPrestamo.ACTIVO)
                OR (:fechaFiltro = 'DEVUELTOS' AND p.estado = com.upgrade.app.domain.EstadoPrestamo.DEVUELTO)
            )
            """)
    Page<Prestamo> buscar(
            @Param("buscar") String buscar,
            @Param("estadoFiltro") String estadoFiltro,
            @Param("fechaFiltro") String fechaFiltro,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"cliente", "inventario", "responsable"})
    @Query("SELECT p FROM Prestamo p WHERE p.id = :id")
    Optional<Prestamo> buscarDetallePorId(@Param("id") Long id);

    @EntityGraph(attributePaths = {"cliente", "inventario", "responsable"})
    @Query("""
            SELECT p FROM Prestamo p
            WHERE p.estado = com.upgrade.app.domain.EstadoPrestamo.ACTIVO
              AND p.fechaDevolucionEstimada < CURRENT_DATE
            ORDER BY p.fechaDevolucionEstimada ASC
            """)
    List<Prestamo> listarVencidos();

    @Query("SELECT COUNT(p) FROM Prestamo p WHERE p.estado = :estado AND p.fechaDevolucionEstimada >= :hoy")
    long contarActivos(@Param("estado") EstadoPrestamo estado, @Param("hoy") LocalDate hoy);

    @Query("SELECT COUNT(p) FROM Prestamo p WHERE p.estado = :estado AND p.fechaDevolucionEstimada < :hoy")
    long contarVencidos(@Param("estado") EstadoPrestamo estado, @Param("hoy") LocalDate hoy);

    @Query("SELECT COUNT(p) FROM Prestamo p WHERE p.estado = :estado AND p.fechaDevolucionEstimada = :hoy")
    long contarPorDevolverHoy(@Param("estado") EstadoPrestamo estado, @Param("hoy") LocalDate hoy);

    long countByEstado(EstadoPrestamo estado);

    @EntityGraph(attributePaths = {"cliente", "inventario", "responsable"})
    List<Prestamo> findTop5ByOrderByFechaActualizacionDesc();

    @EntityGraph(attributePaths = {"cliente", "inventario", "responsable"})
    @Query("""
            SELECT p FROM Prestamo p
            WHERE p.estado = com.upgrade.app.domain.EstadoPrestamo.ACTIVO
              AND p.fechaDevolucionEstimada >= :hoy
            ORDER BY p.fechaDevolucionEstimada ASC, p.id ASC
            """)
    List<Prestamo> listarProximasDevoluciones(@Param("hoy") LocalDate hoy, Pageable pageable);
}
