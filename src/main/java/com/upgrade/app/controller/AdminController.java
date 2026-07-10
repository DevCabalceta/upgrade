package com.upgrade.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin") // Todas las rutas de este controlador empezarán con /admin
public class AdminController {
    
    @GetMapping({"", "/"})
    public String adminRoot() {
        // Redirige automáticamente al navegador de /admin a /admin/dashboard
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        // Retorna el archivo ubicado en src/main/resources/templates/admin/dashboard.html
        return "admin/dashboard"; 
    }

    @GetMapping("/clientes")
    public String clientes() {
        return "admin/clientes";
    }

    @GetMapping("/inventario")
    public String inventario() {
        return "admin/inventario";
    }

    @GetMapping("/servicios")
    public String servicios() {
        return "admin/servicios";
    }

    @GetMapping("/cotizaciones")
    public String cotizaciones() {
        return "admin/cotizaciones";
    }

    @GetMapping("/calendario")
    public String calendario() {
        return "admin/calendario";
    }

    @GetMapping("/prestamos")
    public String prestamos() {
        return "admin/prestamos";
    }

    @GetMapping("/mantenimiento")
    public String mantenimiento() {
        return "admin/mantenimiento";
    }

    @GetMapping("/colaboradores")
    public String colaboradores() {
        return "admin/colaboradores";
    }

    @GetMapping("/roles")
    public String roles() {
        return "admin/roles";
    }

    @GetMapping("/galeria")
    public String galeria() {
        return "admin/galeria";
    }

    @GetMapping("/configuracion")
    public String configuracion() {
        return "admin/configuracion";
    }
}