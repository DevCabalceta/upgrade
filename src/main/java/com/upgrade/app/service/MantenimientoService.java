package com.upgrade.app.service;

import com.upgrade.app.domain.ChecklistMantenimiento;
import com.upgrade.app.domain.Cliente;
import com.upgrade.app.domain.EstadoInventario;
import com.upgrade.app.domain.EstadoMantenimiento;
import com.upgrade.app.domain.EvidenciaMantenimiento;
import com.upgrade.app.domain.Inventario;
import com.upgrade.app.domain.MovimientoInventario;
import com.upgrade.app.domain.ObservacionMantenimiento;
import com.upgrade.app.domain.OrdenMantenimiento;
import com.upgrade.app.domain.PrioridadMantenimiento;
import com.upgrade.app.domain.TipoMantenimiento;
import com.upgrade.app.domain.TipoMovimientoInventario;
import com.upgrade.app.domain.Usuario;
import com.upgrade.app.dto.AsignarTecnicoMantenimientoForm;
import com.upgrade.app.dto.CerrarMantenimientoForm;
import com.upgrade.app.dto.MantenimientoForm;
import com.upgrade.app.dto.ObservacionMantenimientoForm;
import com.upgrade.app.exception.MantenimientoNoEncontradoException;
import com.upgrade.app.repository.ChecklistMantenimientoRepository;
import com.upgrade.app.repository.ClienteRepository;
import com.upgrade.app.repository.EvidenciaMantenimientoRepository;
import com.upgrade.app.repository.InventarioRepository;
import com.upgrade.app.repository.MovimientoInventarioRepository;
import com.upgrade.app.repository.ObservacionMantenimientoRepository;
import com.upgrade.app.repository.OrdenMantenimientoRepository;
import com.upgrade.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MantenimientoService {

    private final OrdenMantenimientoRepository ordenRepository;
    private final ObservacionMantenimientoRepository observacionRepository;
    private final ChecklistMantenimientoRepository checklistRepository;
    private final EvidenciaMantenimientoRepository evidenciaRepository;
    private final InventarioRepository inventarioRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final MantenimientoStorageService storageService;

    public Page<OrdenMantenimiento> listar(
            String buscar,
            TipoMantenimiento tipo,
            EstadoMantenimiento estado,
            PrioridadMantenimiento prioridad,
            int pagina,
            int tamano
    ) {
        return ordenRepository.buscar(
                normalizarOpcional(buscar),
                tipo,
                estado,
                prioridad,
                PageRequest.of(
                        Math.max(pagina, 0),
                        Math.min(Math.max(tamano, 5), 50),
                        Sort.by(Sort.Order.desc("fechaProgramada"), Sort.Order.desc("id"))
                )
        );
    }

    public OrdenMantenimiento obtenerPorId(Long id) {
        return ordenRepository.buscarDetallePorId(id)
                .orElseThrow(() -> new MantenimientoNoEncontradoException(id));
    }

    public List<Inventario> listarEquipos() {
        return inventarioRepository.findAllByActivoTrueOrderByNombreAsc();
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.findAllByActivoTrueOrderByNombreAsc();
    }

    public List<Usuario> listarTecnicos() {
        return usuarioRepository.findAllByActivoTrueOrderByNombreAscApellidoAsc();
    }

    public EstadisticasMantenimiento obtenerEstadisticas() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());
        return new EstadisticasMantenimiento(
                ordenRepository.countByActivoTrueAndEstado(EstadoMantenimiento.EN_PROCESO),
                ordenRepository.countByActivoTrueAndEstadoAndFechaProgramadaBetween(
                        EstadoMantenimiento.PROGRAMADO,
                        inicioMes,
                        finMes
                ),
                ordenRepository.contarVencidas(
                        List.of(EstadoMantenimiento.PROGRAMADO, EstadoMantenimiento.EN_PROCESO),
                        hoy
                ),
                ordenRepository.sumarCostoEntre(inicioMes, finMes)
        );
    }

    public Map<Long, DetalleMantenimiento> obtenerDetalles(Collection<OrdenMantenimiento> ordenes) {
        List<Long> ids = ordenes.stream().map(OrdenMantenimiento::getId).toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<ObservacionMantenimiento>> observaciones = observacionRepository.listarPorOrdenes(ids)
                .stream().collect(Collectors.groupingBy(
                        item -> item.getOrden().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        Map<Long, List<ChecklistMantenimiento>> checklist = checklistRepository.listarPorOrdenes(ids)
                .stream().collect(Collectors.groupingBy(
                        item -> item.getOrden().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        Map<Long, List<EvidenciaMantenimiento>> evidencias = evidenciaRepository.listarPorOrdenes(ids)
                .stream().collect(Collectors.groupingBy(
                        item -> item.getOrden().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        Map<Long, DetalleMantenimiento> resultado = new LinkedHashMap<>();
        for (Long id : ids) {
            resultado.put(id, new DetalleMantenimiento(
                    observaciones.getOrDefault(id, List.of()),
                    checklist.getOrDefault(id, List.of()),
                    evidencias.getOrDefault(id, List.of())
            ));
        }
        return resultado;
    }

    @Transactional
    public OrdenMantenimiento crear(MantenimientoForm form, String identidadUsuario) {
        Inventario inventario = inventarioActivo(form.getInventarioId());
        OrdenMantenimiento orden = new OrdenMantenimiento();
        orden.setInventario(inventario);
        orden.setCreadoPor(usuarioPorIdentidad(identidadUsuario));
        aplicarFormulario(orden, form, true);
        OrdenMantenimiento guardada = ordenRepository.saveAndFlush(orden);
        guardada.setNumero("MNT-" + String.format("%04d", 1000 + guardada.getId()));
        ordenRepository.save(guardada);
        crearChecklistInicial(guardada);
        if (guardada.getEstado() == EstadoMantenimiento.EN_PROCESO) {
            iniciarMantenimiento(guardada, identidadUsuario);
        }
        return guardada;
    }

    @Transactional
    public OrdenMantenimiento actualizar(MantenimientoForm form, String identidadUsuario) {
        OrdenMantenimiento orden = obtenerPorId(form.getId());
        if (orden.getEstado().estaCerrado()) {
            throw new IllegalArgumentException("Una orden finalizada o cancelada ya no puede editarse.");
        }
        if (form.getEstado() == EstadoMantenimiento.FINALIZADO) {
            throw new IllegalArgumentException("Utiliza la acción Cerrar orden para finalizar el mantenimiento.");
        }

        EstadoMantenimiento estadoAnterior = orden.getEstado();
        Long inventarioAnterior = orden.getInventario().getId();
        if (estadoAnterior == EstadoMantenimiento.EN_PROCESO
                && !Objects.equals(inventarioAnterior, form.getInventarioId())) {
            throw new IllegalArgumentException("No puedes cambiar el equipo mientras la orden está en proceso.");
        }

        if (estadoAnterior == EstadoMantenimiento.EN_PROCESO
                && form.getEstado() != EstadoMantenimiento.EN_PROCESO) {
            liberarInventario(orden, identidadUsuario);
        }
        orden.setInventario(inventarioActivo(form.getInventarioId()));
        aplicarFormulario(orden, form, false);
        if (estadoAnterior != EstadoMantenimiento.EN_PROCESO
                && orden.getEstado() == EstadoMantenimiento.EN_PROCESO) {
            iniciarMantenimiento(orden, identidadUsuario);
        }
        if (orden.getEstado() == EstadoMantenimiento.CANCELADO) {
            orden.setFechaCierre(LocalDateTime.now());
        }
        return ordenRepository.save(orden);
    }

    @Transactional
    public OrdenMantenimiento asignarTecnico(AsignarTecnicoMantenimientoForm form) {
        OrdenMantenimiento orden = obtenerPorId(form.getOrdenId());
        if (orden.getEstado().estaCerrado()) {
            throw new IllegalArgumentException("No se puede reasignar una orden cerrada.");
        }
        orden.setTecnico(usuarioActivo(form.getTecnicoId()));
        return ordenRepository.save(orden);
    }

    @Transactional
    public ObservacionMantenimiento agregarObservacion(
            ObservacionMantenimientoForm form,
            String identidadUsuario
    ) {
        ObservacionMantenimiento observacion = new ObservacionMantenimiento();
        observacion.setOrden(obtenerPorId(form.getOrdenId()));
        observacion.setUsuario(usuarioPorIdentidad(identidadUsuario));
        observacion.setTexto(form.getTexto().trim());
        return observacionRepository.save(observacion);
    }

    @Transactional
    public List<EvidenciaMantenimiento> guardarEvidencias(
            Long ordenId,
            MultipartFile[] archivos,
            String identidadUsuario
    ) {
        if (archivos == null || archivos.length == 0 || archivos[0].isEmpty()) {
            throw new IllegalArgumentException("Selecciona al menos una evidencia.");
        }
        if (archivos.length > 8) {
            throw new IllegalArgumentException("Puedes subir un máximo de 8 archivos a la vez.");
        }
        OrdenMantenimiento orden = obtenerPorId(ordenId);
        Usuario usuario = usuarioPorIdentidad(identidadUsuario);
        List<MantenimientoStorageService.ArchivoMantenimientoGuardado> guardados = new ArrayList<>();
        List<EvidenciaMantenimiento> evidencias = new ArrayList<>();
        try {
            for (MultipartFile archivo : archivos) {
                MantenimientoStorageService.ArchivoMantenimientoGuardado guardado = storageService.guardar(archivo);
                guardados.add(guardado);
                EvidenciaMantenimiento evidencia = new EvidenciaMantenimiento();
                evidencia.setOrden(orden);
                evidencia.setUsuario(usuario);
                evidencia.setNombreOriginal(guardado.nombreOriginal());
                evidencia.setRutaArchivo(guardado.rutaPublica());
                evidencia.setTipoMime(guardado.tipoMime());
                evidencia.setTamanoBytes(guardado.tamano());
                evidencias.add(evidenciaRepository.save(evidencia));
            }
            return evidencias;
        } catch (RuntimeException exception) {
            guardados.forEach(item -> storageService.eliminar(item.rutaPublica()));
            throw exception;
        }
    }

    @Transactional
    public ChecklistMantenimiento alternarChecklist(Long checklistId) {
        ChecklistMantenimiento item = checklistRepository.buscarActivoPorId(checklistId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la tarea del checklist."));
        if (item.getOrden().getEstado().estaCerrado()) {
            throw new IllegalArgumentException("El checklist de una orden cerrada no puede modificarse.");
        }
        item.setCompletado(!Boolean.TRUE.equals(item.getCompletado()));
        return checklistRepository.save(item);
    }

    @Transactional
    public OrdenMantenimiento cerrar(CerrarMantenimientoForm form, String identidadUsuario) {
        OrdenMantenimiento orden = obtenerPorId(form.getOrdenId());
        if (orden.getEstado().estaCerrado()) {
            throw new IllegalArgumentException("La orden ya se encuentra cerrada.");
        }
        List<ChecklistMantenimiento> checklist = checklistRepository.listarPorOrdenes(List.of(orden.getId()));
        boolean pendientes = checklist.stream().anyMatch(item -> !Boolean.TRUE.equals(item.getCompletado()));
        if (pendientes) {
            throw new IllegalArgumentException("Completa todas las tareas del checklist antes de cerrar la orden.");
        }
        if (orden.getEstado() == EstadoMantenimiento.EN_PROCESO) {
            liberarInventario(orden, identidadUsuario);
        }
        orden.setEstado(EstadoMantenimiento.FINALIZADO);
        orden.setResumenCierre(form.getResumen().trim());
        orden.setHorasReales(form.getHorasReales());
        orden.setProximaRevision(form.getProximaRevision());
        orden.setFechaCierre(LocalDateTime.now());
        return ordenRepository.save(orden);
    }

    @Transactional
    public OrdenMantenimiento eliminarLogicamente(Long id, String confirmacion, String identidadUsuario) {
        if (!"ELIMINAR".equals(confirmacion == null ? "" : confirmacion.trim().toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Escribe ELIMINAR para confirmar.");
        }
        OrdenMantenimiento orden = obtenerPorId(id);
        if (orden.getEstado() == EstadoMantenimiento.EN_PROCESO) {
            liberarInventario(orden, identidadUsuario);
        }
        orden.setActivo(false);
        return ordenRepository.save(orden);
    }

    public MantenimientoForm convertirAFormulario(OrdenMantenimiento orden) {
        MantenimientoForm form = new MantenimientoForm();
        form.setId(orden.getId());
        form.setInventarioId(orden.getInventario().getId());
        form.setClienteId(orden.getCliente() == null ? null : orden.getCliente().getId());
        form.setTecnicoId(orden.getTecnico() == null ? null : orden.getTecnico().getId());
        form.setTipo(orden.getTipo());
        form.setPrioridad(orden.getPrioridad());
        form.setEstado(orden.getEstado());
        form.setUbicacion(orden.getUbicacion());
        form.setFechaProgramada(orden.getFechaProgramada());
        form.setHoraProgramada(orden.getHoraProgramada());
        form.setCostoEstimado(orden.getCostoEstimado());
        form.setDuracionEstimada(orden.getDuracionEstimada());
        form.setDescripcion(orden.getDescripcion());
        return form;
    }

    private void aplicarFormulario(OrdenMantenimiento orden, MantenimientoForm form, boolean creacion) {
        orden.setCliente(clienteOpcional(form.getClienteId()));
        orden.setTecnico(usuarioActivo(form.getTecnicoId()));
        orden.setTipo(form.getTipo());
        orden.setPrioridad(form.getPrioridad());
        orden.setEstado(creacion ? EstadoMantenimiento.PROGRAMADO : form.getEstado());
        orden.setUbicacion(normalizarOpcional(form.getUbicacion()));
        orden.setFechaProgramada(form.getFechaProgramada());
        orden.setHoraProgramada(form.getHoraProgramada());
        orden.setCostoEstimado(form.getCostoEstimado());
        orden.setDuracionEstimada(form.getDuracionEstimada());
        orden.setDescripcion(form.getDescripcion().trim());
    }

    private void crearChecklistInicial(OrdenMantenimiento orden) {
        List<String> tareas = switch (orden.getTipo()) {
            case PREVENTIVO -> List.of(
                    "Inspección visual",
                    "Limpieza general",
                    "Verificación de conexiones",
                    "Prueba de funcionamiento",
                    "Registro de parámetros"
            );
            case CORRECTIVO -> List.of(
                    "Diagnóstico de la falla",
                    "Identificación de componentes afectados",
                    "Reparación o sustitución",
                    "Prueba operativa",
                    "Validación final"
            );
            case PREDICTIVO -> List.of(
                    "Medición de parámetros",
                    "Análisis de tendencias",
                    "Identificación de riesgos",
                    "Recomendaciones técnicas",
                    "Programación de próxima revisión"
            );
            case EMERGENCIA -> List.of(
                    "Asegurar el equipo y el área",
                    "Diagnóstico inmediato",
                    "Aplicar corrección prioritaria",
                    "Prueba bajo carga",
                    "Documentar la contingencia"
            );
        };
        for (int indice = 0; indice < tareas.size(); indice++) {
            ChecklistMantenimiento item = new ChecklistMantenimiento();
            item.setOrden(orden);
            item.setTarea(tareas.get(indice));
            item.setPosicion(indice + 1);
            item.setCompletado(false);
            checklistRepository.save(item);
        }
    }

    private void iniciarMantenimiento(OrdenMantenimiento orden, String identidadUsuario) {
        Inventario inventario = inventarioActivo(orden.getInventario().getId());
        if (ordenRepository.existsByInventarioIdAndEstadoAndActivoTrueAndIdNot(
                inventario.getId(),
                EstadoMantenimiento.EN_PROCESO,
                orden.getId()
        )) {
            throw new IllegalArgumentException("El equipo ya tiene otra orden de mantenimiento en proceso.");
        }
        orden.setEstadoInventarioPrevio(inventario.getEstado());
        inventario.setEstado(EstadoInventario.EN_MANTENIMIENTO);
        inventarioRepository.save(inventario);
        registrarMovimiento(
                inventario,
                TipoMovimientoInventario.INICIO_MANTENIMIENTO,
                "Inicio de la orden " + orden.getNumero() + ".",
                identidadUsuario
        );
    }

    private void liberarInventario(OrdenMantenimiento orden, String identidadUsuario) {
        Inventario inventario = inventarioActivo(orden.getInventario().getId());
        boolean otraEnProceso = ordenRepository.existsByInventarioIdAndEstadoAndActivoTrueAndIdNot(
                inventario.getId(),
                EstadoMantenimiento.EN_PROCESO,
                orden.getId()
        );
        if (!otraEnProceso && inventario.getEstado() == EstadoInventario.EN_MANTENIMIENTO) {
            EstadoInventario previo = orden.getEstadoInventarioPrevio();
            if (previo != null
                    && previo != EstadoInventario.EN_MANTENIMIENTO
                    && previo != EstadoInventario.DADO_DE_BAJA) {
                inventario.setEstado(previo);
            } else if (Objects.equals(inventario.getCantidadDisponible(), inventario.getCantidadTotal())) {
                inventario.setEstado(EstadoInventario.DISPONIBLE);
            } else {
                inventario.setEstado(EstadoInventario.EN_PRESTAMO);
            }
            inventarioRepository.save(inventario);
        }
        registrarMovimiento(
                inventario,
                TipoMovimientoInventario.FIN_MANTENIMIENTO,
                "Liberación del equipo por la orden " + orden.getNumero() + ".",
                identidadUsuario
        );
    }

    private void registrarMovimiento(
            Inventario inventario,
            TipoMovimientoInventario tipo,
            String descripcion,
            String identidadUsuario
    ) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setInventario(inventario);
        movimiento.setUsuario(usuarioPorIdentidad(identidadUsuario));
        movimiento.setTipo(tipo);
        movimiento.setDescripcion(descripcion);
        movimiento.setCantidadAfectada(0);
        movimientoRepository.save(movimiento);
    }

    private Inventario inventarioActivo(Long id) {
        return inventarioRepository.buscarActivoParaActualizar(id)
                .orElseThrow(() -> new IllegalArgumentException("El equipo seleccionado no existe o está inactivo."));
    }

    private Cliente clienteOpcional(Long id) {
        if (id == null) {
            return null;
        }
        return clienteRepository.findById(id)
                .filter(item -> Boolean.TRUE.equals(item.getActivo()))
                .orElseThrow(() -> new IllegalArgumentException("El cliente seleccionado no existe o está inactivo."));
    }

    private Usuario usuarioActivo(Long id) {
        return usuarioRepository.findById(id)
                .filter(item -> Boolean.TRUE.equals(item.getActivo()))
                .orElseThrow(() -> new IllegalArgumentException("El técnico seleccionado no está disponible."));
    }

    private Usuario usuarioPorIdentidad(String identidad) {
        return usuarioRepository.findByUsernameOrEmail(identidad, identidad)
                .filter(item -> Boolean.TRUE.equals(item.getActivo()))
                .orElseThrow(() -> new IllegalStateException("No se encontró el usuario autenticado."));
    }

    private String normalizarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public record EstadisticasMantenimiento(
            long enProceso,
            long programadasMes,
            long vencidas,
            BigDecimal costoMensual
    ) {
    }

    public record DetalleMantenimiento(
            List<ObservacionMantenimiento> observaciones,
            List<ChecklistMantenimiento> checklist,
            List<EvidenciaMantenimiento> evidencias
    ) {
    }
}
