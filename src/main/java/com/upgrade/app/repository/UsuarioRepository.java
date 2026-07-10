package com.upgrade.app.repository;

import com.upgrade.app.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Busca coincidencias tanto en username como en email
    Optional<Usuario> findByUsernameOrEmail(String username, String email);
    
}