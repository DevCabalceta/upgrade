package com.upgrade.app.controller;

import com.upgrade.app.domain.Cliente;
import com.upgrade.app.domain.CondicionEquipo;
import com.upgrade.app.domain.Inventario;
import com.upgrade.app.domain.Prestamo;
import com.upgrade.app.domain.Usuario;
import com.upgrade.app.dto.DevolucionPrestamoForm;
import com.upgrade.app.dto.NotificacionPrestamoForm;
import com.upgrade.app.dto.PrestamoForm;
import com.upgrade.app.service.PrestamoMailService;
import com.upgrade.app.service.PrestamoService;
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin/prestamos")
@RequiredArgsConstructor
public class PrestamoController {

    private static final int TAMANO_PAGINA = 10;

    private final PrestamoService prestamoService;
    private final PrestamoMailService mailService;

    @Value("${app.mail.loan-recipient}")
    private String destinatarioCorreo;

    @ModelAttribute("clientesPrestamo")
    public List<Cliente> clientesPrestamo() {
        return prestamoService.listarClientesActivos();
    }

    @ModelAttribute("equiposPrestamo")
    public List<Inventario> equiposPrestamo() {
        return prestamoService.listarEquiposDisponibles();
    }

    @ModelAttribute("responsablesPrestamo")
    public List<Usuario> responsablesPrestamo() {
        return prestamoService.listarResponsablesActivos();
    }

    @ModelAttribute("condicionesSalida")
    public List<CondicionEquipo> condicionesSalida() {
        return List.of(CondicionEquipo.OPTIMO, CondicionEquipo.CON_DETALLES, CondicionEquipo.RECIEN_REPARADO);
    }

