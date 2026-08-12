package com.upgrade.app.controller;

import com.upgrade.app.domain.CategoriaServicio;
import com.upgrade.app.domain.EstadoServicio;
import com.upgrade.app.domain.Servicio;
import com.upgrade.app.domain.UnidadServicio;
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private static final int TAMANO_PAGINA = 9;

    private final ServicioService servicioService;

    @ModelAttribute("categoriasServicio")
    public List<CategoriaServicio> categoriasServicio() {
        return servicioService.listarCategorias();
    }

    @ModelAttribute("estadosServicio")
    public EstadoServicio[] estadosServicio() {
        return EstadoServicio.values();
    }

    @ModelAttribute("unidadesServicio")
    public UnidadServicio[] unidadesServicio() {
        return UnidadServicio.values();
    }

    @ModelAttribute("iconosServicio")
    public Map<String, String> iconosServicio() {
        Map<String, String> iconos = new LinkedHashMap<>();
        iconos.put("sparkles", "Destellos");
        iconos.put("headphones", "Audio");
        iconos.put("lightbulb", "Iluminación");
        iconos.put("monitor", "Pantalla");
        iconos.put("video", "Video");
        iconos.put("radio", "Streaming");
        iconos.put("music", "Entretenimiento");
        iconos.put("package", "Equipos");
        iconos.put("calendar", "Eventos");
        iconos.put("wrench", "Mantenimiento");
        return iconos;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long categoria,
            @RequestParam(required = false, defaultValue = "0") int page,
            Model model
    ) {
        cargarListado(model, buscar, estado, categoria, page);
        agregarFormulariosSiFaltan(model);
        return "admin/servicios";
    }

    @PostMapping
    public String crear(
            @Valid @ModelAttribute("nuevoServicio") ServicioForm form,
            BindingResult bindingResult,
            Filtros filtros,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        validarNombreUnico(form, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("editarServicio", formularioNuevo());
            model.addAttribute("modalAbierto", "nuevo");
            cargarListado(model, filtros.buscarFiltro(), filtros.estadoFiltro(), filtros.categoriaFiltro(), filtros.pageFiltro());
            return "admin/servicios";
        }

        Servicio creado = servicioService.crear(form);
        redirectAttributes.addFlashAttribute("mensajeExito", "El servicio “" + creado.getNombre() + "” fue creado.");
        return "redirect:/admin/servicios";
    }

    @PostMapping("/editar")
    public String editar(
            @Valid @ModelAttribute("editarServicio") ServicioForm form,
            BindingResult bindingResult,
            Filtros filtros,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (form.getId() == null) {
            bindingResult.reject("servicio.id.requerido", "No fue posible identificar el servicio.");
        }
        validarNombreUnico(form, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("nuevoServicio", formularioNuevo());
            model.addAttribute("modalAbierto", "editar");
            cargarListado(model, filtros.buscarFiltro(), filtros.estadoFiltro(), filtros.categoriaFiltro(), filtros.pageFiltro());
            return "admin/servicios";
        }

        Servicio actualizado = servicioService.actualizar(form);
        redirectAttributes.addFlashAttribute("mensajeExito", "Los cambios de “" + actualizado.getNombre() + "” fueron guardados.");
        return redireccionListado(filtros);
    }

    @PostMapping("/publicacion")
    public String cambiarPublicacion(
            @RequestParam Long id,
            Filtros filtros,
            RedirectAttributes redirectAttributes
    ) {
        Servicio servicio = servicioService.cambiarPublicacion(id);
        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                Boolean.TRUE.equals(servicio.getVisibleLanding())
                        ? "El servicio quedó habilitado para la landing."
                        : "El servicio fue ocultado de la landing."
        );
        return redireccionListado(filtros);
    }

    @PostMapping("/subir")
    public String subir(@RequestParam Long id, Filtros filtros) {
        servicioService.moverArriba(id);
        return redireccionListado(filtros);
    }

    @PostMapping("/bajar")
    public String bajar(@RequestParam Long id, Filtros filtros) {
        servicioService.moverAbajo(id);
        return redireccionListado(filtros);
    }

    @PostMapping("/eliminar")
    public String eliminar(
            @RequestParam Long id,
            @RequestParam String confirmacion,
            Filtros filtros,
            RedirectAttributes redirectAttributes
    ) {
        if (!"ELIMINAR".equals(confirmacion == null ? "" : confirmacion.trim())) {
            redirectAttributes.addFlashAttribute("mensajeError", "Debes escribir ELIMINAR para confirmar la baja.");
            return redireccionListado(filtros);
        }
        Servicio servicio = servicioService.obtenerPorId(id);
        servicioService.eliminarLogicamente(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "El servicio “" + servicio.getNombre() + "” fue eliminado.");
        return redireccionListado(filtros);
    }

    private void cargarListado(Model model, String buscar, String estado, Long categoria, int pagina) {
        String estadoNormalizado = normalizarEstado(estado);
        Page<Servicio> servicios = servicioService.listar(
                buscar,
                convertirEstado(estadoNormalizado),
                categoria,
                pagina,
                TAMANO_PAGINA
        );
        model.addAttribute("servicios", servicios);
        model.addAttribute("estadisticas", servicioService.obtenerEstadisticas());
        model.addAttribute("buscar", buscar == null ? "" : buscar.trim());
        model.addAttribute("estadoFiltro", estadoNormalizado);
        model.addAttribute("categoriaFiltro", categoria);

        int inicio = Math.max(0, servicios.getNumber() - 2);
        int fin = Math.min(Math.max(servicios.getTotalPages() - 1, 0), servicios.getNumber() + 2);
        model.addAttribute("paginasVisibles", servicios.getTotalPages() == 0
                ? List.of()
                : IntStream.rangeClosed(inicio, fin).boxed().toList());
    }

    private void agregarFormulariosSiFaltan(Model model) {
        if (!model.containsAttribute("nuevoServicio")) {
            model.addAttribute("nuevoServicio", formularioNuevo());
        }
        if (!model.containsAttribute("editarServicio")) {
            model.addAttribute("editarServicio", formularioNuevo());
        }
    }

    private ServicioForm formularioNuevo() {
        ServicioForm form = new ServicioForm();
        form.setOrden(servicioService.siguienteOrden());
        return form;
    }

    private void validarNombreUnico(ServicioForm form, BindingResult bindingResult) {
        if (!bindingResult.hasFieldErrors("nombre")
                && servicioService.nombreOcupado(form.getNombre(), form.getId())) {
            bindingResult.rejectValue("nombre", "servicio.nombre.duplicado", "Ya existe un servicio con este nombre.");
        }
        if (!bindingResult.hasFieldErrors("categoriaId")
                && !servicioService.categoriaDisponible(form.getCategoriaId())) {
            bindingResult.rejectValue("categoriaId", "servicio.categoria.invalida", "La categoría seleccionada no está disponible.");
        }
        if (!bindingResult.hasFieldErrors("icono")
                && !iconosServicio().containsKey(form.getIcono())) {
            bindingResult.rejectValue("icono", "servicio.icono.invalido", "Selecciona un icono válido.");
        }
    }

    private EstadoServicio convertirEstado(String valor) {
        if (valor == null || valor.isBlank() || "todos".equalsIgnoreCase(valor)) {
            return null;
        }
        try {
            return EstadoServicio.valueOf(valor.toUpperCase());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String normalizarEstado(String valor) {
        EstadoServicio estado = convertirEstado(valor);
        return estado == null ? "todos" : estado.name();
    }

    private String redireccionListado(Filtros filtros) {
        StringBuilder redirect = new StringBuilder("redirect:/admin/servicios?page=")
                .append(Math.max(filtros.pageFiltro(), 0))
                .append("&estado=")
                .append(normalizarEstado(filtros.estadoFiltro()));
        if (filtros.buscarFiltro() != null && !filtros.buscarFiltro().isBlank()) {
            redirect.append("&buscar=").append(URLEncoder.encode(filtros.buscarFiltro(), StandardCharsets.UTF_8));
        }
        if (filtros.categoriaFiltro() != null) {
            redirect.append("&categoria=").append(filtros.categoriaFiltro());
        }
        return redirect.toString();
    }

    public record Filtros(
            String buscarFiltro,
            String estadoFiltro,
            Long categoriaFiltro,
            int pageFiltro
    ) {
    }
}
