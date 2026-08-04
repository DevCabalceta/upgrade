package com.upgrade.app.controller;

import com.upgrade.app.service.PreguntaFrecuenteService;
import com.upgrade.app.service.GaleriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final PreguntaFrecuenteService preguntaFrecuenteService;
    private final GaleriaService galeriaService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("preguntasFrecuentes", preguntaFrecuenteService.listarPublicadas());
        model.addAttribute("elementosGaleria", galeriaService.listarPublicados());
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
