package com.upgrade.app.repository;

import com.upgrade.app.domain.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Set;

public interface ModuloRepository extends JpaRepository<Modulo, Long> {
    List<Modulo> findAllByActivoTrueOrderByOrdenAsc();
    List<Modulo> findAllByIdInAndActivoTrue(Set<Long> ids);
    long countByActivoTrue();
}
