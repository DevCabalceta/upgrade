package com.upgrade.app.controller;

import com.upgrade.app.domain.Inventario;
import com.upgrade.app.service.CategoriaInventarioService;
import com.upgrade.app.service.InventarioService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/inventario")
public class InventarioController {

    private final InventarioService inventarioService;
    private final CategoriaInventarioService categoriaService;

    public InventarioController(
            InventarioService inventarioService,
            CategoriaInventarioService categoriaService) {

        this.inventarioService = inventarioService;
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String mostrarInventario(Model model) {

        model.addAttribute(
                "inventario",
                new Inventario()
        );

        model.addAttribute(
                "categorias",
                categoriaService.listar()
        );

        model.addAttribute(
                "inventarios",
                inventarioService.listar()
        );

        return "admin/inventario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {

        model.addAttribute(
                "inventario",
                inventarioService.buscarPorId(id)
        );

        model.addAttribute(
                "categorias",
                categoriaService.listar()
        );

        model.addAttribute(
                "inventarios",
                inventarioService.listar()
        );

        return "admin/inventario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Inventario inventario) {

        inventarioService.guardar(inventario);

        return "redirect:/admin/inventario";
    }
}
