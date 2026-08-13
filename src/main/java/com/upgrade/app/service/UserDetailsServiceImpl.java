package com.upgrade.app.service;

import com.upgrade.app.domain.Usuario;
import com.upgrade.app.repository.UsuarioRepository;
import com.upgrade.app.domain.CustomUserDetails; 
import com.upgrade.app.domain.Modulo;
import com.upgrade.app.repository.ModuloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.DisabledException;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final ModuloRepository moduloRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsernameOrEmail(login, login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario o correo no encontrado"));

        if (!usuario.getActivo()) {
            throw new DisabledException("La cuenta está inactiva");
        }

        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        usuario.getRoles().stream()
                .filter(rol -> Boolean.TRUE.equals(rol.getActivo()))
                .forEach(rol -> authorities.add(new SimpleGrantedAuthority("ROLE_" + rol.getNombre())));

        boolean administrador = usuario.getRoles().stream()
                .anyMatch(rol -> Boolean.TRUE.equals(rol.getActivo()) && "ADMIN".equalsIgnoreCase(rol.getNombre()));
        var modulos = administrador
                ? moduloRepository.findAllByActivoTrueOrderByOrdenAsc()
                : usuario.getRoles().stream()
                    .filter(rol -> Boolean.TRUE.equals(rol.getActivo()))
                    .flatMap(rol -> rol.getModulos().stream())
                    .filter(modulo -> Boolean.TRUE.equals(modulo.getActivo()))
                    .distinct()
                    .toList();
        modulos.stream().map(Modulo::getAutoridad)
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);
        if (authorities.isEmpty()) authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

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
