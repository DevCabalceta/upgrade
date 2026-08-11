package com.upgrade.app.controller;

import com.upgrade.app.domain.CategoriaInventario;
import com.upgrade.app.domain.EstadoInventario;
import com.upgrade.app.domain.Inventario;
import com.upgrade.app.dto.CambioEstadoInventarioForm;
import com.upgrade.app.dto.InventarioForm;
import com.upgrade.app.service.InventarioService;
import jakarta.servlet.http.HttpServletResponse;
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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private static final int TAMANO_PAGINA = 10;

    private final InventarioService inventarioService;

    @ModelAttribute("estadosInventario")
    public EstadoInventario[] estadosInventario() {
        return EstadoInventario.values();
    }

    @ModelAttribute("categoriasInventario")
    public List<CategoriaInventario> categoriasInventario() {
        return inventarioService.listarCategorias();
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
        return "admin/inventario";
    }

    @PostMapping
    public String crear(
            @Valid @ModelAttribute("nuevoEquipo") InventarioForm form,
            BindingResult bindingResult,
            Filtros filtros,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        validarFormulario(form, bindingResult);
        validarUnicos(form, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("editarEquipo", new InventarioForm());
            model.addAttribute("cambioEstado", new CambioEstadoInventarioForm());
            model.addAttribute("modalAbierto", "nuevo");
            cargarListado(model, filtros.buscar(), filtros.estado(), filtros.categoria(), filtros.page());
            return "admin/inventario";
        }

        Inventario creado = inventarioService.crear(form, identidad(principal));
        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "El equipo “" + creado.getCodigo() + " · " + creado.getNombre() + "” fue registrado."
        );
        return "redirect:/admin/inventario";
    }

    @PostMapping("/editar")
    public String editar(
            @Valid @ModelAttribute("editarEquipo") InventarioForm form,
            BindingResult bindingResult,
            Filtros filtros,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (form.getId() == null) {
            bindingResult.reject("inventario.id.requerido", "No fue posible identificar el equipo.");
        }
        validarFormulario(form, bindingResult);
        validarUnicos(form, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("nuevoEquipo", new InventarioForm());
            model.addAttribute("cambioEstado", new CambioEstadoInventarioForm());
            model.addAttribute("modalAbierto", "editar");
            cargarListado(model, filtros.buscar(), filtros.estado(), filtros.categoria(), filtros.page());
            return "admin/inventario";
        }

        Inventario actualizado = inventarioService.actualizar(form, identidad(principal));
        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "Los datos de “" + actualizado.getCodigo() + "” fueron actualizados."
        );
        return redireccionListado(filtros);
    }

    @PostMapping("/estado")
    public String cambiarEstado(
            @Valid @ModelAttribute("cambioEstado") CambioEstadoInventarioForm form,
            BindingResult bindingResult,
            Filtros filtros,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("nuevoEquipo", new InventarioForm());
            model.addAttribute("editarEquipo", new InventarioForm());
            model.addAttribute("modalAbierto", "estado");
            cargarListado(model, filtros.buscar(), filtros.estado(), filtros.categoria(), filtros.page());
            return "admin/inventario";
        }

        Inventario actualizado = inventarioService.cambiarEstado(form, identidad(principal));
        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "El estado de “" + actualizado.getCodigo() + "” fue actualizado."
        );
        return redireccionListado(filtros);
    }

    @PostMapping("/eliminar")
    public String eliminar(
            @RequestParam Long id,
            @RequestParam String confirmacion,
            Filtros filtros,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (!"ELIMINAR".equals(confirmacion == null ? "" : confirmacion.trim())) {
            redirectAttributes.addFlashAttribute("mensajeError", "Debes escribir ELIMINAR para confirmar la baja.");
            return redireccionListado(filtros);
        }
        Inventario inventario = inventarioService.obtenerPorId(id);
        inventarioService.eliminarLogicamente(id, identidad(principal));
        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "El equipo “" + inventario.getCodigo() + "” fue dado de baja."
        );
        return redireccionListado(filtros);
    }

    @GetMapping("/exportar")
    public void exportar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long categoria,
            HttpServletResponse response
    ) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=inventario-upgrade.csv");
        try (PrintWriter writer = response.getWriter()) {
            writer.write('\ufeff');
            writer.println("Código,Equipo,Categoría,Estado,Marca,Modelo,Serie,Bodega,Ubicación,Stock total,Disponible,Costo unitario,Precio venta,Fecha adquisición");
            for (Inventario item : inventarioService.listarParaExportar(buscar, convertirEstado(estado), categoria)) {
                writer.println(String.join(",",
                        csv(item.getCodigo()),
                        csv(item.getNombre()),
                        csv(item.getCategoria().getNombre()),
                        csv(item.getEstado().getEtiqueta()),
                        csv(item.getMarca()),
                        csv(item.getModelo()),
                        csv(item.getNumeroSerie()),
                        csv(item.getBodega()),
                        csv(item.getUbicacion()),
                        csv(item.getCantidadTotal()),
                        csv(item.getCantidadDisponible()),
                        csv(item.getValorUnitario()),
                        csv(item.getPrecioVenta()),
                        csv(item.getFechaAdquisicion())
                ));
            }
        }
    }

    private void cargarListado(Model model, String buscar, String estado, Long categoria, int pagina) {
        String estadoNormalizado = normalizarEstado(estado);
        Page<Inventario> equipos = inventarioService.listar(
                buscar,
                convertirEstado(estadoNormalizado),
                categoria,
                pagina,
                TAMANO_PAGINA
        );
        model.addAttribute("equipos", equipos);
        model.addAttribute("movimientosPorEquipo", inventarioService.movimientosPorEquipo(equipos.getContent()));
        model.addAttribute("estadisticas", inventarioService.obtenerEstadisticas());
        model.addAttribute("conteosCategoria", inventarioService.conteosPorCategoria());
        model.addAttribute("buscar", buscar == null ? "" : buscar.trim());
        model.addAttribute("estadoFiltro", estadoNormalizado);
        model.addAttribute("categoriaFiltro", categoria);

        int inicio = Math.max(0, equipos.getNumber() - 2);
        int fin = Math.min(Math.max(equipos.getTotalPages() - 1, 0), equipos.getNumber() + 2);
        model.addAttribute("paginasVisibles", equipos.getTotalPages() == 0
                ? List.of()
                : IntStream.rangeClosed(inicio, fin).boxed().toList());
    }

    private void agregarFormulariosSiFaltan(Model model) {
        if (!model.containsAttribute("nuevoEquipo")) {
            model.addAttribute("nuevoEquipo", new InventarioForm());
        }
        if (!model.containsAttribute("editarEquipo")) {
            model.addAttribute("editarEquipo", new InventarioForm());
        }
        if (!model.containsAttribute("cambioEstado")) {
            model.addAttribute("cambioEstado", new CambioEstadoInventarioForm());
        }
    }

    private void validarFormulario(InventarioForm form, BindingResult bindingResult) {
        if (form.getCantidadTotal() != null
                && form.getCantidadDisponible() != null
                && form.getCantidadDisponible() > form.getCantidadTotal()) {
            bindingResult.rejectValue(
                    "cantidadDisponible",
                    "inventario.disponible.invalido",
                    "La cantidad disponible no puede superar la cantidad total."
            );
        }
    }

    private void validarUnicos(InventarioForm form, BindingResult bindingResult) {
        if (!bindingResult.hasFieldErrors("codigo")
                && inventarioService.codigoOcupado(form.getCodigo(), form.getId())) {
            bindingResult.rejectValue("codigo", "inventario.codigo.duplicado", "Ya existe un equipo con este código.");
        }
        if (!bindingResult.hasFieldErrors("numeroSerie")
                && inventarioService.serieOcupada(form.getNumeroSerie(), form.getId())) {
            bindingResult.rejectValue(
                    "numeroSerie",
                    "inventario.serie.duplicada",
                    "Ya existe un equipo con este número de serie."
            );
        }
    }

    private EstadoInventario convertirEstado(String valor) {
        if (valor == null || valor.isBlank() || "todos".equalsIgnoreCase(valor)) {
            return null;
        }
        try {
            return EstadoInventario.valueOf(valor.toUpperCase());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String normalizarEstado(String valor) {
        EstadoInventario estado = convertirEstado(valor);
        return estado == null ? "todos" : estado.name();
    }

    private String identidad(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new IllegalStateException("La operación requiere un usuario autenticado.");
        }
        return principal.getName();
    }

    private String redireccionListado(Filtros filtros) {
        StringBuilder redirect = new StringBuilder("redirect:/admin/inventario?page=")
                .append(Math.max(filtros.page(), 0))
                .append("&estado=")
                .append(normalizarEstado(filtros.estado()));
        if (filtros.buscar() != null && !filtros.buscar().isBlank()) {
            redirect.append("&buscar=").append(URLEncoder.encode(filtros.buscar(), StandardCharsets.UTF_8));
        }
        if (filtros.categoria() != null) {
            redirect.append("&categoria=").append(filtros.categoria());
        }
        return redirect.toString();
    }

    private String csv(Object valor) {
        String texto = valor == null ? "" : String.valueOf(valor);
        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }

    public record Filtros(String buscar, String estado, Long categoria, int page) {
    }
}
