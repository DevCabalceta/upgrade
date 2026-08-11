package com.upgrade.app.service;

import com.upgrade.app.domain.Usuario;
import com.upgrade.app.repository.UsuarioRepository;
import com.upgrade.app.domain.CustomUserDetails; 
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.DisabledException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsernameOrEmail(login, login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario o correo no encontrado"));

        if (!usuario.getActivo()) {
            throw new DisabledException("La cuenta está inactiva");
        }

        List<SimpleGrantedAuthority> authorities = usuario.getRoles().isEmpty()
                ? List.of(new SimpleGrantedAuthority("ROLE_USER"))
                : usuario.getRoles().stream()
                .filter(rol -> Boolean.TRUE.equals(rol.getActivo()))
                .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombre()))
                .toList();

        return new CustomUserDetails(
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.getActivo(),
                authorities,
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getRolPrincipalNombre()
        );
    }
}
