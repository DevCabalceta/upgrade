package com.upgrade.app.repository;

import com.upgrade.app.domain.DepartamentoColaborador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartamentoColaboradorRepository extends JpaRepository<DepartamentoColaborador, Long> {
    List<DepartamentoColaborador> findAllByActivoTrueOrderByNombreAsc();
}
