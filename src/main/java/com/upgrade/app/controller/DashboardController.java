package com.upgrade.app.controller;

import com.upgrade.app.domain.CustomUserDetails;
import com.upgrade.app.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Controller
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private static final ZoneId COSTA_RICA = ZoneId.of("America/Costa_Rica");
    private final DashboardService dashboardService;

    @GetMapping
    public String dashboard(Authentication authentication, Model model) {
        int hora = ZonedDateTime.now(COSTA_RICA).getHour();
        String saludo = hora >= 5 && hora < 12 ? "Buenos días"
                : hora >= 12 && hora < 19 ? "Buenas tardes" : "Buenas noches";
        String nombre = "";
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails usuario) {
            String completo = usuario.getNombreCompleto();
            nombre = completo == null || completo.isBlank() ? usuario.getUsername() : completo.trim().split("\\s+")[0];
        }
        model.addAttribute("saludo", saludo);
        model.addAttribute("nombreUsuario", nombre);
        model.addAttribute("dashboard", dashboardService.obtenerResumen());
        model.addAttribute("anioActual", ZonedDateTime.now(COSTA_RICA).getYear());
        return "admin/dashboard";
    }
}
