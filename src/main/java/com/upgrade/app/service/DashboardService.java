package com.upgrade.app.service;

import com.upgrade.app.domain.Cliente;
import com.upgrade.app.domain.EstadoMantenimiento;
import com.upgrade.app.domain.EstadoPrestamo;
import com.upgrade.app.domain.EstadoServicio;
import com.upgrade.app.domain.MovimientoInventario;
import com.upgrade.app.domain.OrdenMantenimiento;
import com.upgrade.app.domain.Prestamo;
import com.upgrade.app.domain.Servicio;
import com.upgrade.app.repository.ClienteRepository;
import com.upgrade.app.repository.DashboardConsultaRepository;
import com.upgrade.app.repository.InventarioRepository;
import com.upgrade.app.repository.MovimientoInventarioRepository;
import com.upgrade.app.repository.OrdenMantenimientoRepository;
import com.upgrade.app.repository.PrestamoRepository;
import com.upgrade.app.repository.ServicioRepository;
import com.upgrade.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    public record Kpi(String etiqueta, String valor, String detalle, String icono, String tono) {}
    public record PuntoMensual(String periodo, String etiqueta, long valor, int porcentaje) {}
    public record ServicioDestacado(String nombre, int servicios, int porcentaje) {}
    public record Actividad(String titulo, String detalle, String tiempo, String icono, String tono,
                            LocalDateTime fecha) {}
    public record Agenda(String dia, String mes, String titulo, String detalle, String estado,
                         String tono, LocalDateTime fecha) {}
    public record Resumen(List<Kpi> kpis, List<PuntoMensual> actividadMensual,
                          List<ServicioDestacado> serviciosDestacados,
                          List<Actividad> actividadReciente, List<Agenda> agenda) {}

    private static final Locale LOCALE_ES = Locale.forLanguageTag("es-CR");
    private static final ZoneId COSTA_RICA = ZoneId.of("America/Costa_Rica");

    private final ClienteRepository clienteRepository;
    private final InventarioRepository inventarioRepository;
    private final PrestamoRepository prestamoRepository;
    private final OrdenMantenimientoRepository mantenimientoRepository;
    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final DashboardConsultaRepository dashboardConsultaRepository;

    public Resumen obtenerResumen() {
        LocalDate hoy = LocalDate.now(COSTA_RICA);
        LocalDateTime inicioMes = hoy.withDayOfMonth(1).atStartOfDay();
        LocalDateTime inicioSiguienteMes = hoy.plusMonths(1).withDayOfMonth(1).atStartOfDay();

        long clientes = clienteRepository.countByActivoTrue();
        long clientesNuevos = clienteRepository.countByActivoTrueAndFechaRegistroBetween(inicioMes, inicioSiguienteMes);
        long equiposDisponibles = inventarioRepository.sumarCantidadDisponible();
        long equiposTotales = inventarioRepository.sumarCantidadTotal();
        long prestamosActivos = prestamoRepository.contarActivos(EstadoPrestamo.ACTIVO, hoy);
        long devolucionesHoy = prestamoRepository.contarPorDevolverHoy(EstadoPrestamo.ACTIVO, hoy);
        long prestamosVencidos = prestamoRepository.contarVencidos(EstadoPrestamo.ACTIVO, hoy);
        long mantenimientos = mantenimientoRepository.countByActivoTrueAndEstado(EstadoMantenimiento.EN_PROCESO);
        long mantenimientosVencidos = mantenimientoRepository.contarVencidas(
                List.of(EstadoMantenimiento.PROGRAMADO, EstadoMantenimiento.EN_PROCESO), hoy);
        long servicios = servicioRepository.countByEliminadoFalseAndEstado(EstadoServicio.ACTIVO);
        long serviciosLanding = servicioRepository.countByEliminadoFalseAndEstadoAndVisibleLandingTrue(EstadoServicio.ACTIVO);
        long colaboradores = usuarioRepository.countByActivoTrue();
        BigDecimal valorInventario = inventarioRepository.sumarValorInventarioActivo();

        List<Kpi> kpis = List.of(
                new Kpi("Clientes activos", numero(clientes), clientesNuevos + " nuevos este mes", "users", "primary"),
                new Kpi("Equipos disponibles", numero(equiposDisponibles), numero(equiposTotales) + " unidades totales", "package", "primary"),
                new Kpi("Préstamos activos", numero(prestamosActivos), devolucionesHoy + " por devolver hoy", "hand-coins", "warning"),
                new Kpi("En mantenimiento", numero(mantenimientos), mantenimientosVencidos + " órdenes vencidas", "wrench", "warning"),
                new Kpi("Servicios activos", numero(servicios), serviciosLanding + " publicados en landing", "sparkles", "primary"),
                new Kpi("Colaboradores activos", numero(colaboradores), "cuentas habilitadas", "user-cog", "muted"),
                new Kpi("Valor de inventario", monedaCompacta(valorInventario), "valor registrado", "circle-dollar-sign", "success"),
                new Kpi("Préstamos vencidos", numero(prestamosVencidos), prestamosVencidos == 0 ? "operación al día" : "requieren seguimiento", "triangle-alert", "destructive")
        );

        return new Resumen(kpis, construirSerieMensual(hoy), listarServiciosDestacados(),
                construirActividadReciente(), construirAgenda(hoy));
    }

    private List<PuntoMensual> construirSerieMensual(LocalDate hoy) {
        YearMonth primero = YearMonth.from(hoy).minusMonths(11);
        Map<String, Long> datos = dashboardConsultaRepository.contarActividadMensual(primero.atDay(1).atStartOfDay());
        List<YearMonth> meses = new ArrayList<>();
        for (int i = 0; i < 12; i++) meses.add(primero.plusMonths(i));
        long maximo = meses.stream().mapToLong(mes -> datos.getOrDefault(mes.toString(), 0L)).max().orElse(0L);
        List<PuntoMensual> serie = new ArrayList<>();
        for (YearMonth mes : meses) {
            long valor = datos.getOrDefault(mes.toString(), 0L);
            int porcentaje = maximo == 0 ? 0 : (int) Math.round(valor * 100.0 / maximo);
            String nombre = mes.getMonth().getDisplayName(TextStyle.SHORT, LOCALE_ES).replace(".", "");
            serie.add(new PuntoMensual(
                    nombre + " " + mes.getYear(),
                    nombre.substring(0, 1).toUpperCase(LOCALE_ES),
                    valor,
                    porcentaje
            ));
        }
        return serie;
    }

    private List<ServicioDestacado> listarServiciosDestacados() {
        List<Servicio> servicios = servicioRepository
                .findTop5ByEliminadoFalseAndEstadoOrderByServiciosYtdDescNombreAsc(EstadoServicio.ACTIVO);
        int maximo = servicios.stream().map(Servicio::getServiciosYtd).max(Integer::compareTo).orElse(0);
        return servicios.stream().map(servicio -> new ServicioDestacado(
                servicio.getNombre(),
                servicio.getServiciosYtd(),
                maximo == 0 ? 0 : (int) Math.round(servicio.getServiciosYtd() * 100.0 / maximo)
        )).toList();
    }

    private List<Actividad> construirActividadReciente() {
        List<Actividad> actividades = new ArrayList<>();
        for (Cliente cliente : clienteRepository.findTop5ByActivoTrueOrderByFechaRegistroDesc()) {
            actividades.add(actividad("Nuevo cliente registrado", cliente.getNombreMostrado() + " · " + cliente.getCorreo(),
                    cliente.getFechaRegistro(), "user-plus", "primary"));
        }
        for (Prestamo prestamo : prestamoRepository.findTop5ByOrderByFechaActualizacionDesc()) {
            String titulo = prestamo.getEstado() == EstadoPrestamo.DEVUELTO ? "Equipo devuelto"
                    : prestamo.isVencido() ? "Préstamo vencido" : "Préstamo registrado";
            String tono = prestamo.getEstado() == EstadoPrestamo.DEVUELTO ? "success"
                    : prestamo.isVencido() ? "destructive" : "primary";
            String icono = prestamo.getEstado() == EstadoPrestamo.DEVUELTO ? "check-circle-2"
                    : prestamo.isVencido() ? "triangle-alert" : "hand-coins";
            actividades.add(actividad(titulo, prestamo.getFolio() + " · " + prestamo.getInventario().getNombre(),
                    prestamo.getFechaActualizacion(), icono, tono));
        }
        for (OrdenMantenimiento orden : mantenimientoRepository.findTop5ByActivoTrueOrderByFechaActualizacionDesc()) {
            String titulo = orden.getEstado() == EstadoMantenimiento.FINALIZADO
                    ? "Mantenimiento finalizado" : "Orden de mantenimiento actualizada";
            String detalle = orden.getNumero() + " · " + orden.getInventario().getNombre();
            actividades.add(actividad(titulo, detalle, orden.getFechaActualizacion(),
                    orden.getEstado() == EstadoMantenimiento.FINALIZADO ? "circle-check-big" : "wrench",
                    orden.getEstado() == EstadoMantenimiento.FINALIZADO ? "success" : "warning"));
        }
        for (MovimientoInventario movimiento : movimientoRepository.findTop5ByOrderByFechaMovimientoDesc()) {
            actividades.add(actividad(movimiento.getTipo().getEtiqueta() + " de inventario",
                    movimiento.getInventario().getCodigo() + " · " + movimiento.getInventario().getNombre(),
                    movimiento.getFechaMovimiento(), "arrow-left-right", tonoMovimiento(movimiento)));
        }
        return actividades.stream().filter(item -> item.fecha() != null)
                .sorted(Comparator.comparing(Actividad::fecha).reversed()).limit(6).toList();
    }

    private List<Agenda> construirAgenda(LocalDate hoy) {
        List<Agenda> agenda = new ArrayList<>();
        for (OrdenMantenimiento orden : mantenimientoRepository.listarProximos(
                List.of(EstadoMantenimiento.PROGRAMADO, EstadoMantenimiento.EN_PROCESO), hoy, PageRequest.of(0, 5))) {
            LocalDateTime fecha = LocalDateTime.of(orden.getFechaProgramada(), orden.getHoraProgramada());
            agenda.add(new Agenda(dia(fecha), mes(fecha), orden.getInventario().getNombre(),
                    orden.getHoraProgramada() + " · " + (orden.getTecnico() == null ? "Sin técnico" : orden.getTecnico().getNombreCompleto()),
                    orden.getEstado().getEtiqueta(), "warning", fecha));
        }
        for (Prestamo prestamo : prestamoRepository.listarProximasDevoluciones(hoy, PageRequest.of(0, 5))) {
            LocalDateTime fecha = prestamo.getFechaDevolucionEstimada().atTime(23, 59);
            agenda.add(new Agenda(dia(fecha), mes(fecha), "Devolución " + prestamo.getFolio(),
                    prestamo.getCliente().getNombreMostrado() + " · " + prestamo.getInventario().getNombre(),
                    "Devolución", "primary", fecha));
        }
        return agenda.stream().sorted(Comparator.comparing(Agenda::fecha)).limit(5).toList();
    }

    private Actividad actividad(String titulo, String detalle, LocalDateTime fecha, String icono, String tono) {
        return new Actividad(titulo, detalle, tiempoRelativo(fecha), icono, tono, fecha);
    }

    private String tonoMovimiento(MovimientoInventario movimiento) {
        return switch (movimiento.getTipo()) {
            case DEVOLUCION, FIN_MANTENIMIENTO -> "success";
            case PRESTAMO, INICIO_MANTENIMIENTO -> "warning";
            case BAJA -> "destructive";
            default -> "muted";
        };
    }

    private String tiempoRelativo(LocalDateTime fecha) {
        if (fecha == null) return "sin fecha";
        long minutos = Math.max(0, ChronoUnit.MINUTES.between(fecha, LocalDateTime.now(COSTA_RICA)));
        if (minutos < 1) return "ahora";
        if (minutos < 60) return "hace " + minutos + " min";
        long horas = minutos / 60;
        if (horas < 24) return "hace " + horas + " h";
        long dias = horas / 24;
        if (dias < 7) return "hace " + dias + (dias == 1 ? " día" : " días");
        return fecha.toLocalDate().getDayOfMonth() + " "
                + fecha.getMonth().getDisplayName(TextStyle.SHORT, LOCALE_ES).replace(".", "");
    }

    private String dia(LocalDateTime fecha) { return String.format("%02d", fecha.getDayOfMonth()); }
    private String mes(LocalDateTime fecha) {
        return fecha.getMonth().getDisplayName(TextStyle.SHORT, LOCALE_ES).replace(".", "").toUpperCase(LOCALE_ES);
    }
    private String numero(long valor) { return String.format(LOCALE_ES, "%,d", valor); }

    private String monedaCompacta(BigDecimal valor) {
        BigDecimal seguro = valor == null ? BigDecimal.ZERO : valor;
        BigDecimal millon = BigDecimal.valueOf(1_000_000);
        BigDecimal mil = BigDecimal.valueOf(1_000);
        if (seguro.abs().compareTo(millon) >= 0) return "₡" + compacto(seguro.divide(millon, 1, RoundingMode.HALF_UP)) + "M";
        if (seguro.abs().compareTo(mil) >= 0) return "₡" + compacto(seguro.divide(mil, 1, RoundingMode.HALF_UP)) + "K";
        return "₡" + numero(seguro.longValue());
    }

    private String compacto(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString();
    }
}
