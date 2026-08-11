package com.upgrade.app.repository;

import com.upgrade.app.domain.Servicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    @Query("""
            SELECT s
            FROM Servicio s
            WHERE (
                :buscar IS NULL
                OR LOWER(s.nombre) LIKE LOWER(CONCAT('%', :buscar, '%'))
                OR LOWER(COALESCE(s.descripcion, '')) LIKE LOWER(CONCAT('%', :buscar, '%'))
            )
            AND (:activo IS NULL OR s.activo = :activo)
            """)
    Page<Servicio> buscar(
            @Param("buscar") String buscar,
            @Param("activo") Boolean activo,
            Pageable pageable
    );
}
