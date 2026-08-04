package com.upgrade.app.controller;

import com.upgrade.app.domain.Cliente;
import com.upgrade.app.domain.TipoCliente;
import com.upgrade.app.dto.ClienteForm;
import com.upgrade.app.service.ClienteService;
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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private static final int TAMANO_PAGINA = 10;

    private final ClienteService clienteService;

    @ModelAttribute("tiposCliente")
    public TipoCliente[] tiposCliente() {
        return TipoCliente.values();
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false, defaultValue = "todos") String estado,
            @RequestParam(required = false, defaultValue = "0") int page,
            Model model
    ) {
        cargarListado(model, buscar, estado, page);
        agregarFormulariosSiFaltan(model);
        return "admin/clientes";
    }

    @PostMapping
    public String crear(
            @Valid @ModelAttribute("nuevoCliente") ClienteForm form,
            BindingResult bindingResult,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false, defaultValue = "todos") String estado,
            @RequestParam(required = false, defaultValue = "0") int page,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        validarReglasNegocio(form, bindingResult);
        validarUnicos(form, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("editarCliente", new ClienteForm());
            model.addAttribute("modalAbierto", "nuevo");
            cargarListado(model, buscar, estado, page);
            return "admin/clientes";
        }

        Cliente cliente = clienteService.crear(form);
        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "El cliente “" + cliente.getNombreMostrado() + "” fue creado correctamente."
        );
        return "redirect:/admin/clientes";
    }

    @PostMapping("/editar")
    public String editar(
            @Valid @ModelAttribute("editarCliente") ClienteForm form,
            BindingResult bindingResult,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false, defaultValue = "todos") String estado,
            @RequestParam(required = false, defaultValue = "0") int page,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (form.getId() == null) {
            bindingResult.reject("cliente.id.requerido", "No fue posible identificar el cliente.");
        } else {
            validarReglasNegocio(form, bindingResult);
            validarUnicos(form, bindingResult);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("nuevoCliente", new ClienteForm());
            model.addAttribute("modalAbierto", "editar");
            cargarListado(model, buscar, estado, page);
            return "admin/clientes";
        }

        Cliente cliente = clienteService.actualizar(form);
        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "Los datos de “" + cliente.getNombreMostrado() + "” fueron actualizados."
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
        Cliente cliente = clienteService.obtenerPorId(id);
        clienteService.eliminarLogicamente(id);
        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "El cliente “" + cliente.getNombreMostrado() + "” fue desactivado correctamente."
        );
        return redireccionListado(buscar, estado, page);
    }

    @GetMapping("/exportar")
    public void exportarCsv(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false, defaultValue = "todos") String estado,
            HttpServletResponse response
    ) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=clientes-upgrade.csv");

        try (PrintWriter writer = response.getWriter()) {
            writer.write('\ufeff');
            writer.println("ID,Tipo,Identificación,Nombre,Empresa,Correo,Teléfono,Dirección,Estado,Fecha de registro");
            for (Cliente cliente : clienteService.listarParaExportar(buscar, convertirEstado(estado))) {
                writer.println(String.join(",",
                        csv(cliente.getId()),
                        csv(cliente.getTipoCliente()),
                        csv(cliente.getIdentificacion()),
                        csv(cliente.getNombre()),
                        csv(cliente.getEmpresa()),
                        csv(cliente.getCorreo()),
                        csv(cliente.getTelefono()),
                        csv(cliente.getDireccion()),
                        csv(Boolean.TRUE.equals(cliente.getActivo()) ? "Activo" : "Inactivo"),
                        csv(cliente.getFechaRegistro())
                ));
            }
        }
    }

    private void cargarListado(Model model, String buscar, String estado, int page) {
        String estadoNormalizado = normalizarEstado(estado);
        Page<Cliente> clientes = clienteService.listar(
                buscar,
                convertirEstado(estadoNormalizado),
                page,
                TAMANO_PAGINA
        );
        model.addAttribute("clientes", clientes);
        model.addAttribute("buscar", buscar == null ? "" : buscar.trim());
        model.addAttribute("estado", estadoNormalizado);

        int inicio = Math.max(0, clientes.getNumber() - 2);
        int fin = Math.min(Math.max(clientes.getTotalPages() - 1, 0), clientes.getNumber() + 2);
        List<Integer> paginasVisibles = clientes.getTotalPages() == 0
                ? List.of()
                : IntStream.rangeClosed(inicio, fin).boxed().toList();
        model.addAttribute("paginasVisibles", paginasVisibles);
    }

    private void agregarFormulariosSiFaltan(Model model) {
        if (!model.containsAttribute("nuevoCliente")) {
            model.addAttribute("nuevoCliente", new ClienteForm());
        }
        if (!model.containsAttribute("editarCliente")) {
            model.addAttribute("editarCliente", new ClienteForm());
        }
    }

    private void validarUnicos(ClienteForm form, BindingResult bindingResult) {
        if (form.getCorreo() != null
                && !bindingResult.hasFieldErrors("correo")
                && clienteService.correoOcupado(form.getCorreo(), form.getId())) {
            bindingResult.rejectValue("correo", "cliente.correo.duplicado", "Ya existe un cliente con este correo.");
        }
        if (form.getIdentificacion() != null
                && !bindingResult.hasFieldErrors("identificacion")
                && clienteService.identificacionOcupada(form.getIdentificacion(), form.getId())) {
            bindingResult.rejectValue(
                    "identificacion",
                    "cliente.identificacion.duplicada",
                    "Ya existe un cliente con esta identificación."
            );
        }
    }

    private void validarReglasNegocio(ClienteForm form, BindingResult bindingResult) {
        if (form.getTipoCliente() == TipoCliente.EMPRESA
                && (form.getEmpresa() == null || form.getEmpresa().isBlank())) {
            bindingResult.rejectValue(
                    "empresa",
                    "cliente.empresa.requerida",
                    "La empresa o razón social es obligatoria para un cliente empresarial."
            );
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
        StringBuilder redirect = new StringBuilder("redirect:/admin/clientes?page=")
                .append(Math.max(page, 0))
                .append("&estado=")
                .append(normalizarEstado(estado));
        if (buscar != null && !buscar.isBlank()) {
            redirect.append("&buscar=").append(java.net.URLEncoder.encode(buscar, StandardCharsets.UTF_8));
        }
        return redirect.toString();
    }

    private String csv(Object valor) {
        String texto = valor == null ? "" : String.valueOf(valor);
        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }
}
