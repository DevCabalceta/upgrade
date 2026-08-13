package com.upgrade.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;
import com.upgrade.app.domain.CustomUserDetails;

@Controller
@RequestMapping("/admin") // Todas las rutas de este controlador empezarán con /admin
public class AdminController {
    
    @GetMapping({"", "/"})
    public String adminRoot(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails usuario) {
            return "redirect:" + usuario.getRutaInicial();
        }
        return "redirect:/login";
    }

    @GetMapping("/cotizaciones")
    public String cotizaciones() {
        return "admin/cotizaciones";
    }

    @GetMapping("/calendario")
    public String calendario() {
        return "admin/calendario";
    }

    @GetMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "admin/acceso-denegado";
    }

}
