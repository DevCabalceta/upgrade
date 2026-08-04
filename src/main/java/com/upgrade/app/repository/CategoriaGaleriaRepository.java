package com.upgrade.app.repository;

import com.upgrade.app.domain.CategoriaGaleria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaGaleriaRepository extends JpaRepository<CategoriaGaleria, Long> {
    List<CategoriaGaleria> findAllByActivaTrueOrderByNombreAsc();
    boolean existsByNombreIgnoreCase(String nombre);
}
