package com.upgrade.app.controller;

import com.upgrade.app.service.ConfiguracionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class ConfiguracionGlobalAdvice {
    private final ConfiguracionService configuracionService;

    @ModelAttribute("cuentaActual")
    public ConfiguracionService.Cuenta cuentaActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return null;
        try { return configuracionService.obtenerCuenta(auth.getName()); }
        catch (RuntimeException ignored) { return null; }
    }
}
