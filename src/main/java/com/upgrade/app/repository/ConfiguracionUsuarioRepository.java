package com.upgrade.app.repository;

import com.upgrade.app.domain.ConfiguracionUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ConfiguracionUsuarioRepository extends JpaRepository<ConfiguracionUsuario, Long> {
    Optional<ConfiguracionUsuario> findByUsuarioId(Long usuarioId);
}
