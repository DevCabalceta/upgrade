package com.upgrade.app.repository;

import com.upgrade.app.domain.EvidenciaMantenimiento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface EvidenciaMantenimientoRepository extends JpaRepository<EvidenciaMantenimiento, Long> {

    @EntityGraph(attributePaths = "usuario")
    @Query("""
            SELECT e FROM EvidenciaMantenimiento e
            WHERE e.orden.id IN :ordenes
            ORDER BY e.fechaCreacion DESC
            """)
    List<EvidenciaMantenimiento> listarPorOrdenes(@Param("ordenes") Collection<Long> ordenes);
}
