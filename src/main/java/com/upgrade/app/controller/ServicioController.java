package com.upgrade.app.controller;

import com.upgrade.app.domain.Servicio;
import com.upgrade.app.dto.ServicioForm;
import com.upgrade.app.service.ServicioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        agregarFormulariosSiFaltan(model);
        return "admin/servicios";
    }

    @PostMapping
    public String crear(
            @Valid @ModelAttribute("nuevoServicio") ServicioForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        validarUnicos(form, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("editarServicio", new ServicioForm());
            model.addAttribute("modalAbierto", "nuevo");
            model.addAttribute("servicios", servicioService.listar(0, TAMANO_PAGINA));
            return "admin/servicios";
        }

        Servicio servicio = servicioService.crear(form);
        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "El servicio “" + servicio.getNombre() + "” fue creado correctamente."
        );
        return "redirect:/admin/servicios";
    }

    @PostMapping("/editar")
    public String editar(
            @Valid @ModelAttribute("editarServicio") ServicioForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (form.getId() == null) {
            bindingResult.reject("servicio.id.requerido", "No fue posible identificar el servicio.");
        } else {
            validarUnicos(form, bindingResult);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("nuevoServicio", new ServicioForm());
            model.addAttribute("modalAbierto", "editar");
            model.addAttribute("servicios", servicioService.listar(0, TAMANO_PAGINA));
            return "admin/servicios";
        }

        Servicio servicio = servicioService.actualizar(form);
        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "Los datos de “" + servicio.getNombre() + "” fueron actualizados."
        );
        return "redirect:/admin/servicios";
    }

    private void agregarFormulariosSiFaltan(Model model) {
        if (!model.containsAttribute("nuevoServicio")) {
            model.addAttribute("nuevoServicio", new ServicioForm());
        }
        if (!model.containsAttribute("editarServicio")) {
            model.addAttribute("editarServicio", new ServicioForm());
        }
    }

    private void validarUnicos(ServicioForm form, BindingResult bindingResult) {
        if (form.getNombre() != null
                && !bindingResult.hasFieldErrors("nombre")
                && servicioService.nombreOcupado(form.getNombre(), form.getId())) {
            bindingResult.rejectValue("nombre", "servicio.nombre.duplicado", "Ya existe un servicio con este nombre.");
        }
    }
}