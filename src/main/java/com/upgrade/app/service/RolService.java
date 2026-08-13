package com.upgrade.app.service;

import com.upgrade.app.domain.Modulo;
import com.upgrade.app.domain.Rol;
import com.upgrade.app.dto.AccesosRolForm;
import com.upgrade.app.dto.EditarRolForm;
import com.upgrade.app.dto.EliminarRolForm;
import com.upgrade.app.dto.NuevoRolForm;
import com.upgrade.app.exception.RolNoEncontradoException;
import com.upgrade.app.repository.ModuloRepository;
import com.upgrade.app.repository.RolRepository;
import com.upgrade.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RolService {

    public record RolResumen(Rol rol, long usuarios) {}
    public record Estadisticas(long roles, long usuariosAsignados, long modulos, long accesos) {}

    private final RolRepository rolRepository;
    private final ModuloRepository moduloRepository;
    private final UsuarioRepository usuarioRepository;
    private final SesionUsuarioService sesionUsuarioService;

    public List<RolResumen> listarResumenes() {
        return rolRepository.findAllByActivoTrueOrderByNombreAsc().stream()
                .map(rol -> new RolResumen(rol, usuarioRepository.countDistinctByActivoTrueAndRolesId(rol.getId())))
                .toList();
    }

    public List<Modulo> listarModulos() {
        return moduloRepository.findAllByActivoTrueOrderByOrdenAsc();
    }

    public Rol obtener(Long id) {
        return rolRepository.findWithModulosById(id)
                .filter(rol -> Boolean.TRUE.equals(rol.getActivo()))
                .orElseThrow(() -> new RolNoEncontradoException(id));
    }

    public Estadisticas estadisticas() {
        List<RolResumen> roles = listarResumenes();
        long usuarios = roles.stream().mapToLong(RolResumen::usuarios).sum();
        long accesos = roles.stream().mapToLong(resumen -> resumen.rol().getModulos().size()).sum();
        return new Estadisticas(roles.size(), usuarios, moduloRepository.countByActivoTrue(), accesos);
    }

    public boolean identificadorOcupado(String identificador, Long idActual) {
        String normalizado = normalizarIdentificador(identificador);
        return idActual == null
                ? rolRepository.existsByNombreIgnoreCase(normalizado)
                : rolRepository.existsByNombreIgnoreCaseAndIdNot(normalizado, idActual);
    }

    public boolean nombreOcupado(String nombre, Long idActual) {
        String limpio = limpiar(nombre);
        return idActual == null
                ? rolRepository.existsByNombreMostradoIgnoreCase(limpio)
                : rolRepository.existsByNombreMostradoIgnoreCaseAndIdNot(limpio, idActual);
    }

    @Transactional
    public Rol crear(NuevoRolForm form) {
        String identificador = normalizarIdentificador(form.getIdentificador());
        if ("ADMIN".equals(identificador)) throw new IllegalArgumentException("El identificador ADMIN está reservado.");
        if (identificadorOcupado(identificador, null)) throw new IllegalArgumentException("El identificador ya está en uso.");
        if (nombreOcupado(form.getNombre(), null)) throw new IllegalArgumentException("Ya existe un rol con ese nombre.");

        Rol rol = new Rol();
        rol.setNombre(identificador);
        rol.setNombreMostrado(limpiar(form.getNombre()));
        rol.setDescripcion(limpiar(form.getDescripcion()));
        rol.setActivo(true);
        rol.setProtegido(false);
        if (form.getPlantillaId() != null) {
            rol.setModulos(new LinkedHashSet<>(obtener(form.getPlantillaId()).getModulos()));
        }
        return rolRepository.save(rol);
    }

    @Transactional
    public Rol editar(EditarRolForm form) {
        Rol rol = obtener(form.getId());
        exigirEditable(rol);
        if (nombreOcupado(form.getNombre(), rol.getId())) throw new IllegalArgumentException("Ya existe un rol con ese nombre.");
        rol.setNombreMostrado(limpiar(form.getNombre()));
        rol.setDescripcion(limpiar(form.getDescripcion()));
        return rolRepository.save(rol);
    }

    @Transactional
    public Rol guardarAccesos(AccesosRolForm form) {
        Rol rol = obtener(form.getRolId());
        exigirEditable(rol);
        var ids = form.getModuloIds() == null ? new LinkedHashSet<Long>() : new LinkedHashSet<>(form.getModuloIds());
        List<Modulo> modulos = ids.isEmpty() ? List.of() : moduloRepository.findAllByIdInAndActivoTrue(ids);
        if (modulos.size() != ids.size()) throw new IllegalArgumentException("Uno de los módulos seleccionados no es válido.");
        rol.setModulos(new LinkedHashSet<>(modulos));
        Rol guardado = rolRepository.save(rol);
        sesionUsuarioService.expirarUsuarios(usuarioRepository.findUsernamesActivosByRolId(rol.getId()));
        return guardado;
    }

    @Transactional
    public String eliminar(EliminarRolForm form) {
        Rol rol = obtener(form.getId());
        exigirEditable(rol);
        if (!"ELIMINAR".equalsIgnoreCase(limpiar(form.getConfirmacion()))) {
            throw new IllegalArgumentException("Escribe ELIMINAR para confirmar la acción.");
        }
        long usuarios = usuarioRepository.countDistinctByActivoTrueAndRolesId(rol.getId());
        if (usuarios > 0) {
            throw new IllegalStateException("No se puede eliminar el rol porque tiene " + usuarios + " usuario(s) asignado(s). Reasígnalos primero.");
        }
        String nombre = rol.getNombreMostrado();
        rol.getModulos().clear();
        rol.setActivo(false);
        rolRepository.save(rol);
        return nombre;
    }

    public NuevoRolForm nuevoFormulario() { return new NuevoRolForm(); }

    public EditarRolForm formularioEditar(Rol rol) {
        EditarRolForm form = new EditarRolForm();
        form.setId(rol.getId()); form.setNombre(rol.getNombreMostrado()); form.setDescripcion(rol.getDescripcion());
        return form;
    }

    public AccesosRolForm formularioAccesos(Rol rol) {
        AccesosRolForm form = new AccesosRolForm();
        form.setRolId(rol.getId());
        form.setModuloIds(rol.getModulos().stream().map(Modulo::getId).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)));
        return form;
    }

    public EliminarRolForm formularioEliminar(Rol rol) {
        EliminarRolForm form = new EliminarRolForm(); form.setId(rol.getId()); return form;
    }

    private void exigirEditable(Rol rol) {
        if (Boolean.TRUE.equals(rol.getProtegido()) || "ADMIN".equalsIgnoreCase(rol.getNombre())) {
            throw new IllegalStateException("El rol Administrador está protegido y no puede modificarse ni eliminarse.");
        }
    }

    private String normalizarIdentificador(String valor) {
        return limpiar(valor).toUpperCase(Locale.ROOT);
    }

    private String limpiar(String valor) { return valor == null ? "" : valor.trim(); }
}
