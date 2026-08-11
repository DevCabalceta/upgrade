package com.upgrade.app.controller;

import com.upgrade.app.domain.Servicio;
import com.upgrade.app.service.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private static final int TAMANO_PAGINA = 10;

    private final ServicioService servicioService;

    @GetMapping
    public String listar(
            @RequestParam(required = false, defaultValue = "0") int page,
            Model model
    ) {
        Page<Servicio> servicios = servicioService.listar(page, TAMANO_PAGINA);
        model.addAttribute("servicios", servicios);
        return "admin/servicios";
    }
}