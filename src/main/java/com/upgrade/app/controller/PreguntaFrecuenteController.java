package com.upgrade.app.controller;

import com.upgrade.app.domain.CategoriaPregunta;
import com.upgrade.app.domain.PreguntaFrecuente;
import com.upgrade.app.dto.PreguntaFrecuenteForm;
import com.upgrade.app.service.PreguntaFrecuenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/admin/preguntas")
@RequiredArgsConstructor
public class PreguntaFrecuenteController {

    private final PreguntaFrecuenteService preguntaService;

    @ModelAttribute("categoriasPregunta")
    public CategoriaPregunta[] categoriasPregunta() {
        return CategoriaPregunta.values();
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false, defaultValue = "todas") String estado,
            @RequestParam(required = false) String categoria,
            Model model
    ) {
        cargarListado(model, buscar, estado, categoria);
        agregarFormulariosSiFaltan(model);
        return "admin/preguntas";
    }

    @PostMapping
    public String crear(
            @Valid @ModelAttribute("nuevaPregunta") PreguntaFrecuenteForm form,
            BindingResult bindingResult,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false, defaultValue = "todas") String estado,
            @RequestParam(name = "categoriaFiltro", required = false) String categoriaFiltro,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        validarPreguntaUnica(form, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("editarPregunta", new PreguntaFrecuenteForm());
            model.addAttribute("modalAbierto", "nueva");
            cargarListado(model, buscar, estado, categoriaFiltro);
            return "admin/preguntas";
        }

        PreguntaFrecuente creada = preguntaService.crear(form);
        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "La pregunta “" + creada.getPregunta() + "” fue creada correctamente."
        );
        return "redirect:/admin/preguntas";
    }

    @PostMapping("/editar")
    public String editar(
            @Valid @ModelAttribute("editarPregunta") PreguntaFrecuenteForm form,
            BindingResult bindingResult,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false, defaultValue = "todas") String estado,
            @RequestParam(name = "categoriaFiltro", required = false) String categoriaFiltro,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (form.getId() == null) {
            bindingResult.reject("pregunta.id.requerido", "No fue posible identificar la pregunta.");
        } else {
            validarPreguntaUnica(form, bindingResult);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("nuevaPregunta", new PreguntaFrecuenteForm());
            model.addAttribute("modalAbierto", "editar");
            cargarListado(model, buscar, estado, categoriaFiltro);
            return "admin/preguntas";
        }

        PreguntaFrecuente actualizada = preguntaService.actualizar(form);
        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "La pregunta “" + actualizada.getPregunta() + "” fue actualizada."
        );
        return redireccionListado(buscar, estado, categoriaFiltro);
    }

    @PostMapping("/publicacion")
    public String cambiarPublicacion(
            @RequestParam Long id,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false, defaultValue = "todas") String estado,
            @RequestParam(required = false) String categoria,
            RedirectAttributes redirectAttributes
    ) {
        PreguntaFrecuente pregunta = preguntaService.cambiarPublicacion(id);
        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                Boolean.TRUE.equals(pregunta.getActiva())
                        ? "La pregunta fue publicada en la landing."
                        : "La pregunta fue ocultada de la landing."
        );
        return redireccionListado(buscar, estado, categoria);
    }

    @PostMapping("/subir")
    public String subir(
            @RequestParam Long id,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false, defaultValue = "todas") String estado,
            @RequestParam(required = false) String categoria
    ) {
        preguntaService.moverArriba(id);
        return redireccionListado(buscar, estado, categoria);
    }

    @PostMapping("/bajar")
    public String bajar(
            @RequestParam Long id,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false, defaultValue = "todas") String estado,
            @RequestParam(required = false) String categoria
    ) {
        preguntaService.moverAbajo(id);
        return redireccionListado(buscar, estado, categoria);
    }

    @PostMapping("/eliminar")
    public String eliminar(
            @RequestParam Long id,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false, defaultValue = "todas") String estado,
            @RequestParam(required = false) String categoria,
            RedirectAttributes redirectAttributes
    ) {
        PreguntaFrecuente pregunta = preguntaService.obtenerPorId(id);
        preguntaService.eliminar(id);
        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "La pregunta “" + pregunta.getPregunta() + "” fue eliminada."
        );
        return redireccionListado(buscar, estado, categoria);
    }

    private void cargarListado(Model model, String buscar, String estado, String categoria) {
        String estadoNormalizado = normalizarEstado(estado);
        String categoriaNormalizada = normalizarCategoria(categoria);
        model.addAttribute(
                "preguntas",
                preguntaService.listar(
                        buscar,
                        convertirEstado(estadoNormalizado),
                        convertirCategoria(categoriaNormalizada)
                )
        );
        model.addAttribute("estadisticas", preguntaService.obtenerEstadisticas());
        model.addAttribute("buscar", buscar == null ? "" : buscar.trim());
        model.addAttribute("estado", estadoNormalizado);
        model.addAttribute("categoriaFiltro", categoriaNormalizada);
    }

    private void agregarFormulariosSiFaltan(Model model) {
        if (!model.containsAttribute("nuevaPregunta")) {
            model.addAttribute("nuevaPregunta", new PreguntaFrecuenteForm());
        }
        if (!model.containsAttribute("editarPregunta")) {
            model.addAttribute("editarPregunta", new PreguntaFrecuenteForm());
        }
    }

    private void validarPreguntaUnica(PreguntaFrecuenteForm form, BindingResult bindingResult) {
        if (form.getPregunta() != null
                && !bindingResult.hasFieldErrors("pregunta")
                && preguntaService.preguntaOcupada(form.getPregunta(), form.getId())) {
            bindingResult.rejectValue(
                    "pregunta",
                    "pregunta.duplicada",
                    "Ya existe una pregunta con el mismo contenido."
            );
        }
    }

    private Boolean convertirEstado(String estado) {
        if ("publicadas".equals(estado)) {
            return true;
        }
        if ("ocultas".equals(estado)) {
            return false;
        }
        return null;
    }

    private String normalizarEstado(String estado) {
        if ("publicadas".equalsIgnoreCase(estado)) {
            return "publicadas";
        }
        if ("ocultas".equalsIgnoreCase(estado)) {
            return "ocultas";
        }
        return "todas";
    }

    private CategoriaPregunta convertirCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return null;
        }
        return CategoriaPregunta.valueOf(categoria);
    }

    private String normalizarCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return "";
        }
        try {
            return CategoriaPregunta.valueOf(categoria.toUpperCase()).name();
        } catch (IllegalArgumentException exception) {
            return "";
        }
    }

    private String redireccionListado(String buscar, String estado, String categoria) {
        StringBuilder redirect = new StringBuilder("redirect:/admin/preguntas?estado=")
                .append(normalizarEstado(estado));
        if (buscar != null && !buscar.isBlank()) {
            redirect.append("&buscar=").append(codificar(buscar));
        }
        String categoriaNormalizada = normalizarCategoria(categoria);
        if (!categoriaNormalizada.isBlank()) {
            redirect.append("&categoria=").append(codificar(categoriaNormalizada));
        }
        return redirect.toString();
    }

    private String codificar(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }
}