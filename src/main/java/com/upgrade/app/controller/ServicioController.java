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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private static final int TAMANO_PAGINA = 10;

    private final ServicioService servicioService;

    @GetMapping
    public String listar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false, defaultValue = "todos") String estado,
            @RequestParam(required = false, defaultValue = "0") int page,
            Model model
    ) {
        cargarListado(model, buscar, estado, page);
        agregarFormulariosSiFaltan(model);
        return "admin/servicios";
    }

    @PostMapping
    public String crear(
            @Valid @ModelAttribute("nuevoServicio") ServicioForm form,
            BindingResult bindingResult,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false, defaultValue = "todos") String estado,
            @RequestParam(required = false, defaultValue = "0") int page,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        validarUnicos(form, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("editarServicio", new ServicioForm());
            model.addAttribute("modalAbierto", "nuevo");
            cargarListado(model, buscar, estado, page);
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
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false, defaultValue = "todos") String estado,
            @RequestParam(required = false, defaultValue = "0") int page,
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
            cargarListado(model, buscar, estado, page);
            return "admin/servicios";
        }

        Servicio servicio = servicioService.actualizar(form);
        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "Los datos de “" + servicio.getNombre() + "” fueron actualizados."
        );
        return redireccionListado(buscar, estado, page);
    }

    @PostMapping("/eliminar")
    public String eliminar(
            @RequestParam Long id,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false, defaultValue = "todos") String estado,
            @RequestParam(required = false, defaultValue = "0") int page,
            RedirectAttributes redirectAttributes
    ) {
        Servicio servicio = servicioService.obtenerPorId(id);
        servicioService.eliminarLogicamente(id);
        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "El servicio “" + servicio.getNombre() + "” fue desactivado correctamente."
        );
        return redireccionListado(buscar, estado, page);
    }

    private void cargarListado(Model model, String buscar, String estado, int page) {
        String estadoNormalizado = normalizarEstado(estado);
        Page<Servicio> servicios = servicioService.listar(
                buscar,
                convertirEstado(estadoNormalizado),
                page,
                TAMANO_PAGINA
        );
        model.addAttribute("servicios", servicios);
        model.addAttribute("buscar", buscar == null ? "" : buscar.trim());
        model.addAttribute("estado", estadoNormalizado);

        int inicio = Math.max(0, servicios.getNumber() - 2);
        int fin = Math.min(Math.max(servicios.getTotalPages() - 1, 0), servicios.getNumber() + 2);
        List<Integer> paginasVisibles = servicios.getTotalPages() == 0
                ? List.of()
                : IntStream.rangeClosed(inicio, fin).boxed().toList();
        model.addAttribute("paginasVisibles", paginasVisibles);
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

    private Boolean convertirEstado(String estado) {
        if ("activos".equalsIgnoreCase(estado)) {
            return true;
        }
        if ("inactivos".equalsIgnoreCase(estado)) {
            return false;
        }
        return null;
    }

    private String normalizarEstado(String estado) {
        if ("activos".equalsIgnoreCase(estado)) {
            return "activos";
        }
        if ("inactivos".equalsIgnoreCase(estado)) {
            return "inactivos";
        }
        return "todos";
    }

    private String redireccionListado(String buscar, String estado, int page) {
        StringBuilder redirect = new StringBuilder("redirect:/admin/servicios?page=")
                .append(Math.max(page, 0))
                .append("&estado=")
                .append(normalizarEstado(estado));
        if (buscar != null && !buscar.isBlank()) {
            redirect.append("&buscar=").append(java.net.URLEncoder.encode(buscar, StandardCharsets.UTF_8));
        }
        return redirect.toString();
    }
}