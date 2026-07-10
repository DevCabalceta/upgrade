package com.upgrade.app.service;

import com.upgrade.app.domain.Usuario;
import com.upgrade.app.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.DisabledException;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        // Pasamos la variable 'login' a ambos parámetros (username y email)
        Usuario usuario = usuarioRepository.findByUsernameOrEmail(login, login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario o correo no encontrado"));

        if (!usuario.getActivo()) {
            throw new DisabledException("La cuenta está inactiva");
        }

        // =================================================================
        // MODO ESPÍA: Vamos a imprimir qué diablos está extrayendo Java
        // =================================================================
        System.out.println("🚨 --- DEBUG DE LOGIN --- 🚨");
        System.out.println("Login ingresado: " + login);
        System.out.println("Usuario encontrado en BD: " + usuario.getUsername());
        System.out.println("Contraseña extraída de BD: [" + usuario.getPassword() + "]");
        System.out.println("¿Coincide con BCrypt? " + usuario.getPassword().startsWith("$2a$"));
        System.out.println("🚨 ---------------------- 🚨");

        return org.springframework.security.core.userdetails.User
                .withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities("ROLE_USER") // Cambia esto según tus roles
                .build();
    }
}