    @ModelAttribute("condicionesDevolucion")
    public List<CondicionEquipo> condicionesDevolucion() {
        return Arrays.asList(CondicionEquipo.values());
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false, defaultValue = "todos") String estado,
            @RequestParam(required = false, defaultValue = "todos") String fecha,
            @RequestParam(required = false, defaultValue = "0") int page,
            Model model
    ) {
        cargarListado(model, buscar, estado, fecha, page);
        agregarFormulariosSiFaltan(model);
        return "admin/prestamos";
    }

    @PostMapping
    public String crear(
            @Valid @ModelAttribute("nuevoPrestamo") PrestamoForm form,
            BindingResult bindingResult,
            Filtros filtros,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (!bindingResult.hasErrors()
                && form.getFechaSalida() != null
                && form.getFechaDevolucionEstimada() != null
                && form.getFechaDevolucionEstimada().isBefore(form.getFechaSalida())) {
            bindingResult.rejectValue(
                    "fechaDevolucionEstimada",
                    "prestamo.fecha.invalida",
                    "La devolución estimada no puede ser anterior a la salida."
            );
        }
        if (bindingResult.hasErrors()) {
            prepararError(model, filtros, "nuevo");
            return "admin/prestamos";
        }

        Prestamo prestamo;
        try {
            prestamo = prestamoService.crear(form, identidad(principal));
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("prestamo.negocio", exception.getMessage());
            prepararError(model, filtros, "nuevo");
            return "admin/prestamos";
        }

        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "El préstamo “" + prestamo.getFolio() + "” fue registrado correctamente."
        );
        try {
            mailService.enviarRegistro(prestamo);
            redirectAttributes.addFlashAttribute("mensajeCorreo", "La notificación fue enviada a " + destinatarioCorreo + ".");
        } catch (MailException exception) {
            redirectAttributes.addFlashAttribute(
                    "mensajeAdvertencia",
                    "El préstamo quedó registrado, pero no fue posible enviar el correo. Revisa la contraseña de aplicación de Gmail."
            );
        }
        return "redirect:/admin/prestamos";
    }

    @PostMapping("/devolver")
    public String devolver(
            @Valid @ModelAttribute("devolucionPrestamo") DevolucionPrestamoForm form,
            BindingResult bindingResult,
            Filtros filtros,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepararError(model, filtros, "devolucion");
            return "admin/prestamos";
        }

        Prestamo prestamo;
        try {
            prestamo = prestamoService.registrarDevolucion(form, identidad(principal));
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("prestamo.devolucion", exception.getMessage());
            prepararError(model, filtros, "devolucion");
            return "admin/prestamos";
        }

        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "La devolución de “" + prestamo.getFolio() + "” fue registrada."
        );
        try {
            mailService.enviarDevolucion(prestamo);
            redirectAttributes.addFlashAttribute("mensajeCorreo", "El comprobante fue enviado a " + destinatarioCorreo + ".");
        } catch (MailException exception) {
            redirectAttributes.addFlashAttribute(
                    "mensajeAdvertencia",
                    "La devolución quedó registrada, pero no fue posible enviar el correo."
            );
        }
        return redireccionListado(filtros);
    }

    @PostMapping("/notificar")
    public String notificar(
            @Valid @ModelAttribute("notificacionPrestamo") NotificacionPrestamoForm form,
            BindingResult bindingResult,
            Filtros filtros,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Prestamo prestamo = null;
        if (!bindingResult.hasErrors()) {
            prestamo = prestamoService.obtenerPorId(form.getId());
            if (!prestamo.isVencido()) {
                bindingResult.reject("prestamo.no.vencido", "Solo se pueden enviar alertas para préstamos vencidos.");
            }
        }
        if (bindingResult.hasErrors()) {
            prepararError(model, filtros, "notificacion");
            return "admin/prestamos";
        }

        try {
            mailService.enviarVencimiento(prestamo, form.getMensaje());
            prestamoService.marcarNotificacionEnviada(prestamo.getId());
            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "La alerta de “" + prestamo.getFolio() + "” fue enviada a " + destinatarioCorreo + "."
            );
        } catch (MailException exception) {
            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    "No fue posible enviar la alerta. Verifica la configuración SMTP de Gmail."
            );
        }
        return redireccionListado(filtros);
    }

    private void prepararError(Model model, Filtros filtros, String modal) {
        if (!model.containsAttribute("nuevoPrestamo")) {
            model.addAttribute("nuevoPrestamo", new PrestamoForm());
        }
        if (!model.containsAttribute("devolucionPrestamo")) {
            model.addAttribute("devolucionPrestamo", new DevolucionPrestamoForm());
        }
        if (!model.containsAttribute("notificacionPrestamo")) {
            model.addAttribute("notificacionPrestamo", new NotificacionPrestamoForm());
        }
        model.addAttribute("modalAbierto", modal);
        cargarListado(
                model,
                filtros.buscarFiltro(),
                filtros.estadoFiltro(),
                filtros.fechaFiltro(),
                filtros.pageFiltro()
        );
    }

    private void cargarListado(Model model, String buscar, String estado, String fecha, int pagina) {
        String estadoNormalizado = prestamoService.normalizarFiltroEstado(estado);
        String fechaNormalizada = prestamoService.normalizarFiltroFecha(fecha);
        Page<Prestamo> prestamos = prestamoService.listar(
                buscar,
                estadoNormalizado,
                fechaNormalizada,
                pagina,
                TAMANO_PAGINA
        );
        model.addAttribute("prestamos", prestamos);
        model.addAttribute("estadisticas", prestamoService.obtenerEstadisticas());
        model.addAttribute("prestamosVencidos", prestamoService.listarVencidos());
        model.addAttribute("buscar", buscar == null ? "" : buscar.trim());
        model.addAttribute("estadoFiltro", estadoNormalizado == null ? "todos" : estadoNormalizado);
        model.addAttribute("fechaFiltro", fechaNormalizada == null ? "todos" : fechaNormalizada);
        model.addAttribute("destinatarioCorreo", destinatarioCorreo);

        int inicio = Math.max(0, prestamos.getNumber() - 2);
        int fin = Math.min(Math.max(prestamos.getTotalPages() - 1, 0), prestamos.getNumber() + 2);
        model.addAttribute("paginasVisibles", prestamos.getTotalPages() == 0
                ? List.of()
                : IntStream.rangeClosed(inicio, fin).boxed().toList());
    }

    private void agregarFormulariosSiFaltan(Model model) {
        if (!model.containsAttribute("nuevoPrestamo")) {
            model.addAttribute("nuevoPrestamo", new PrestamoForm());
        }
        if (!model.containsAttribute("devolucionPrestamo")) {
            model.addAttribute("devolucionPrestamo", new DevolucionPrestamoForm());
        }
        if (!model.containsAttribute("notificacionPrestamo")) {
            model.addAttribute("notificacionPrestamo", new NotificacionPrestamoForm());
        }
    }

    private String identidad(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new IllegalStateException("La operación requiere un usuario autenticado.");
        }
        return principal.getName();
    }

    private String redireccionListado(Filtros filtros) {
        StringBuilder redirect = new StringBuilder("redirect:/admin/prestamos?page=")
                .append(Math.max(filtros.pageFiltro(), 0))
                .append("&estado=")
                .append(valorFiltro(filtros.estadoFiltro()))
                .append("&fecha=")
                .append(valorFiltro(filtros.fechaFiltro()));
        if (filtros.buscarFiltro() != null && !filtros.buscarFiltro().isBlank()) {
            redirect.append("&buscar=")
                    .append(URLEncoder.encode(filtros.buscarFiltro(), StandardCharsets.UTF_8));
        }
        return redirect.toString();
    }

    private String valorFiltro(String valor) {
        return valor == null || valor.isBlank() ? "todos" : valor;
    }

    public record Filtros(
            String buscarFiltro,
            String estadoFiltro,
            String fechaFiltro,
            int pageFiltro
    ) {
    }
}
