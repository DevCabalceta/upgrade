package com.upgrade.app.repository;

import com.upgrade.app.domain.CategoriaServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaServicioRepository extends JpaRepository<CategoriaServicio, Long> {

    List<CategoriaServicio> findAllByActivaTrueOrderByNombreAsc();
}
