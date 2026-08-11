package com.upgrade.app.controller;

import com.upgrade.app.domain.CargoColaborador;
import com.upgrade.app.domain.DepartamentoColaborador;
import com.upgrade.app.domain.EstadoColaborador;
import com.upgrade.app.domain.Rol;
import com.upgrade.app.domain.Usuario;
import com.upgrade.app.dto.AsignarRolColaboradorForm;
import com.upgrade.app.dto.EditarColaboradorForm;
import com.upgrade.app.dto.EliminarColaboradorForm;
import com.upgrade.app.dto.NuevoColaboradorForm;
import com.upgrade.app.service.ColaboradorMailService;
import com.upgrade.app.service.ColaboradorService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.mail.MailException;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin/colaboradores")
@RequiredArgsConstructor
public class ColaboradorController {

    private static final int TAMANO_PAGINA = 9;
    private static final DateTimeFormatter FECHA_CSV = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ColaboradorService colaboradorService;
    private final ColaboradorMailService mailService;

    @Value("${app.mail.collaborator-recipient}")
    private String destinatarioCorreo;

    @ModelAttribute("rolesColaborador")
    public List<Rol> rolesColaborador() {
        return colaboradorService.listarRolesActivos();
    }

    @ModelAttribute("cargosColaborador")
    public List<CargoColaborador> cargosColaborador() {
        return colaboradorService.listarCargosActivos();
    }

    @ModelAttribute("departamentosColaborador")
    public List<DepartamentoColaborador> departamentosColaborador() {
        return colaboradorService.listarDepartamentosActivos();
    }

