package com.upgrade.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        // Esto le dice a Spring que busque el archivo index.html en la carpeta templates
        return "index"; 
    }
}
