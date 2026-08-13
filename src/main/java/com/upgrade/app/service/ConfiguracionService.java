package com.upgrade.app.service;

import com.upgrade.app.domain.*;
import com.upgrade.app.dto.*;
import com.upgrade.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConfiguracionService {
    private final UsuarioRepository usuarioRepository;
    private final ConfiguracionUsuarioRepository configuracionRepository;
    private final CargoColaboradorRepository cargoRepository;
    private final PasswordEncoder passwordEncoder;
    private final PerfilStorageService perfilStorageService;

    public record Cuenta(Usuario usuario, ConfiguracionUsuario configuracion) {}

    @Transactional
    public Cuenta obtenerCuenta(String login) {
        Usuario usuario = obtenerUsuario(login);
        ConfiguracionUsuario configuracion = configuracionRepository.findByUsuarioId(usuario.getId())
                .orElseGet(() -> crearConfiguracion(usuario));
        normalizarValoresHeredados(configuracion);
        return new Cuenta(usuario, configuracion);
    }

    public PerfilUsuarioForm crearPerfilForm(Cuenta cuenta) {
        PerfilUsuarioForm form = new PerfilUsuarioForm();
        form.setNombre(cuenta.usuario().getNombre());
        form.setApellido(cuenta.usuario().getApellido());
        form.setEmail(cuenta.usuario().getEmail());
        form.setTelefono(cuenta.usuario().getTelefono());
        form.setCargoId(cuenta.usuario().getCargo() == null ? null : cuenta.usuario().getCargo().getId());
        form.setIdioma(cuenta.configuracion().getIdioma());
        form.setZonaHoraria(cuenta.configuracion().getZonaHoraria());
        form.setBiografia(cuenta.configuracion().getBiografia());
        return form;
    }

    public AparienciaForm crearAparienciaForm(Cuenta cuenta) {
        AparienciaForm form = new AparienciaForm();
        form.setTema(cuenta.configuracion().getTema());
        form.setDensidad(cuenta.configuracion().getDensidad());
        return form;
    }

    public boolean correoOcupado(String email, String login) {
        Usuario actual = obtenerUsuario(login);
        return usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email.trim(), actual.getId());
    }

    @Transactional
    public Cuenta actualizarPerfil(String login, PerfilUsuarioForm form) {
        Cuenta cuenta = obtenerCuenta(login);
        CargoColaborador cargo = cargoRepository.findById(form.getCargoId())
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .orElseThrow(() -> new IllegalArgumentException("El cargo seleccionado no está disponible."));
        Usuario usuario = cuenta.usuario();
        ConfiguracionUsuario config = cuenta.configuracion();
        String fotoAnterior = config.getFotoPerfil();
        String fotoNueva = perfilStorageService.guardar(form.getFoto());

        usuario.setNombre(limpiar(form.getNombre()));
        usuario.setApellido(limpiar(form.getApellido()));
        usuario.setEmail(limpiar(form.getEmail()).toLowerCase(Locale.ROOT));
        usuario.setTelefono(vacioANull(form.getTelefono()));
        usuario.setCargo(cargo);
        config.setIdioma(form.getIdioma());
        config.setZonaHoraria(form.getZonaHoraria());
        config.setBiografia(vacioANull(form.getBiografia()));
        if (form.isEliminarFoto()) config.setFotoPerfil(null);
        if (fotoNueva != null) config.setFotoPerfil(fotoNueva);
        usuarioRepository.save(usuario);
        configuracionRepository.save(config);
        if ((form.isEliminarFoto() || fotoNueva != null) && fotoAnterior != null) perfilStorageService.eliminar(fotoAnterior);
        return cuenta;
    }

    @Transactional
    public void cambiarPassword(String login, CambioPasswordForm form) {
        Cuenta cuenta = obtenerCuenta(login);
        if (!passwordEncoder.matches(form.getPasswordActual(), cuenta.usuario().getPassword()))
            throw new IllegalArgumentException("La contraseña actual no es correcta.");
        if (!form.getPasswordNueva().equals(form.getConfirmarPassword()))
            throw new IllegalArgumentException("La confirmación no coincide con la nueva contraseña.");
        String nueva = form.getPasswordNueva();
        if (!nueva.matches(".*[A-ZÁÉÍÓÚÑ].*") || !nueva.matches(".*\\d.*") || !nueva.matches(".*[^A-Za-z0-9].*"))
            throw new IllegalArgumentException("La nueva contraseña debe incluir una mayúscula, un número y un símbolo.");
        if (passwordEncoder.matches(nueva, cuenta.usuario().getPassword()))
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente de la actual.");
        cuenta.usuario().setPassword(passwordEncoder.encode(nueva));
        cuenta.configuracion().setFechaCambioPassword(LocalDateTime.now());
        usuarioRepository.save(cuenta.usuario());
        configuracionRepository.save(cuenta.configuracion());
    }

    @Transactional
    public void actualizarApariencia(String login, AparienciaForm form) {
        Cuenta cuenta = obtenerCuenta(login);
        cuenta.configuracion().setTema(form.getTema());
        cuenta.configuracion().setDensidad(form.getDensidad());
        configuracionRepository.save(cuenta.configuracion());
    }

    private Usuario obtenerUsuario(String login) {
        return usuarioRepository.findByUsernameOrEmail(login, login)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la cuenta autenticada."));
    }

    private ConfiguracionUsuario crearConfiguracion(Usuario usuario) {
        ConfiguracionUsuario config = new ConfiguracionUsuario();
        config.setUsuario(usuario);
        return configuracionRepository.save(config);
    }

    private void normalizarValoresHeredados(ConfiguracionUsuario config) {
        if ("claro".equalsIgnoreCase(config.getTema())) config.setTema("light");
        if ("oscuro".equalsIgnoreCase(config.getTema())) config.setTema("dark");
        if (config.getTema() == null || !config.getTema().matches("light|dark|system")) config.setTema("system");
        if (config.getIdioma() == null || !config.getIdioma().matches("es|en")) config.setIdioma("es");
        if (config.getDensidad() == null) config.setDensidad("comfortable");
    }

    private String limpiar(String valor) { return valor == null ? "" : valor.trim(); }
    private String vacioANull(String valor) { String limpio = limpiar(valor); return limpio.isEmpty() ? null : limpio; }
}
