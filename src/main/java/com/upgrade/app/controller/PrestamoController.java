package com.upgrade.app.controller;

import com.upgrade.app.domain.Prestamo;
import com.upgrade.app.service.InventarioService;
import com.upgrade.app.service.PrestamoService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Prestamo prestamo,
            RedirectAttributes redirectAttributes) {

        try {

            prestamoService.guardar(prestamo);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Préstamo registrado correctamente."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/admin/prestamos";
    }

    @GetMapping("/devolver/{id}")
    public String devolver(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        prestamoService.devolver(id);

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Préstamo devuelto correctamente."
        );

        return "redirect:/admin/prestamos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        prestamoService.eliminar(id);

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Préstamo eliminado correctamente."
        );

        return "redirect:/admin/prestamos";
    }
}