package com.upgrade.app.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class CustomUserDetails extends User {
    
    private String nombre;
    private String apellido;
    private String rolNombre;

    // Constructor
    public CustomUserDetails(String username, String password, boolean enabled,
                             Collection<? extends GrantedAuthority> authorities,
                             String nombre, String apellido, String rolNombre) {
        super(username, password, enabled, true, true, true, authorities);
        this.nombre = nombre;
        this.apellido = apellido;
        this.rolNombre = rolNombre;
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

    public String getRolNombre() {
        return rolNombre;
    }

    public void actualizarPerfil(String nombre, String apellido, String rolNombre) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.rolNombre = rolNombre;
    }

    public boolean tieneModulo(String modulo) {
        String autoridad = "MODULO_" + modulo;
        return getAuthorities().stream().anyMatch(item -> item.getAuthority().equals(autoridad));
    }

    public String getRutaInicial() {
        Map<String, String> rutas = new LinkedHashMap<>();
        rutas.put("DASHBOARD", "/admin/dashboard");
        rutas.put("CLIENTES", "/admin/clientes");
        rutas.put("INVENTARIO", "/admin/inventario");
        rutas.put("SERVICIOS", "/admin/servicios");
        rutas.put("COTIZACIONES", "/admin/cotizaciones");
        rutas.put("CALENDARIO", "/admin/calendario");
        rutas.put("PRESTAMOS", "/admin/prestamos");
        rutas.put("MANTENIMIENTO", "/admin/mantenimiento");
        rutas.put("COLABORADORES", "/admin/colaboradores");
        rutas.put("ROLES", "/admin/roles");
        rutas.put("GALERIA", "/admin/galeria");
        rutas.put("PREGUNTAS", "/admin/preguntas");
        rutas.put("CONFIGURACION", "/admin/configuracion");
        return rutas.entrySet().stream().filter(entry -> tieneModulo(entry.getKey()))
                .map(Map.Entry::getValue).findFirst().orElse("/admin/acceso-denegado");
    }
}
