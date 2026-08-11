package com.upgrade.app.controller;

import com.upgrade.app.domain.Prestamo;
import com.upgrade.app.service.InventarioService;
import com.upgrade.app.service.PrestamoService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;
    private final InventarioService inventarioService;

    public PrestamoController(
            PrestamoService prestamoService,
            InventarioService inventarioService) {

        this.prestamoService = prestamoService;
        this.inventarioService = inventarioService;
    }

    @GetMapping
    public String mostrarPrestamos(Model model) {

        model.addAttribute(
                "prestamo",
                new Prestamo()
        );

        model.addAttribute(
                "inventarios",
                inventarioService.listar()
        );

        model.addAttribute(
                "prestamos",
                prestamoService.listar()
        );

        return "admin/prestamos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {

        model.addAttribute(
                "prestamo",
                prestamoService.buscarPorId(id)
        );

        model.addAttribute(
                "inventarios",
                inventarioService.listar()
        );

        model.addAttribute(
                "prestamos",
                prestamoService.listar()
        );

        return "admin/prestamos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {

        prestamoService.eliminar(id);

        return "redirect:/admin/prestamos";
    }

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Prestamo prestamo) {

        prestamoService.guardar(prestamo);

        return "redirect:/admin/prestamos";
    }
}
