package com.upgrade.app.repository;

import com.upgrade.app.domain.MovimientoInventario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    @EntityGraph(attributePaths = "usuario")
    List<MovimientoInventario> findTop20ByInventarioIdOrderByFechaMovimientoDesc(Long inventarioId);

    @EntityGraph(attributePaths = {"usuario", "inventario"})
    List<MovimientoInventario> findTop5ByOrderByFechaMovimientoDesc();
}
