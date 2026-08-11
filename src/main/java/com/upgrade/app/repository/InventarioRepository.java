package com.upgrade.app.repository;

import com.upgrade.app.domain.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {
}
