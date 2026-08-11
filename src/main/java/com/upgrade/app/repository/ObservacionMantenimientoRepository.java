package com.upgrade.app.repository;

import com.upgrade.app.domain.ObservacionMantenimiento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ObservacionMantenimientoRepository extends JpaRepository<ObservacionMantenimiento, Long> {

    @EntityGraph(attributePaths = "usuario")
    @Query("""
            SELECT o FROM ObservacionMantenimiento o
            WHERE o.orden.id IN :ordenes
            ORDER BY o.fechaCreacion DESC
            """)
    List<ObservacionMantenimiento> listarPorOrdenes(@Param("ordenes") Collection<Long> ordenes);
}
