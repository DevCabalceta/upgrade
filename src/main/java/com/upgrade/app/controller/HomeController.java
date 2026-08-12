package com.upgrade.app.controller;

import com.upgrade.app.service.PreguntaFrecuenteService;
import com.upgrade.app.service.GaleriaService;
import com.upgrade.app.service.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.web.csrf.CsrfToken;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final PreguntaFrecuenteService preguntaFrecuenteService;
    private final GaleriaService galeriaService;
    private final ServicioService servicioService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("preguntasFrecuentes", preguntaFrecuenteService.listarPublicadas());
        model.addAttribute("elementosGaleria", galeriaService.listarPublicados());
        model.addAttribute("serviciosPublicados", servicioService.listarPublicados());
        return "index";
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(name = "error", required = false) String error,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // Fuerza la creación y almacenamiento del token antes de que Thymeleaf
        // comience a enviar la plantilla. El login contiene bastante CSS inline
        // y no debe intentar crear la sesión cuando la respuesta ya fue enviada.
        Object csrfAttribute = request.getAttribute(CsrfToken.class.getName());
        if (csrfAttribute instanceof CsrfToken csrfToken) {
            csrfToken.getToken();
        }

        model.addAttribute("loginError", error != null);
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        return "login";
    }
}
