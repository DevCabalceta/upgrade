package com.upgrade.app.repository;

import com.upgrade.app.domain.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    @Query("""
            SELECT c
            FROM Cliente c
            WHERE (
                :buscar IS NULL
                OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :buscar, '%'))
                OR LOWER(COALESCE(c.empresa, '')) LIKE LOWER(CONCAT('%', :buscar, '%'))
                OR LOWER(c.correo) LIKE LOWER(CONCAT('%', :buscar, '%'))
                OR LOWER(c.telefono) LIKE LOWER(CONCAT('%', :buscar, '%'))
                OR LOWER(c.identificacion) LIKE LOWER(CONCAT('%', :buscar, '%'))
            )
            AND (:activo IS NULL OR c.activo = :activo)
            """)
    Page<Cliente> buscar(
            @Param("buscar") String buscar,
            @Param("activo") Boolean activo,
            Pageable pageable
    );

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);

    boolean existsByIdentificacionIgnoreCase(String identificacion);

    boolean existsByIdentificacionIgnoreCaseAndIdNot(String identificacion, Long id);
}