    @ModelAttribute("estadosColaborador")
    public List<EstadoColaborador> estadosColaborador() {
        return Arrays.asList(EstadoColaborador.values());
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false, defaultValue = "todos") String estado,
            @RequestParam(required = false) Long rol,
            @RequestParam(required = false, defaultValue = "0") int page,
            Model model
    ) {
        cargarListado(model, buscar, estado, rol, page);
        agregarFormulariosSiFaltan(model);
        return "admin/colaboradores";
    }

    @GetMapping("/exportar")
    public void exportar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false, defaultValue = "todos") String estado,
            @RequestParam(required = false) Long rol,
            HttpServletResponse response
    ) throws IOException {
        EstadoColaborador estadoEnum = convertirEstado(estado);
        List<Usuario> colaboradores = colaboradorService.listarParaExportar(buscar, estadoEnum, rol);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=colaboradores-upgrade.csv");
        response.getWriter().write('\ufeff');
        response.getWriter().println("Usuario,Nombre,Apellido,Correo,Teléfono,Cargo,Departamento,Estado,Rol,Fecha ingreso,Eventos");
        for (Usuario usuario : colaboradores) {
            response.getWriter().println(String.join(",",
                    csv(usuario.getUsername()),
                    csv(usuario.getNombre()),
                    csv(usuario.getApellido()),
                    csv(usuario.getEmail()),
                    csv(usuario.getTelefono()),
                    csv(usuario.getCargoNombre()),
                    csv(usuario.getDepartamentoNombre()),
                    csv(usuario.getEstadoColaborador().getEtiqueta()),
                    csv(usuario.getRolPrincipalNombre()),
                    csv(usuario.getFechaIngreso() == null ? "" : FECHA_CSV.format(usuario.getFechaIngreso())),
                    csv(usuario.getEventosAsignados())
            ));
        }
    }

    @PostMapping
    public String crear(
            @Valid @ModelAttribute("nuevoColaborador") NuevoColaboradorForm form,
            BindingResult bindingResult,
            Filtros filtros,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        validarNuevo(form, bindingResult);
        if (bindingResult.hasErrors()) {
            prepararError(model, filtros, "nuevo", null);
            return "admin/colaboradores";
        }

        Usuario colaborador;
        try {
            colaborador = colaboradorService.crear(form);
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("colaborador.creacion", exception.getMessage());
            prepararError(model, filtros, "nuevo", null);
            return "admin/colaboradores";
        }
        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "El colaborador “" + colaborador.getNombreCompleto() + "” fue creado y ya puede iniciar sesión."
        );
        if (Boolean.TRUE.equals(form.getEnviarInvitacion())) {
            try {
                mailService.enviarInvitacion(colaborador);
                redirectAttributes.addFlashAttribute(
                        "mensajeCorreo",
                        "La invitación de prueba fue enviada a " + destinatarioCorreo + "."
                );
            } catch (MailException exception) {
                redirectAttributes.addFlashAttribute(
                        "mensajeAdvertencia",
                        "La cuenta quedó creada, pero no fue posible enviar la invitación. Revisa la contraseña de aplicación de Gmail."
                );
            }
        }
        return "redirect:/admin/colaboradores";
    }

    @PostMapping("/editar")
    public String editar(
            @Valid @ModelAttribute("editarColaborador") EditarColaboradorForm form,
            BindingResult bindingResult,
            Filtros filtros,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        validarEdicion(form, bindingResult);
        if (bindingResult.hasErrors()) {
            prepararError(model, filtros, "editar", form.getId());
            return "admin/colaboradores";
        }
        Usuario colaborador;
        try {
            colaborador = colaboradorService.actualizar(form);
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("colaborador.edicion", exception.getMessage());
            prepararError(model, filtros, "editar", form.getId());
            return "admin/colaboradores";
        }
        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "Los datos de “" + colaborador.getNombreCompleto() + "” fueron actualizados."
        );
        return redireccionListado(filtros);
    }

    @PostMapping("/rol")
    public String asignarRol(
            @Valid @ModelAttribute("asignarRolColaborador") AsignarRolColaboradorForm form,
            BindingResult bindingResult,
            Filtros filtros,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepararError(model, filtros, "rol", form.getColaboradorId());
            return "admin/colaboradores";
        }
        Usuario colaborador;
        try {
            colaborador = colaboradorService.asignarRol(form.getColaboradorId(), form.getRolId());
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("colaborador.rol", exception.getMessage());
            prepararError(model, filtros, "rol", form.getColaboradorId());
            return "admin/colaboradores";
        }
        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "Se asignó el rol “" + colaborador.getRolPrincipalNombre() + "” a " + colaborador.getNombreCompleto() + "."
        );
        return redireccionListado(filtros);
    }

    @PostMapping("/eliminar")
    public String eliminar(
            @Valid @ModelAttribute("eliminarColaborador") EliminarColaboradorForm form,
            BindingResult bindingResult,
            Filtros filtros,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (!bindingResult.hasFieldErrors("confirmacion")
                && !"ELIMINAR".equals(form.getConfirmacion().trim())) {
            bindingResult.rejectValue("confirmacion", "colaborador.confirmacion", "Debes escribir exactamente ELIMINAR.");
        }
        if (bindingResult.hasErrors()) {
            prepararError(model, filtros, "eliminar", form.getId());
            return "admin/colaboradores";
        }
        try {
            colaboradorService.eliminarLogicamente(form.getId(), identidad(principal));
            redirectAttributes.addFlashAttribute("mensajeExito", "El colaborador fue desactivado correctamente.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("mensajeError", exception.getMessage());
        }
        return redireccionListado(filtros);
    }

    private void validarNuevo(NuevoColaboradorForm form, BindingResult bindingResult) {
        if (!bindingResult.hasFieldErrors("confirmarPassword")
                && form.getPassword() != null
                && !form.getPassword().equals(form.getConfirmarPassword())) {
            bindingResult.rejectValue("confirmarPassword", "colaborador.password.no.coincide", "Las contraseñas no coinciden.");
        }
        if (!bindingResult.hasFieldErrors("username")
                && colaboradorService.usernameOcupado(form.getUsername(), null)) {
            bindingResult.rejectValue("username", "colaborador.username.duplicado", "Ese nombre de usuario ya está registrado.");
        }
        if (!bindingResult.hasFieldErrors("email")
                && colaboradorService.correoOcupado(form.getEmail(), null)) {
            bindingResult.rejectValue("email", "colaborador.email.duplicado", "Ese correo ya está registrado.");
        }
    }

    private void validarEdicion(EditarColaboradorForm form, BindingResult bindingResult) {
        if (!bindingResult.hasFieldErrors("username")
                && colaboradorService.usernameOcupado(form.getUsername(), form.getId())) {
            bindingResult.rejectValue("username", "colaborador.username.duplicado", "Ese nombre de usuario ya está registrado.");
        }
        if (!bindingResult.hasFieldErrors("email")
                && colaboradorService.correoOcupado(form.getEmail(), form.getId())) {
            bindingResult.rejectValue("email", "colaborador.email.duplicado", "Ese correo ya está registrado.");
        }
    }

    private void prepararError(Model model, Filtros filtros, String modal, Long colaboradorId) {
        model.addAttribute("modalAbierto", modal);
        model.addAttribute("colaboradorModalId", colaboradorId);
        cargarListado(
                model,
                filtros.buscarFiltro(),
                filtros.estadoFiltro(),
                filtros.rolFiltro(),
                filtros.pageFiltro()
        );
        agregarFormulariosSiFaltan(model);
    }

    private void cargarListado(Model model, String buscar, String estado, Long rol, int pagina) {
        EstadoColaborador estadoEnum = convertirEstado(estado);
        Page<Usuario> colaboradores = colaboradorService.listar(buscar, estadoEnum, rol, pagina, TAMANO_PAGINA);
        model.addAttribute("colaboradores", colaboradores);
        model.addAttribute("buscar", buscar == null ? "" : buscar.trim());
        model.addAttribute("estadoFiltro", estadoEnum == null ? "todos" : estadoEnum.name());
        model.addAttribute("rolFiltro", rol);
        model.addAttribute("destinatarioCorreo", destinatarioCorreo);

        int inicio = Math.max(0, colaboradores.getNumber() - 2);
        int fin = Math.min(Math.max(colaboradores.getTotalPages() - 1, 0), colaboradores.getNumber() + 2);
        model.addAttribute("paginasVisibles", colaboradores.getTotalPages() == 0
                ? List.of()
                : IntStream.rangeClosed(inicio, fin).boxed().toList());
    }

    private void agregarFormulariosSiFaltan(Model model) {
        if (!model.containsAttribute("nuevoColaborador")) {
            model.addAttribute("nuevoColaborador", new NuevoColaboradorForm());
        }
        if (!model.containsAttribute("editarColaborador")) {
            model.addAttribute("editarColaborador", new EditarColaboradorForm());
        }
        if (!model.containsAttribute("asignarRolColaborador")) {
            model.addAttribute("asignarRolColaborador", new AsignarRolColaboradorForm());
        }
        if (!model.containsAttribute("eliminarColaborador")) {
            model.addAttribute("eliminarColaborador", new EliminarColaboradorForm());
        }
    }

    private EstadoColaborador convertirEstado(String valor) {
        if (valor == null || valor.isBlank() || "todos".equalsIgnoreCase(valor)) {
            return null;
        }
        try {
            return EstadoColaborador.valueOf(valor.toUpperCase());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String redireccionListado(Filtros filtros) {
        StringBuilder redirect = new StringBuilder("redirect:/admin/colaboradores?page=")
                .append(Math.max(filtros.pageFiltro(), 0))
                .append("&estado=")
                .append(filtros.estadoFiltro() == null || filtros.estadoFiltro().isBlank() ? "todos" : filtros.estadoFiltro());
        if (filtros.buscarFiltro() != null && !filtros.buscarFiltro().isBlank()) {
            redirect.append("&buscar=").append(URLEncoder.encode(filtros.buscarFiltro(), StandardCharsets.UTF_8));
        }
        if (filtros.rolFiltro() != null) {
            redirect.append("&rol=").append(filtros.rolFiltro());
        }
        return redirect.toString();
    }

    private String identidad(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new IllegalStateException("La operación requiere un usuario autenticado.");
        }
        return principal.getName();
    }

    private String csv(Object valor) {
        String texto = valor == null ? "" : String.valueOf(valor);
        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }

    public record Filtros(
            String buscarFiltro,
            String estadoFiltro,
            Long rolFiltro,
            int pageFiltro
    ) {
    }
}
