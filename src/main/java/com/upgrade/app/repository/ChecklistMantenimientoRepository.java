package com.upgrade.app.repository;

import com.upgrade.app.domain.ChecklistMantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ChecklistMantenimientoRepository extends JpaRepository<ChecklistMantenimiento, Long> {

    @Query("""
            SELECT c FROM ChecklistMantenimiento c
            WHERE c.orden.id IN :ordenes
            ORDER BY c.orden.id, c.posicion
            """)
    List<ChecklistMantenimiento> listarPorOrdenes(@Param("ordenes") Collection<Long> ordenes);

    @Query("SELECT c FROM ChecklistMantenimiento c WHERE c.id = :id AND c.orden.activo = true")
    Optional<ChecklistMantenimiento> buscarActivoPorId(@Param("id") Long id);

    long countByOrdenId(Long ordenId);
}
