package com.upgrade.app.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Collection;

@Service
public class SesionUsuarioService {
    public record SesionActiva(String id, String dispositivo, String ubicacion, LocalDateTime ultimoAcceso, boolean actual) {}
    private final SessionRegistry sessionRegistry;

    public SesionUsuarioService(SessionRegistry sessionRegistry) { this.sessionRegistry = sessionRegistry; }

    public List<SesionActiva> listar(String username, HttpServletRequest request) {
        HttpSession actual = request.getSession(false);
        String actualId = actual == null ? "" : actual.getId();
        List<SesionActiva> sesiones = new ArrayList<>();
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (!(principal instanceof UserDetails user) || !user.getUsername().equalsIgnoreCase(username)) continue;
            for (SessionInformation info : sessionRegistry.getAllSessions(principal, false)) {
                boolean esActual = info.getSessionId().equals(actualId);
                String dispositivo = esActual ? dispositivo(request.getHeader("User-Agent")) : "Otro navegador autenticado";
                String ubicacion = esActual ? direccion(request) : "Sesión registrada";
                LocalDateTime acceso = LocalDateTime.ofInstant(info.getLastRequest().toInstant(), ZoneId.systemDefault());
                sesiones.add(new SesionActiva(info.getSessionId(), dispositivo, ubicacion, acceso, esActual));
            }
        }
        sesiones.sort(Comparator.comparing(SesionActiva::actual).reversed()
                .thenComparing(Comparator.comparing(SesionActiva::ultimoAcceso).reversed()));
        return sesiones;
    }

    public boolean cerrar(String username, String sessionId, String actualId) {
        if (sessionId == null || sessionId.equals(actualId)) return false;
        SessionInformation info = sessionRegistry.getSessionInformation(sessionId);
        if (info == null || !(info.getPrincipal() instanceof UserDetails user) || !user.getUsername().equalsIgnoreCase(username)) return false;
        info.expireNow();
        return true;
    }

    public int cerrarOtras(String username, String actualId) {
        int cerradas = 0;
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (!(principal instanceof UserDetails user) || !user.getUsername().equalsIgnoreCase(username)) continue;
            for (SessionInformation info : sessionRegistry.getAllSessions(principal, false)) {
                if (!info.getSessionId().equals(actualId)) { info.expireNow(); cerradas++; }
            }
        }
        return cerradas;
    }

    public int expirarUsuarios(Collection<String> usernames) {
        if (usernames == null || usernames.isEmpty()) return 0;
        int expiradas = 0;
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (!(principal instanceof UserDetails user)
                    || usernames.stream().noneMatch(nombre -> nombre.equalsIgnoreCase(user.getUsername()))) {
                continue;
            }
            for (SessionInformation info : sessionRegistry.getAllSessions(principal, false)) {
                info.expireNow();
                expiradas++;
            }
        }
        return expiradas;
    }

    private String dispositivo(String agente) {
        if (agente == null) return "Navegador web";
        String navegador = agente.contains("Edg/") ? "Edge" : agente.contains("Chrome/") ? "Chrome" : agente.contains("Safari/") ? "Safari" : agente.contains("Firefox/") ? "Firefox" : "Navegador";
        String sistema = agente.contains("Windows") ? "Windows" : agente.contains("Macintosh") ? "macOS" : agente.contains("Android") ? "Android" : agente.contains("iPhone") ? "iPhone" : "Dispositivo";
        return sistema + " · " + navegador;
    }

    private String direccion(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        return ("0:0:0:0:0:0:0:1".equals(ip) || "127.0.0.1".equals(ip)) ? "Este equipo · Local" : "IP " + ip.split(",")[0].trim();
    }
}
