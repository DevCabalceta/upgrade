package com.upgrade.app.service;

import com.upgrade.app.domain.Usuario;
import com.upgrade.app.repository.UsuarioRepository;
import com.upgrade.app.domain.CustomUserDetails; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.DisabledException;
import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsernameOrEmail(login, login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario o correo no encontrado"));

        if (!usuario.getActivo()) {
            throw new DisabledException("La cuenta está inactiva");
        }

        // Devolvemos nuestra clase personalizada con el nombre y apellido
        return new CustomUserDetails(
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.getActivo(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                usuario.getNombre(),
                usuario.getApellido()
        );
    }
}