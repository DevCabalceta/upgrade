package com.upgrade.app.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.Collection;

public class CustomUserDetails extends User {
    
    private String nombre;
    private String apellido;

    // Constructor
    public CustomUserDetails(String username, String password, boolean enabled,
                             Collection<? extends GrantedAuthority> authorities,
                             String nombre, String apellido) {
        super(username, password, enabled, true, true, true, authorities);
        this.nombre = nombre;
        this.apellido = apellido;
    }

    // Método para obtener el nombre completo
    public String getNombreCompleto() {
        return this.nombre + " " + this.apellido;
    }

    // Método para generar las iniciales automáticamente
    public String getIniciales() {
        String inicialNombre = (nombre != null && !nombre.isEmpty()) ? nombre.substring(0, 1) : "";
        String inicialApellido = (apellido != null && !apellido.isEmpty()) ? apellido.substring(0, 1) : "";
        return (inicialNombre + inicialApellido).toUpperCase();
    }
}