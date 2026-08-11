package com.upgrade.app.service;

import com.upgrade.app.domain.CargoColaborador;
import com.upgrade.app.domain.DepartamentoColaborador;
import com.upgrade.app.domain.EstadoColaborador;
import com.upgrade.app.domain.Rol;
import com.upgrade.app.domain.Usuario;
import com.upgrade.app.dto.EditarColaboradorForm;
import com.upgrade.app.dto.NuevoColaboradorForm;
import com.upgrade.app.exception.ColaboradorNoEncontradoException;
import com.upgrade.app.repository.CargoColaboradorRepository;
import com.upgrade.app.repository.DepartamentoColaboradorRepository;
import com.upgrade.app.repository.RolRepository;
import com.upgrade.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ColaboradorService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final CargoColaboradorRepository cargoRepository;
    private final DepartamentoColaboradorRepository departamentoRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<Usuario> listar(String buscar, EstadoColaborador estado, Long rolId, int pagina, int tamano) {
        return usuarioRepository.buscarColaboradores(
                limpiarOpcional(buscar),
                estado,
                rolId,
                PageRequest.of(
                        Math.max(pagina, 0),
                        Math.min(Math.max(tamano, 6), 50),
                        Sort.by(Sort.Direction.ASC, "nombre").and(Sort.by(Sort.Direction.ASC, "apellido"))
                )
        );
    }

    public List<Usuario> listarParaExportar(String buscar, EstadoColaborador estado, Long rolId) {
        return usuarioRepository.buscarColaboradores(
                limpiarOpcional(buscar),
                estado,
                rolId,
                PageRequest.of(0, 10_000, Sort.by("nombre", "apellido"))
        ).getContent();
    }

    public List<Rol> listarRolesActivos() {
        return rolRepository.findAllByActivoTrueOrderByNombreAsc();
    }

    public List<CargoColaborador> listarCargosActivos() {
        return cargoRepository.findAllByActivoTrueOrderByNombreAsc();
    }

    public List<DepartamentoColaborador> listarDepartamentosActivos() {
        return departamentoRepository.findAllByActivoTrueOrderByNombreAsc();
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ColaboradorNoEncontradoException(id));
    }

    public boolean usernameOcupado(String username, Long idActual) {
        String normalizado = normalizarUsername(username);
        return idActual == null
                ? usuarioRepository.existsByUsernameIgnoreCase(normalizado)
                : usuarioRepository.existsByUsernameIgnoreCaseAndIdNot(normalizado, idActual);
    }

    public boolean correoOcupado(String correo, Long idActual) {
        String normalizado = normalizarCorreo(correo);
        return idActual == null
                ? usuarioRepository.existsByEmailIgnoreCase(normalizado)
                : usuarioRepository.existsByEmailIgnoreCaseAndIdNot(normalizado, idActual);
    }

    @Transactional
    public Usuario crear(NuevoColaboradorForm form) {
        Rol rol = obtenerRol(form.getRolId());
        CargoColaborador cargo = obtenerCargo(form.getCargoId());
        DepartamentoColaborador departamento = obtenerDepartamento(form.getDepartamentoId());
        Usuario usuario = new Usuario();
        usuario.setNombre(limpiar(form.getNombre()));
        usuario.setApellido(limpiar(form.getApellido()));
        usuario.setUsername(normalizarUsername(form.getUsername()));
        usuario.setEmail(normalizarCorreo(form.getEmail()));
        usuario.setTelefono(limpiarOpcional(form.getTelefono()));
        usuario.setPassword(passwordEncoder.encode(form.getPassword()));
        usuario.setCargo(cargo);
        usuario.setDepartamento(departamento);
        usuario.setEstadoColaborador(form.getEstadoColaborador());
        usuario.setFechaIngreso(LocalDate.now());
        usuario.setEventosAsignados(0);
        usuario.setActivo(true);
        usuario.getRoles().add(rol);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario actualizar(EditarColaboradorForm form) {
        Usuario usuario = obtenerPorId(form.getId());
        Rol rol = obtenerRol(form.getRolId());
        CargoColaborador cargo = obtenerCargo(form.getCargoId());
        DepartamentoColaborador departamento = obtenerDepartamento(form.getDepartamentoId());
        usuario.setNombre(limpiar(form.getNombre()));
        usuario.setApellido(limpiar(form.getApellido()));
        usuario.setUsername(normalizarUsername(form.getUsername()));
        usuario.setEmail(normalizarCorreo(form.getEmail()));
        usuario.setTelefono(limpiarOpcional(form.getTelefono()));
        usuario.setCargo(cargo);
        usuario.setDepartamento(departamento);
        usuario.setEstadoColaborador(form.getEstadoColaborador());
        usuario.getRoles().clear();
        usuario.getRoles().add(rol);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario asignarRol(Long colaboradorId, Long rolId) {
        Usuario usuario = obtenerPorId(colaboradorId);
        Rol rol = obtenerRol(rolId);
        usuario.getRoles().clear();
        usuario.getRoles().add(rol);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminarLogicamente(Long id, String usernameActual) {
        Usuario usuario = obtenerPorId(id);
        if (usuario.getUsername().equalsIgnoreCase(limpiar(usernameActual))) {
            throw new IllegalArgumentException("No puedes desactivar la cuenta con la que tienes la sesión iniciada.");
        }
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    private Rol obtenerRol(Long id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El rol seleccionado no existe."));
        if (!Boolean.TRUE.equals(rol.getActivo())) {
            throw new IllegalArgumentException("El rol seleccionado está inactivo.");
        }
        return rol;
    }

    private CargoColaborador obtenerCargo(Long id) {
        CargoColaborador cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El cargo seleccionado no existe."));
        if (!Boolean.TRUE.equals(cargo.getActivo())) {
            throw new IllegalArgumentException("El cargo seleccionado está inactivo.");
        }
        return cargo;
    }

    private DepartamentoColaborador obtenerDepartamento(Long id) {
        DepartamentoColaborador departamento = departamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El departamento seleccionado no existe."));
        if (!Boolean.TRUE.equals(departamento.getActivo())) {
            throw new IllegalArgumentException("El departamento seleccionado está inactivo.");
        }
        return departamento;
    }

    private String normalizarUsername(String valor) {
        return limpiar(valor).toLowerCase(Locale.ROOT);
    }

    private String normalizarCorreo(String valor) {
        return limpiar(valor).toLowerCase(Locale.ROOT);
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String limpiarOpcional(String valor) {
        String limpio = limpiar(valor);
        return limpio.isEmpty() ? null : limpio;
    }
}
