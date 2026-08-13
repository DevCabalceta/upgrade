package com.upgrade.app.controller;

import com.upgrade.app.domain.Rol;
import com.upgrade.app.dto.AccesosRolForm;
import com.upgrade.app.dto.EditarRolForm;
import com.upgrade.app.dto.EliminarRolForm;
import com.upgrade.app.dto.NuevoRolForm;
import com.upgrade.app.service.RolService;
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

@Controller
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    @GetMapping
    public String listar(@RequestParam(required = false) Long rol,
                         @RequestParam(required = false) String modal,
                         Model model) {
        cargarPagina(model, rol, modal);
        return "admin/roles";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("nuevoRol") NuevoRolForm form,
                        BindingResult bindingResult, Model model,
                        RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasFieldErrors("identificador")
                && rolService.identificadorOcupado(form.getIdentificador(), null)) {
            bindingResult.rejectValue("identificador", "rol.identificador.duplicado", "Este identificador ya está en uso.");
        }
        if (!bindingResult.hasFieldErrors("nombre") && rolService.nombreOcupado(form.getNombre(), null)) {
            bindingResult.rejectValue("nombre", "rol.nombre.duplicado", "Ya existe un rol con este nombre.");
        }
        if (bindingResult.hasErrors()) {
            cargarPagina(model, null, "nuevo");
            return "admin/roles";
        }
        try {
            Rol creado = rolService.crear(form);
            redirectAttributes.addFlashAttribute("mensajeExito", "El rol “" + creado.getNombreMostrado() + "” fue creado.");
            return "redirect:/admin/roles?rol=" + creado.getId();
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("rol.crear", ex.getMessage());
            cargarPagina(model, null, "nuevo");
            return "admin/roles";
        }
    }

    @PostMapping("/editar")
    public String editar(@Valid @ModelAttribute("editarRol") EditarRolForm form,
                         BindingResult bindingResult, Model model,
                         RedirectAttributes redirectAttributes) {
        if (form.getId() != null && !bindingResult.hasFieldErrors("nombre")
                && rolService.nombreOcupado(form.getNombre(), form.getId())) {
            bindingResult.rejectValue("nombre", "rol.nombre.duplicado", "Ya existe un rol con este nombre.");
        }
        if (bindingResult.hasErrors()) {
            cargarPagina(model, form.getId(), "editar");
            return "admin/roles";
        }
        try {
            Rol actualizado = rolService.editar(form);
            redirectAttributes.addFlashAttribute("mensajeExito", "Los datos del rol fueron actualizados.");
            return "redirect:/admin/roles?rol=" + actualizado.getId();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            bindingResult.reject("rol.editar", ex.getMessage());
            cargarPagina(model, form.getId(), "editar");
            return "admin/roles";
        }
    }

    @PostMapping("/accesos")
    public String guardarAccesos(@Valid @ModelAttribute("accesosRol") AccesosRolForm form,
                                 BindingResult bindingResult, Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            cargarPagina(model, form.getRolId(), "accesos");
            return "admin/roles";
        }
        try {
            Rol rol = rolService.guardarAccesos(form);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Accesos de “" + rol.getNombreMostrado() + "” actualizados. Sus usuarios deberán iniciar sesión nuevamente.");
            return "redirect:/admin/roles?rol=" + rol.getId();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            bindingResult.reject("rol.accesos", ex.getMessage());
            cargarPagina(model, form.getRolId(), "accesos");
            return "admin/roles";
        }
    }

    @PostMapping("/eliminar")
    public String eliminar(@Valid @ModelAttribute("eliminarRol") EliminarRolForm form,
                           BindingResult bindingResult, Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            cargarPagina(model, form.getId(), "eliminar");
            return "admin/roles";
        }
        try {
            String nombre = rolService.eliminar(form);
            redirectAttributes.addFlashAttribute("mensajeExito", "El rol “" + nombre + "” fue eliminado.");
            return "redirect:/admin/roles";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            bindingResult.reject("rol.eliminar", ex.getMessage());
            cargarPagina(model, form.getId(), "eliminar");
            return "admin/roles";
        }
    }

    private void cargarPagina(Model model, Long rolId, String modal) {
        var resumenes = rolService.listarResumenes();
        Rol seleccionado = null;
        if (!resumenes.isEmpty()) {
            Long id = rolId != null ? rolId : resumenes.getFirst().rol().getId();
            seleccionado = resumenes.stream().map(RolService.RolResumen::rol)
                    .filter(item -> item.getId().equals(id)).findFirst().orElse(resumenes.getFirst().rol());
        }
        model.addAttribute("rolesResumen", resumenes);
        model.addAttribute("modulos", rolService.listarModulos());
        model.addAttribute("estadisticas", rolService.estadisticas());
        model.addAttribute("rolSeleccionado", seleccionado);
        Long seleccionadoId = seleccionado == null ? null : seleccionado.getId();
        model.addAttribute("usuariosRolSeleccionado", seleccionado == null ? 0L : resumenes.stream()
                .filter(item -> item.rol().getId().equals(seleccionadoId))
                .mapToLong(RolService.RolResumen::usuarios).findFirst().orElse(0L));
        model.addAttribute("modalAbierto", modal == null ? "" : modal);

        if (!model.containsAttribute("nuevoRol")) model.addAttribute("nuevoRol", rolService.nuevoFormulario());
        if (!model.containsAttribute("editarRol")) model.addAttribute("editarRol",
                seleccionado == null ? new EditarRolForm() : rolService.formularioEditar(seleccionado));
        if (!model.containsAttribute("accesosRol")) model.addAttribute("accesosRol",
                seleccionado == null ? new AccesosRolForm() : rolService.formularioAccesos(seleccionado));
        if (!model.containsAttribute("eliminarRol")) model.addAttribute("eliminarRol",
                seleccionado == null ? new EliminarRolForm() : rolService.formularioEliminar(seleccionado));
    }
}
