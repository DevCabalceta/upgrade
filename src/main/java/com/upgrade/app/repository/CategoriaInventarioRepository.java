package com.upgrade.app.repository;

import com.upgrade.app.domain.CategoriaInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaInventarioRepository
        extends JpaRepository<CategoriaInventario, Long> {

}