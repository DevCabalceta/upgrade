package com.upgrade.app.controller;

import com.upgrade.app.domain.EstadoMantenimiento;
import com.upgrade.app.domain.Cliente;
import com.upgrade.app.domain.Inventario;
import com.upgrade.app.domain.OrdenMantenimiento;
import com.upgrade.app.domain.PrioridadMantenimiento;
import com.upgrade.app.domain.TipoMantenimiento;
import com.upgrade.app.domain.Usuario;
import com.upgrade.app.dto.AsignarTecnicoMantenimientoForm;
import com.upgrade.app.dto.CerrarMantenimientoForm;
import com.upgrade.app.dto.EliminarMantenimientoForm;
import com.upgrade.app.dto.MantenimientoForm;
import com.upgrade.app.dto.ObservacionMantenimientoForm;
import com.upgrade.app.exception.ArchivoMantenimientoException;
import com.upgrade.app.service.MantenimientoService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin/mantenimiento")
@RequiredArgsConstructor
public class MantenimientoController {

    private static final int TAMANO_PAGINA = 10;

    private final MantenimientoService mantenimientoService;

    @ModelAttribute("tiposMantenimiento")
    public TipoMantenimiento[] tiposMantenimiento() {
        return TipoMantenimiento.values();
    }

    @ModelAttribute("prioridadesMantenimiento")
    public PrioridadMantenimiento[] prioridadesMantenimiento() {
        return PrioridadMantenimiento.values();
    }

    @ModelAttribute("estadosMantenimiento")
    public List<EstadoMantenimiento> estadosMantenimiento() {
        return List.of(
                EstadoMantenimiento.PROGRAMADO,
                EstadoMantenimiento.EN_PROCESO,
                EstadoMantenimiento.CANCELADO
        );
    }

    @ModelAttribute("todosEstadosMantenimiento")
    public EstadoMantenimiento[] todosEstadosMantenimiento() {
        return EstadoMantenimiento.values();
    }

    @ModelAttribute("equiposMantenimiento")
    public List<Inventario> equiposMantenimiento() {
        return mantenimientoService.listarEquipos();
    }

    @ModelAttribute("clientesMantenimiento")
    public List<Cliente> clientesMantenimiento() {
        return mantenimientoService.listarClientes();
    }

    @ModelAttribute("tecnicosMantenimiento")
    public List<Usuario> tecnicosMantenimiento() {
        return mantenimientoService.listarTecnicos();
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String prioridad,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        cargarListado(model, buscar, tipo, estado, prioridad, page);
        agregarFormulariosSiFaltan(model);
        return "admin/mantenimiento";
    }

