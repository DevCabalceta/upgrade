package com.upgrade.app.repository;

import com.upgrade.app.domain.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RolRepository extends JpaRepository<Rol, Long> {
    @EntityGraph(attributePaths = "modulos")
    List<Rol> findAllByActivoTrueOrderByNombreAsc();

    @Query("select distinct r from Rol r left join fetch r.modulos where r.id = :id")
    Optional<Rol> findWithModulosById(@Param("id") Long id);

    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
    boolean existsByNombreMostradoIgnoreCase(String nombreMostrado);
    boolean existsByNombreMostradoIgnoreCaseAndIdNot(String nombreMostrado, Long id);
}
