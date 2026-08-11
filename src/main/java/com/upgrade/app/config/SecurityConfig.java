package com.upgrade.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas: página de inicio, login y todos los recursos estáticos
                .requestMatchers("/", "/login", "/assets/**", "/dist/**").permitAll()
                // Cualquier ruta que empiece con /admin requiere haber iniciado sesión
                .requestMatchers("/admin/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/login") // Indica dónde está tu HTML de login
                .defaultSuccessUrl("/admin/dashboard", true) // A dónde ir al entrar con éxito
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout") // A dónde ir al cerrar sesión
                .permitAll()
            );

        return http.build();
    }

    // Bean para encriptar/desencriptar contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}