    @PostMapping
    public String crear(
            @Valid @ModelAttribute("nuevaOrden") MantenimientoForm form,
            BindingResult bindingResult,
            Filtros filtros,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepararError(model, filtros, "nueva", null);
            return "admin/mantenimiento";
        }
        try {
            OrdenMantenimiento orden = mantenimientoService.crear(form, identidad(principal));
            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "La orden “" + orden.getNumero() + "” fue creada correctamente."
            );
            return "redirect:/admin/mantenimiento";
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("mantenimiento.negocio", exception.getMessage());
            prepararError(model, filtros, "nueva", null);
            return "admin/mantenimiento";
        }
    }

    @PostMapping("/editar")
    public String editar(
            @Valid @ModelAttribute("editarOrden") MantenimientoForm form,
            BindingResult bindingResult,
            Filtros filtros,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (form.getId() == null) {
            bindingResult.reject("mantenimiento.id", "No fue posible identificar la orden.");
        }
        if (bindingResult.hasErrors()) {
            prepararError(model, filtros, "editar", form.getId());
            return "admin/mantenimiento";
        }
        try {
            OrdenMantenimiento orden = mantenimientoService.actualizar(form, identidad(principal));
            redirectAttributes.addFlashAttribute("mensajeExito", "La orden “" + orden.getNumero() + "” fue actualizada.");
            return redireccionListado(filtros);
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("mantenimiento.negocio", exception.getMessage());
            prepararError(model, filtros, "editar", form.getId());
            return "admin/mantenimiento";
        }
    }

    @PostMapping("/tecnico")
    public String asignarTecnico(
            @Valid @ModelAttribute("asignarTecnico") AsignarTecnicoMantenimientoForm form,
            BindingResult bindingResult,
            Filtros filtros,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepararError(model, filtros, "tecnico", form.getOrdenId());
            return "admin/mantenimiento";
        }
        try {
            OrdenMantenimiento orden = mantenimientoService.asignarTecnico(form);
            redirectAttributes.addFlashAttribute("mensajeExito", "Técnico asignado a “" + orden.getNumero() + "”.");
            return redireccionListado(filtros);
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("mantenimiento.tecnico", exception.getMessage());
            prepararError(model, filtros, "tecnico", form.getOrdenId());
            return "admin/mantenimiento";
        }
    }

    @PostMapping("/observaciones")
    public String agregarObservacion(
            @Valid @ModelAttribute("observacionOrden") ObservacionMantenimientoForm form,
            BindingResult bindingResult,
            Filtros filtros,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepararError(model, filtros, "observacion", form.getOrdenId());
            return "admin/mantenimiento";
        }
        mantenimientoService.agregarObservacion(form, identidad(principal));
        redirectAttributes.addFlashAttribute("mensajeExito", "Observación añadida correctamente.");
        redirectAttributes.addFlashAttribute("abrirDetalleId", form.getOrdenId());
        return redireccionListado(filtros);
    }

    @PostMapping("/evidencias")
    public String subirEvidencias(
            @RequestParam Long ordenId,
            @RequestParam("archivos") MultipartFile[] archivos,
            Filtros filtros,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            int cantidad = mantenimientoService.guardarEvidencias(ordenId, archivos, identidad(principal)).size();
            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    cantidad == 1 ? "Evidencia subida correctamente." : cantidad + " evidencias subidas correctamente."
            );
            redirectAttributes.addFlashAttribute("abrirDetalleId", ordenId);
            return redireccionListado(filtros);
        } catch (ArchivoMantenimientoException | IllegalArgumentException exception) {
            model.addAttribute("mensajeError", exception.getMessage());
            prepararError(model, filtros, "evidencia", ordenId);
            return "admin/mantenimiento";
        }
    }

    @PostMapping("/checklist")
    public String alternarChecklist(
            @RequestParam Long checklistId,
            @RequestParam Long ordenId,
            Filtros filtros,
            RedirectAttributes redirectAttributes
    ) {
        try {
            mantenimientoService.alternarChecklist(checklistId);
            redirectAttributes.addFlashAttribute("abrirDetalleId", ordenId);
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("mensajeError", exception.getMessage());
            redirectAttributes.addFlashAttribute("abrirDetalleId", ordenId);
        }
        return redireccionListado(filtros);
    }

    @PostMapping("/cerrar")
    public String cerrar(
            @Valid @ModelAttribute("cerrarOrden") CerrarMantenimientoForm form,
            BindingResult bindingResult,
            Filtros filtros,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepararError(model, filtros, "cerrar", form.getOrdenId());
            return "admin/mantenimiento";
        }
        try {
            OrdenMantenimiento orden = mantenimientoService.cerrar(form, identidad(principal));
            redirectAttributes.addFlashAttribute("mensajeExito", "La orden “" + orden.getNumero() + "” fue finalizada.");
            return redireccionListado(filtros);
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("mantenimiento.cierre", exception.getMessage());
            prepararError(model, filtros, "cerrar", form.getOrdenId());
            return "admin/mantenimiento";
        }
    }

    @PostMapping("/eliminar")
    public String eliminar(
            @Valid @ModelAttribute("eliminarOrden") EliminarMantenimientoForm form,
            BindingResult bindingResult,
            Filtros filtros,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepararError(model, filtros, "eliminar", form.getOrdenId());
            return "admin/mantenimiento";
        }
        try {
            OrdenMantenimiento orden = mantenimientoService.eliminarLogicamente(
                    form.getOrdenId(),
                    form.getConfirmacion(),
                    identidad(principal)
            );
            redirectAttributes.addFlashAttribute("mensajeExito", "La orden “" + orden.getNumero() + "” fue eliminada.");
            return redireccionListado(filtros);
        } catch (IllegalArgumentException exception) {
            bindingResult.rejectValue("confirmacion", "mantenimiento.confirmacion", exception.getMessage());
            prepararError(model, filtros, "eliminar", form.getOrdenId());
            return "admin/mantenimiento";
        }
    }

    private void cargarListado(
            Model model,
            String buscar,
            String tipo,
            String estado,
            String prioridad,
            int pagina
    ) {
        TipoMantenimiento tipoEnum = convertirEnum(tipo, TipoMantenimiento.values(), TipoMantenimiento::name);
        EstadoMantenimiento estadoEnum = convertirEnum(estado, EstadoMantenimiento.values(), EstadoMantenimiento::name);
        PrioridadMantenimiento prioridadEnum = convertirEnum(
                prioridad,
                PrioridadMantenimiento.values(),
                PrioridadMantenimiento::name
        );
        Page<OrdenMantenimiento> ordenes = mantenimientoService.listar(
                buscar,
                tipoEnum,
                estadoEnum,
                prioridadEnum,
                pagina,
                TAMANO_PAGINA
        );
        model.addAttribute("ordenes", ordenes);
        model.addAttribute("detallesOrdenes", mantenimientoService.obtenerDetalles(ordenes.getContent()));
        model.addAttribute("estadisticas", mantenimientoService.obtenerEstadisticas());
        model.addAttribute("buscar", buscar == null ? "" : buscar.trim());
        model.addAttribute("tipoFiltro", tipoEnum == null ? "todos" : tipoEnum.name());
        model.addAttribute("estadoFiltro", estadoEnum == null ? "todos" : estadoEnum.name());
        model.addAttribute("prioridadFiltro", prioridadEnum == null ? "todos" : prioridadEnum.name());

        int inicio = Math.max(0, ordenes.getNumber() - 2);
        int fin = Math.min(Math.max(ordenes.getTotalPages() - 1, 0), ordenes.getNumber() + 2);
        model.addAttribute("paginasVisibles", ordenes.getTotalPages() == 0
                ? List.of()
                : IntStream.rangeClosed(inicio, fin).boxed().toList());
    }

    private void prepararError(Model model, Filtros filtros, String modal, Long ordenId) {
        agregarFormulariosSiFaltan(model);
        model.addAttribute("modalAbierto", modal);
        model.addAttribute("ordenModalId", ordenId);
        cargarListado(
                model,
                filtros.buscarFiltro(),
                filtros.tipoFiltro(),
                filtros.estadoFiltro(),
                filtros.prioridadFiltro(),
                filtros.pageFiltro()
        );
    }

    private void agregarFormulariosSiFaltan(Model model) {
        if (!model.containsAttribute("nuevaOrden")) model.addAttribute("nuevaOrden", new MantenimientoForm());
        if (!model.containsAttribute("editarOrden")) model.addAttribute("editarOrden", new MantenimientoForm());
        if (!model.containsAttribute("asignarTecnico")) {
            model.addAttribute("asignarTecnico", new AsignarTecnicoMantenimientoForm());
        }
        if (!model.containsAttribute("observacionOrden")) {
            model.addAttribute("observacionOrden", new ObservacionMantenimientoForm());
        }
        if (!model.containsAttribute("cerrarOrden")) model.addAttribute("cerrarOrden", new CerrarMantenimientoForm());
        if (!model.containsAttribute("eliminarOrden")) {
            model.addAttribute("eliminarOrden", new EliminarMantenimientoForm());
        }
    }

    private <E extends Enum<E>> E convertirEnum(String valor, E[] valores, Function<E, String> nombre) {
        if (valor == null || valor.isBlank() || "todos".equalsIgnoreCase(valor)) {
            return null;
        }
        String normalizado = valor.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(valores)
                .filter(item -> nombre.apply(item).equalsIgnoreCase(normalizado))
                .findFirst()
                .orElse(null);
    }

    private String redireccionListado(Filtros filtros) {
        StringBuilder redirect = new StringBuilder("redirect:/admin/mantenimiento?page=")
                .append(Math.max(filtros.pageFiltro(), 0))
                .append("&tipo=").append(valorFiltro(filtros.tipoFiltro()))
                .append("&estado=").append(valorFiltro(filtros.estadoFiltro()))
                .append("&prioridad=").append(valorFiltro(filtros.prioridadFiltro()));
        if (filtros.buscarFiltro() != null && !filtros.buscarFiltro().isBlank()) {
            redirect.append("&buscar=")
                    .append(URLEncoder.encode(filtros.buscarFiltro(), StandardCharsets.UTF_8));
        }
        return redirect.toString();
    }

    private String valorFiltro(String valor) {
        return valor == null || valor.isBlank() ? "todos" : valor;
    }

    private String identidad(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new IllegalStateException("La operación requiere un usuario autenticado.");
        }
        return principal.getName();
    }

    public record Filtros(
            String buscarFiltro,
            String tipoFiltro,
            String estadoFiltro,
            String prioridadFiltro,
            int pageFiltro
    ) {
    }
}
