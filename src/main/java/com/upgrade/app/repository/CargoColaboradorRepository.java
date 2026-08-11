package com.upgrade.app.repository;

import com.upgrade.app.domain.CargoColaborador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CargoColaboradorRepository extends JpaRepository<CargoColaborador, Long> {
    List<CargoColaborador> findAllByActivoTrueOrderByNombreAsc();
}
