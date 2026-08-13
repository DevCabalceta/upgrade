package com.upgrade.app.controller;

import com.upgrade.app.domain.CustomUserDetails;
import com.upgrade.app.dto.*;
import com.upgrade.app.exception.ArchivoPerfilException;
import com.upgrade.app.repository.CargoColaboradorRepository;
import com.upgrade.app.service.ConfiguracionService;
import com.upgrade.app.service.SesionUsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/admin/configuracion")
@RequiredArgsConstructor
public class ConfiguracionController {
    private final ConfiguracionService configuracionService;
    private final CargoColaboradorRepository cargoRepository;
    private final SesionUsuarioService sesionService;

    @GetMapping
    public String ver(@RequestParam(defaultValue = "perfil") String tab, Principal principal,
                      HttpServletRequest request, Model model) {
        cargarModelo(model, principal.getName(), tabValido(tab), request);
        return "admin/configuracion";
    }

    @PostMapping(value = "/perfil", consumes = "multipart/form-data")
    public String actualizarPerfil(@Valid @ModelAttribute("perfilForm") PerfilUsuarioForm form,
                                   BindingResult result, Authentication authentication,
                                   HttpServletRequest request, Model model,
                                   RedirectAttributes redirect) {
        String login = authentication.getName();
        if (!result.hasFieldErrors("email") && configuracionService.correoOcupado(form.getEmail(), login))
            result.rejectValue("email", "configuracion.email.duplicado", "Ese correo ya está registrado.");
        if (result.hasErrors()) {
            model.addAttribute("modalAbierto", "perfil");
            cargarModelo(model, login, "perfil", request);
            return "admin/configuracion";
        }
        try {
            var cuenta = configuracionService.actualizarPerfil(login, form);
            if (authentication.getPrincipal() instanceof CustomUserDetails usuario) {
                usuario.actualizarPerfil(cuenta.usuario().getNombre(), cuenta.usuario().getApellido(), cuenta.usuario().getRolPrincipalNombre());
            }
            redirect.addFlashAttribute("mensajeExito", "Tu perfil fue actualizado correctamente.");
        } catch (IllegalArgumentException | ArchivoPerfilException ex) {
            result.reject("configuracion.perfil", ex.getMessage());
            model.addAttribute("modalAbierto", "perfil");
            cargarModelo(model, login, "perfil", request);
            return "admin/configuracion";
        }
        return "redirect:/admin/configuracion?tab=perfil";
    }

    @PostMapping("/password")
    public String cambiarPassword(@Valid @ModelAttribute("passwordForm") CambioPasswordForm form,
                                  BindingResult result, Principal principal,
                                  HttpServletRequest request, Model model,
                                  RedirectAttributes redirect) {
        if (!result.hasFieldErrors("confirmarPassword") && form.getPasswordNueva() != null
                && !form.getPasswordNueva().equals(form.getConfirmarPassword()))
            result.rejectValue("confirmarPassword", "configuracion.password.confirmacion", "Las contraseñas no coinciden.");
        if (!result.hasErrors()) {
            try { configuracionService.cambiarPassword(principal.getName(), form); }
            catch (IllegalArgumentException ex) { result.reject("configuracion.password", ex.getMessage()); }
        }
        if (result.hasErrors()) {
            model.addAttribute("modalAbierto", "password");
            cargarModelo(model, principal.getName(), "seguridad", request);
            return "admin/configuracion";
        }
        redirect.addFlashAttribute("mensajeExito", "La contraseña fue actualizada. Tus otras sesiones permanecen disponibles para que puedas revisarlas.");
        return "redirect:/admin/configuracion?tab=seguridad";
    }

    @PostMapping("/apariencia")
    public String guardarApariencia(@Valid @ModelAttribute("aparienciaForm") AparienciaForm form,
                                    BindingResult result, Principal principal,
                                    HttpServletRequest request, Model model,
                                    RedirectAttributes redirect) {
        if (result.hasErrors()) {
            cargarModelo(model, principal.getName(), "apariencia", request);
            return "admin/configuracion";
        }
        configuracionService.actualizarApariencia(principal.getName(), form);
        redirect.addFlashAttribute("mensajeExito", "Las preferencias de apariencia fueron guardadas.");
        return "redirect:/admin/configuracion?tab=apariencia";
    }

    @PostMapping("/sesiones/cerrar")
    public String cerrarSesion(@RequestParam String sessionId, Principal principal,
                               HttpServletRequest request, RedirectAttributes redirect) {
        HttpSession actual = request.getSession(false);
        boolean cerrada = sesionService.cerrar(principal.getName(), sessionId, actual == null ? "" : actual.getId());
        redirect.addFlashAttribute(cerrada ? "mensajeExito" : "mensajeError",
                cerrada ? "La sesión seleccionada fue cerrada." : "No se pudo cerrar esa sesión.");
        return "redirect:/admin/configuracion?tab=sesiones";
    }

    @PostMapping("/sesiones/cerrar-otras")
    public String cerrarOtras(Principal principal, HttpServletRequest request, RedirectAttributes redirect) {
        HttpSession actual = request.getSession(false);
        int total = sesionService.cerrarOtras(principal.getName(), actual == null ? "" : actual.getId());
        redirect.addFlashAttribute("mensajeExito", total == 0
                ? "No había otras sesiones abiertas."
                : "Se cerraron " + total + " sesiones en otros dispositivos.");
        return "redirect:/admin/configuracion?tab=sesiones";
    }

    private void cargarModelo(Model model, String login, String tab, HttpServletRequest request) {
        var cuenta = configuracionService.obtenerCuenta(login);
        model.addAttribute("cuenta", cuenta);
        model.addAttribute("tabActivo", tab);
        model.addAttribute("cargos", cargoRepository.findAllByActivoTrueOrderByNombreAsc());
        model.addAttribute("sesiones", sesionService.listar(login, request));
        if (!model.containsAttribute("perfilForm")) model.addAttribute("perfilForm", configuracionService.crearPerfilForm(cuenta));
        if (!model.containsAttribute("passwordForm")) model.addAttribute("passwordForm", new CambioPasswordForm());
        if (!model.containsAttribute("aparienciaForm")) model.addAttribute("aparienciaForm", configuracionService.crearAparienciaForm(cuenta));
    }

    private String tabValido(String tab) {
        return switch (tab) { case "seguridad", "apariencia", "sesiones" -> tab; default -> "perfil"; };
    }
}
