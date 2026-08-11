package com.upgrade.app.repository;

import com.upgrade.app.domain.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolRepository extends JpaRepository<Rol, Long> {
    List<Rol> findAllByActivoTrueOrderByNombreAsc();
}
