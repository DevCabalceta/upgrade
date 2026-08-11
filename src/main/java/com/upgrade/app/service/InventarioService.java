package com.upgrade.app.service;

import com.upgrade.app.domain.CategoriaInventario;
import com.upgrade.app.domain.EstadoInventario;
import com.upgrade.app.domain.Inventario;
import com.upgrade.app.domain.MovimientoInventario;
import com.upgrade.app.domain.TipoMovimientoInventario;
import com.upgrade.app.domain.Usuario;
import com.upgrade.app.dto.CambioEstadoInventarioForm;
import com.upgrade.app.dto.InventarioForm;
import com.upgrade.app.exception.InventarioNoEncontradoException;
import com.upgrade.app.repository.CategoriaInventarioRepository;
import com.upgrade.app.repository.InventarioRepository;
import com.upgrade.app.repository.MovimientoInventarioRepository;
import com.upgrade.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final CategoriaInventarioRepository categoriaRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final UsuarioRepository usuarioRepository;

    public Page<Inventario> listar(
            String buscar,
            EstadoInventario estado,
            Long categoriaId,
            int pagina,
            int tamano
    ) {
        PageRequest pageable = PageRequest.of(
                Math.max(pagina, 0),
                Math.min(Math.max(tamano, 5), 50),
                Sort.by(Sort.Order.asc("codigo"))
        );
        return inventarioRepository.buscar(normalizarOpcional(buscar), estado, categoriaId, pageable);
    }

    public List<Inventario> listarParaExportar(String buscar, EstadoInventario estado, Long categoriaId) {
        return inventarioRepository.buscar(
                normalizarOpcional(buscar),
                estado,
                categoriaId,
                PageRequest.of(0, 10_000, Sort.by(Sort.Order.asc("codigo")))
        ).getContent();
    }

    public List<CategoriaInventario> listarCategorias() {
        return categoriaRepository.findAllByActivaTrueOrderByNombreAsc();
    }

    public Inventario obtenerPorId(Long id) {
        return inventarioRepository.findById(id)
                .filter(item -> Boolean.TRUE.equals(item.getActivo()))
                .orElseThrow(() -> new InventarioNoEncontradoException(id));
    }

    public List<MovimientoInventario> listarMovimientos(Long inventarioId) {
        return movimientoRepository.findTop20ByInventarioIdOrderByFechaMovimientoDesc(inventarioId);
    }

    public Map<Long, List<MovimientoInventario>> movimientosPorEquipo(List<Inventario> equipos) {
        Map<Long, List<MovimientoInventario>> movimientos = new LinkedHashMap<>();
        equipos.forEach(equipo -> movimientos.put(equipo.getId(), listarMovimientos(equipo.getId())));
        return movimientos;
    }

    public Map<Long, Long> conteosPorCategoria() {
        Map<Long, Long> conteos = new LinkedHashMap<>();
        listarCategorias().forEach(categoria ->
                conteos.put(categoria.getId(), inventarioRepository.countByActivoTrueAndCategoriaId(categoria.getId()))
        );
        return conteos;
    }

    public EstadisticasInventario obtenerEstadisticas() {
        return new EstadisticasInventario(
                inventarioRepository.countByActivoTrue(),
                inventarioRepository.countByActivoTrueAndEstado(EstadoInventario.DISPONIBLE),
                inventarioRepository.countByActivoTrueAndEstado(EstadoInventario.EN_PRESTAMO),
                inventarioRepository.countByActivoTrueAndEstado(EstadoInventario.EN_MANTENIMIENTO),
                inventarioRepository.sumarValorInventarioActivo()
        );
    }

    public boolean codigoOcupado(String codigo, Long idActual) {
        if (codigo == null || codigo.isBlank()) {
            return false;
        }
        String normalizado = codigo.trim().toUpperCase(Locale.ROOT);
        return idActual == null
                ? inventarioRepository.existsByCodigoIgnoreCase(normalizado)
                : inventarioRepository.existsByCodigoIgnoreCaseAndIdNot(normalizado, idActual);
    }

    public boolean serieOcupada(String serie, Long idActual) {
        String normalizada = normalizarOpcional(serie);
        if (normalizada == null) {
            return false;
        }
        return idActual == null
                ? inventarioRepository.existsByNumeroSerieIgnoreCase(normalizada)
                : inventarioRepository.existsByNumeroSerieIgnoreCaseAndIdNot(normalizada, idActual);
    }

    @Transactional
    public Inventario crear(InventarioForm form, String usuarioActual) {
        Inventario inventario = new Inventario();
        copiarFormulario(form, inventario);
        Inventario guardado = inventarioRepository.save(inventario);
        registrarMovimiento(
                guardado,
                TipoMovimientoInventario.REGISTRO,
                "Registro inicial del equipo en inventario.",
                guardado.getCantidadTotal(),
                usuarioActual
        );
        return guardado;
    }

    @Transactional
    public Inventario actualizar(InventarioForm form, String usuarioActual) {
        Inventario inventario = obtenerPorId(form.getId());
        int cantidadAnterior = inventario.getCantidadTotal();
        copiarFormulario(form, inventario);
        Inventario guardado = inventarioRepository.save(inventario);
        registrarMovimiento(
                guardado,
                TipoMovimientoInventario.ACTUALIZACION,
                "Se actualizaron los datos generales, stock y ubicación del equipo.",
                guardado.getCantidadTotal() - cantidadAnterior,
                usuarioActual
        );
        return guardado;
    }

    @Transactional
    public Inventario cambiarEstado(CambioEstadoInventarioForm form, String usuarioActual) {
        Inventario inventario = obtenerPorId(form.getId());
        EstadoInventario anterior = inventario.getEstado();
        inventario.setEstado(form.getNuevoEstado());
        Inventario guardado = inventarioRepository.save(inventario);
        registrarMovimiento(
                guardado,
                TipoMovimientoInventario.CAMBIO_ESTADO,
                "Estado: " + anterior.getEtiqueta() + " → " + form.getNuevoEstado().getEtiqueta()
                        + ". Motivo: " + form.getMotivo().trim(),
                0,
                usuarioActual
        );
        return guardado;
    }

    @Transactional
    public void eliminarLogicamente(Long id, String usuarioActual) {
        Inventario inventario = obtenerPorId(id);
        inventario.setActivo(false);
        inventario.setEstado(EstadoInventario.DADO_DE_BAJA);
        inventarioRepository.save(inventario);
        registrarMovimiento(
                inventario,
                TipoMovimientoInventario.BAJA,
                "El equipo fue dado de baja del inventario.",
                -inventario.getCantidadTotal(),
                usuarioActual
        );
    }

    private void copiarFormulario(InventarioForm form, Inventario inventario) {
        inventario.setCodigo(limpiar(form.getCodigo()).toUpperCase(Locale.ROOT));
        inventario.setNombre(limpiar(form.getNombre()));
        inventario.setDescripcion(normalizarOpcional(form.getDescripcion()));
        inventario.setNumeroSerie(normalizarOpcional(form.getNumeroSerie()));
        inventario.setMarca(normalizarOpcional(form.getMarca()));
        inventario.setModelo(normalizarOpcional(form.getModelo()));
        inventario.setBodega(normalizarOpcional(form.getBodega()));
        inventario.setUbicacion(normalizarOpcional(form.getUbicacion()));
        inventario.setValorUnitario(form.getValorUnitario());
        inventario.setPrecioVenta(form.getPrecioVenta());
        inventario.setCantidadTotal(form.getCantidadTotal());
        inventario.setCantidadDisponible(form.getCantidadDisponible());
        inventario.setEstado(form.getEstado());
        inventario.setFechaAdquisicion(form.getFechaAdquisicion());
        inventario.setCategoria(categoriaRepository.findById(form.getCategoriaId())
                .filter(categoria -> Boolean.TRUE.equals(categoria.getActiva()))
                .orElseThrow(() -> new IllegalArgumentException("La categoría seleccionada no existe o está inactiva.")));
        inventario.setActivo(true);
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

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String normalizarOpcional(String valor) {
        String limpio = limpiar(valor);
        return limpio.isEmpty() ? null : limpio;
    }

    public record EstadisticasInventario(
            long total,
            long disponibles,
            long enPrestamo,
            long enMantenimiento,
            BigDecimal valorTotal
    ) {
    }
}
