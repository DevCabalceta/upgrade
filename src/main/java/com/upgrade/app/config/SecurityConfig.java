package com.upgrade.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SessionRegistry sessionRegistry,
                                                   CustomAuthenticationSuccessHandler successHandler) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas: página de inicio, login y todos los recursos estáticos
                .requestMatchers("/", "/login", "/assets/**", "/dist/**", "/uploads/gallery/**").permitAll()
                // Las evidencias técnicas no deben ser públicas.
                .requestMatchers("/uploads/maintenance/**").hasAuthority("MODULO_MANTENIMIENTO")
                .requestMatchers("/uploads/profile/**", "/admin/acceso-denegado").authenticated()
                .requestMatchers("/admin/dashboard/**").hasAuthority("MODULO_DASHBOARD")
                .requestMatchers("/admin/clientes/**").hasAuthority("MODULO_CLIENTES")
                .requestMatchers("/admin/inventario/**").hasAuthority("MODULO_INVENTARIO")
                .requestMatchers("/admin/servicios/**").hasAuthority("MODULO_SERVICIOS")
                .requestMatchers("/admin/cotizaciones/**").hasAuthority("MODULO_COTIZACIONES")
                .requestMatchers("/admin/calendario/**").hasAuthority("MODULO_CALENDARIO")
                .requestMatchers("/admin/prestamos/**").hasAuthority("MODULO_PRESTAMOS")
                .requestMatchers("/admin/mantenimiento/**").hasAuthority("MODULO_MANTENIMIENTO")
                .requestMatchers("/admin/colaboradores/**").hasAuthority("MODULO_COLABORADORES")
                .requestMatchers("/admin/roles/**").hasAuthority("MODULO_ROLES")
                .requestMatchers("/admin/galeria/**").hasAuthority("MODULO_GALERIA")
                .requestMatchers("/admin/preguntas/**").hasAuthority("MODULO_PREGUNTAS")
                .requestMatchers("/admin/configuracion/**").hasAuthority("MODULO_CONFIGURACION")
                .requestMatchers("/admin/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(successHandler)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                .maximumSessions(-1)
                .expiredUrl("/login?expired")
                .sessionRegistry(sessionRegistry)
            )
            .exceptionHandling(ex -> ex.accessDeniedPage("/admin/acceso-denegado"));

        return http.build();
    }

    // Bean para encriptar/desencriptar contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SessionRegistry sessionRegistry() { return new SessionRegistryImpl(); }

    @Bean
    public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
    }
}
