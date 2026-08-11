package com.upgrade.app.repository;

import com.upgrade.app.domain.CategoriaInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaInventarioRepository extends JpaRepository<CategoriaInventario, Long> {

    List<CategoriaInventario> findAllByActivaTrueOrderByNombreAsc();
}
