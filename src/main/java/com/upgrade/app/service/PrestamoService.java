package com.upgrade.app.service;

import com.upgrade.app.domain.Cliente;
import com.upgrade.app.domain.CondicionEquipo;
import com.upgrade.app.domain.EstadoInventario;
import com.upgrade.app.domain.EstadoPrestamo;
import com.upgrade.app.domain.Inventario;
import com.upgrade.app.domain.MovimientoInventario;
import com.upgrade.app.domain.Prestamo;
import com.upgrade.app.domain.TipoMovimientoInventario;
import com.upgrade.app.domain.Usuario;
import com.upgrade.app.dto.DevolucionPrestamoForm;
import com.upgrade.app.dto.PrestamoForm;
import com.upgrade.app.exception.PrestamoNoEncontradoException;
import com.upgrade.app.repository.ClienteRepository;
import com.upgrade.app.repository.InventarioRepository;
import com.upgrade.app.repository.MovimientoInventarioRepository;
import com.upgrade.app.repository.PrestamoRepository;
import com.upgrade.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrestamoService {

    private static final Set<EstadoInventario> ESTADOS_PRESTABLES = Set.of(
            EstadoInventario.DISPONIBLE,
            EstadoInventario.EN_BODEGA,
            EstadoInventario.EN_PRESTAMO
    );

    private final PrestamoRepository prestamoRepository;
    private final InventarioRepository inventarioRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    public Page<Prestamo> listar(String buscar, String estado, String fecha, int pagina, int tamano) {
        return prestamoRepository.buscar(
                normalizarOpcional(buscar),
                normalizarFiltroEstado(estado),
                normalizarFiltroFecha(fecha),
                PageRequest.of(
                        Math.max(pagina, 0),
                        Math.min(Math.max(tamano, 5), 50),
                        Sort.by(Sort.Order.desc("fechaSalida"), Sort.Order.desc("id"))
                )
        );
    }

    public Prestamo obtenerPorId(Long id) {
        return prestamoRepository.buscarDetallePorId(id)
                .orElseThrow(() -> new PrestamoNoEncontradoException(id));
    }

    public List<Prestamo> listarVencidos() {
        return prestamoRepository.listarVencidos();
    }

    public List<Cliente> listarClientesActivos() {
        return clienteRepository.findAllByActivoTrueOrderByNombreAsc();
    }

    public List<Usuario> listarResponsablesActivos() {
        return usuarioRepository.findAllByActivoTrueOrderByNombreAscApellidoAsc();
    }

    public List<Inventario> listarEquiposDisponibles() {
        return inventarioRepository.findAllByActivoTrueAndCantidadDisponibleGreaterThanOrderByNombreAsc(0)
                .stream()
                .filter(item -> ESTADOS_PRESTABLES.contains(item.getEstado()))
                .toList();
    }

    public EstadisticasPrestamo obtenerEstadisticas() {
        LocalDate hoy = LocalDate.now();
        return new EstadisticasPrestamo(
                prestamoRepository.contarActivos(EstadoPrestamo.ACTIVO, hoy),
                prestamoRepository.contarVencidos(EstadoPrestamo.ACTIVO, hoy),
                prestamoRepository.contarPorDevolverHoy(EstadoPrestamo.ACTIVO, hoy),
                prestamoRepository.countByEstado(EstadoPrestamo.DEVUELTO)
        );
    }

    @Transactional
    public Prestamo crear(PrestamoForm form, String identidadUsuario) {
        validarFechas(form.getFechaSalida(), form.getFechaDevolucionEstimada());
        Inventario inventario = inventarioRepository.buscarActivoParaActualizar(form.getInventarioId())
                .orElseThrow(() -> new IllegalArgumentException("El equipo seleccionado no existe o está inactivo."));
        if (!ESTADOS_PRESTABLES.contains(inventario.getEstado())) {
            throw new IllegalArgumentException("El equipo no se encuentra en un estado disponible para préstamo.");
        }
        if (form.getCantidad() > inventario.getCantidadDisponible()) {
            throw new IllegalArgumentException(
                    "Solo hay " + inventario.getCantidadDisponible() + " unidades disponibles de este equipo."
            );
        }

        Cliente cliente = clienteRepository.findById(form.getClienteId())
                .filter(item -> Boolean.TRUE.equals(item.getActivo()))
                .orElseThrow(() -> new IllegalArgumentException("El cliente seleccionado no existe o está inactivo."));
        Usuario responsable = usuarioRepository.findById(form.getResponsableId())
                .filter(item -> Boolean.TRUE.equals(item.getActivo()))
                .orElseThrow(() -> new IllegalArgumentException("El responsable seleccionado no está disponible."));

        Prestamo prestamo = new Prestamo();
        prestamo.setCliente(cliente);
        prestamo.setInventario(inventario);
        prestamo.setResponsable(responsable);
        prestamo.setCorreoContacto(correoContacto(form.getCorreoContacto(), cliente));
        prestamo.setCantidad(form.getCantidad());
        prestamo.setFechaSalida(form.getFechaSalida());
        prestamo.setFechaDevolucionEstimada(form.getFechaDevolucionEstimada());
        prestamo.setEstado(EstadoPrestamo.ACTIVO);
        prestamo.setCondicionSalida(form.getCondicionSalida());
        prestamo.setObservaciones(normalizarOpcional(form.getObservaciones()));

        Prestamo guardado = prestamoRepository.saveAndFlush(prestamo);
        guardado.setFolio("PR-" + String.format("%04d", 1000 + guardado.getId()));
        prestamoRepository.save(guardado);

        inventario.setCantidadDisponible(inventario.getCantidadDisponible() - form.getCantidad());
        inventario.setEstado(EstadoInventario.EN_PRESTAMO);
        inventarioRepository.save(inventario);
        registrarMovimiento(
                inventario,
                TipoMovimientoInventario.PRESTAMO,
                "Salida por préstamo " + guardado.getFolio() + " para " + cliente.getNombreMostrado() + ".",
                -form.getCantidad(),
                identidadUsuario
        );
        return guardado;
    }

    @Transactional
    public Prestamo registrarDevolucion(DevolucionPrestamoForm form, String identidadUsuario) {
        Prestamo prestamo = obtenerPorId(form.getId());
        if (prestamo.getEstado() != EstadoPrestamo.ACTIVO) {
            throw new IllegalArgumentException("Este préstamo ya fue devuelto o no se encuentra activo.");
        }
        if (form.getFechaDevolucion().isBefore(prestamo.getFechaSalida())) {
            throw new IllegalArgumentException("La devolución no puede ser anterior a la fecha de salida.");
        }

        Inventario inventario = inventarioRepository.buscarActivoParaActualizar(prestamo.getInventario().getId())
                .orElseThrow(() -> new IllegalArgumentException("El equipo relacionado ya no está activo."));
        CondicionEquipo condicion = form.getCondicionDevolucion();
        if (condicion.requiereMantenimiento()) {
            inventario.setEstado(EstadoInventario.EN_MANTENIMIENTO);
        } else {
            int nuevaDisponibilidad = Math.min(
                    inventario.getCantidadTotal(),
                    inventario.getCantidadDisponible() + prestamo.getCantidad()
            );
            inventario.setCantidadDisponible(nuevaDisponibilidad);
            inventario.setEstado(nuevaDisponibilidad == inventario.getCantidadTotal()
                    ? EstadoInventario.DISPONIBLE
                    : EstadoInventario.EN_PRESTAMO);
        }
        inventarioRepository.save(inventario);

        prestamo.setEstado(EstadoPrestamo.DEVUELTO);
        prestamo.setFechaDevolucionReal(form.getFechaDevolucion());
        prestamo.setCondicionDevolucion(condicion);
        prestamo.setObservacionesDevolucion(normalizarOpcional(form.getObservaciones()));
        Prestamo guardado = prestamoRepository.save(prestamo);

        registrarMovimiento(
                inventario,
                TipoMovimientoInventario.DEVOLUCION,
                "Devolución del préstamo " + prestamo.getFolio() + ". Condición: " + condicion.getEtiqueta() + ".",
                condicion.requiereMantenimiento() ? 0 : prestamo.getCantidad(),
                identidadUsuario
        );
        return guardado;
    }

    @Transactional
    public Prestamo marcarNotificacionEnviada(Long id) {
        Prestamo prestamo = obtenerPorId(id);
        prestamo.setUltimaNotificacionVencimiento(LocalDateTime.now());
        return prestamoRepository.save(prestamo);
    }

    public String normalizarFiltroEstado(String estado) {
        if (estado == null) {
            return null;
        }
        String normalizado = estado.trim().toUpperCase(Locale.ROOT);
        return Set.of("ACTIVO", "VENCIDO", "DEVUELTO").contains(normalizado) ? normalizado : null;
    }

    public String normalizarFiltroFecha(String fecha) {
        if (fecha == null) {
            return null;
        }
        String normalizada = fecha.trim().toUpperCase(Locale.ROOT);
        return Set.of("HOY", "PENDIENTES", "DEVUELTOS").contains(normalizada) ? normalizada : null;
    }

    private void validarFechas(LocalDate salida, LocalDate devolucion) {
        if (salida != null && devolucion != null && devolucion.isBefore(salida)) {
            throw new IllegalArgumentException("La devolución estimada no puede ser anterior a la fecha de salida.");
        }
    }

    private void registrarMovimiento(
            Inventario inventario,
            TipoMovimientoInventario tipo,
            String descripcion,
            int cantidad,
            String identidadUsuario
    ) {
        Usuario usuario = usuarioRepository.findByUsernameOrEmail(identidadUsuario, identidadUsuario)
                .orElseThrow(() -> new IllegalStateException("No se encontró el usuario autenticado."));
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setInventario(inventario);
        movimiento.setUsuario(usuario);
        movimiento.setTipo(tipo);
        movimiento.setDescripcion(descripcion);
        movimiento.setCantidadAfectada(cantidad);
        movimientoRepository.save(movimiento);
    }

    private String correoContacto(String correo, Cliente cliente) {
        String normalizado = normalizarOpcional(correo);
        return (normalizado == null ? cliente.getCorreo() : normalizado).toLowerCase(Locale.ROOT);
    }

    private String normalizarOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    public record EstadisticasPrestamo(long activos, long vencidos, long paraHoy, long devueltos) {
    }
}
