package com.upgrade.app.service;

import com.upgrade.app.domain.CategoriaServicio;
import com.upgrade.app.domain.EstadoServicio;
import com.upgrade.app.domain.Servicio;
import com.upgrade.app.dto.ServicioForm;
import com.upgrade.app.exception.ServicioNoEncontradoException;
import com.upgrade.app.repository.CategoriaServicioRepository;
import com.upgrade.app.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final CategoriaServicioRepository categoriaRepository;

    public Page<Servicio> listar(
            String buscar,
            EstadoServicio estado,
            Long categoriaId,
            int pagina,
            int tamano
    ) {
        PageRequest pageable = PageRequest.of(
                Math.max(pagina, 0),
                Math.min(Math.max(tamano, 6), 50),
                Sort.by(Sort.Order.asc("orden"), Sort.Order.asc("id"))
        );
        return servicioRepository.buscar(normalizarOpcional(buscar), estado, categoriaId, pageable);
    }

    public List<Servicio> listarPublicados() {
        return servicioRepository
                .findAllByEliminadoFalseAndEstadoAndVisibleLandingTrueOrderByOrdenAscIdAsc(EstadoServicio.ACTIVO);
    }

    public List<CategoriaServicio> listarCategorias() {
        return categoriaRepository.findAllByActivaTrueOrderByNombreAsc();
    }

    public boolean categoriaDisponible(Long categoriaId) {
        return categoriaId != null
                && categoriaRepository.findById(categoriaId)
                .map(CategoriaServicio::getActiva)
                .orElse(false);
    }

    public Servicio obtenerPorId(Long id) {
        return servicioRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new ServicioNoEncontradoException(id));
    }

    public int siguienteOrden() {
        return servicioRepository.obtenerOrdenMaximo() + 1;
    }

    public boolean nombreOcupado(String nombre, Long idActual) {
        String normalizado = limpiar(nombre);
        if (normalizado.isEmpty()) {
            return false;
        }
        return idActual == null
                ? servicioRepository.existsByNombreIgnoreCase(normalizado)
                : servicioRepository.existsByNombreIgnoreCaseAndIdNot(normalizado, idActual);
    }

    public EstadisticasServicio obtenerEstadisticas() {
        return new EstadisticasServicio(
                servicioRepository.countByEliminadoFalse(),
                servicioRepository.countByEliminadoFalseAndEstado(EstadoServicio.ACTIVO),
                servicioRepository.countByEliminadoFalseAndEstado(EstadoServicio.BORRADOR),
                servicioRepository.countByEliminadoFalseAndEstado(EstadoServicio.INACTIVO),
                servicioRepository.countByEliminadoFalseAndEstadoAndVisibleLandingTrue(EstadoServicio.ACTIVO)
        );
    }

    @Transactional
    public Servicio crear(ServicioForm form) {
        int posicion = limitar(form.getOrden(), 1, siguienteOrden());
        desplazarParaInsertar(posicion);

        Servicio servicio = new Servicio();
        copiarFormulario(form, servicio);
        servicio.setOrden(posicion);
        servicio.setServiciosYtd(0);
        servicio.setEliminado(false);
        return servicioRepository.save(servicio);
    }

    @Transactional
    public Servicio actualizar(ServicioForm form) {
        Servicio servicio = obtenerPorId(form.getId());
        int ordenAnterior = servicio.getOrden();
        int ordenNuevo = limitar(form.getOrden(), 1, Math.max(servicioRepository.obtenerOrdenMaximo(), 1));
        reajustarOrdenEdicion(servicio, ordenAnterior, ordenNuevo);
        copiarFormulario(form, servicio);
        servicio.setOrden(ordenNuevo);
        return servicioRepository.save(servicio);
    }

    @Transactional
    public Servicio cambiarPublicacion(Long id) {
        Servicio servicio = obtenerPorId(id);
        servicio.setVisibleLanding(!Boolean.TRUE.equals(servicio.getVisibleLanding()));
        return servicioRepository.save(servicio);
    }

    @Transactional
    public void moverArriba(Long id) {
        Servicio actual = obtenerPorId(id);
        servicioRepository.findFirstByEliminadoFalseAndOrdenLessThanOrderByOrdenDesc(actual.getOrden())
                .ifPresent(anterior -> intercambiarOrden(actual, anterior));
    }

    @Transactional
    public void moverAbajo(Long id) {
        Servicio actual = obtenerPorId(id);
        servicioRepository.findFirstByEliminadoFalseAndOrdenGreaterThanOrderByOrdenAsc(actual.getOrden())
                .ifPresent(siguiente -> intercambiarOrden(actual, siguiente));
    }

    @Transactional
    public void eliminarLogicamente(Long id) {
        Servicio servicio = obtenerPorId(id);
        servicio.setEliminado(true);
        servicio.setVisibleLanding(false);
        servicio.setActivo(false);
        servicio.setEstado(EstadoServicio.INACTIVO);
        servicioRepository.saveAndFlush(servicio);
        normalizarOrdenes();
    }

    private void copiarFormulario(ServicioForm form, Servicio servicio) {
        servicio.setNombre(limpiar(form.getNombre()));
        servicio.setDescripcion(limpiar(form.getDescripcion()));
        servicio.setPrecioBase(form.getPrecioBase());
        servicio.setCategoria(categoriaRepository.findById(form.getCategoriaId())
                .filter(categoria -> Boolean.TRUE.equals(categoria.getActiva()))
                .orElseThrow(() -> new IllegalArgumentException("La categoría seleccionada no existe o está inactiva.")));
        servicio.setUnidad(form.getUnidad());
        servicio.setDuracionEstimada(normalizarOpcional(form.getDuracionEstimada()));
        servicio.setEstado(form.getEstado());
        servicio.setActivo(form.getEstado() == EstadoServicio.ACTIVO);
        servicio.setEquiposRequeridos(form.getEquiposRequeridos());
        servicio.setIcono(limpiar(form.getIcono()));
        servicio.setVisibleLanding(Boolean.TRUE.equals(form.getVisibleLanding()));
    }

    private void desplazarParaInsertar(int posicion) {
        List<Servicio> servicios = servicioRepository.findAllByEliminadoFalseOrderByOrdenAscIdAsc();
        servicios.stream()
                .filter(servicio -> servicio.getOrden() >= posicion)
                .forEach(servicio -> servicio.setOrden(servicio.getOrden() + 1));
        servicioRepository.saveAll(servicios);
    }

    private void reajustarOrdenEdicion(Servicio actual, int anterior, int nuevo) {
        if (anterior == nuevo) {
            return;
        }
        List<Servicio> servicios = servicioRepository.findAllByEliminadoFalseOrderByOrdenAscIdAsc();
        for (Servicio servicio : servicios) {
            if (servicio.getId().equals(actual.getId())) {
                continue;
            }
            if (nuevo < anterior && servicio.getOrden() >= nuevo && servicio.getOrden() < anterior) {
                servicio.setOrden(servicio.getOrden() + 1);
            } else if (nuevo > anterior && servicio.getOrden() <= nuevo && servicio.getOrden() > anterior) {
                servicio.setOrden(servicio.getOrden() - 1);
            }
        }
        servicioRepository.saveAll(servicios);
    }

    private void intercambiarOrden(Servicio primero, Servicio segundo) {
        int temporal = primero.getOrden();
        primero.setOrden(segundo.getOrden());
        segundo.setOrden(temporal);
        servicioRepository.saveAll(List.of(primero, segundo));
    }

    private void normalizarOrdenes() {
        List<Servicio> servicios = servicioRepository.findAllByEliminadoFalseOrderByOrdenAscIdAsc();
        for (int indice = 0; indice < servicios.size(); indice++) {
            servicios.get(indice).setOrden(indice + 1);
        }
        servicioRepository.saveAll(servicios);
    }

    private int limitar(Integer valor, int minimo, int maximo) {
        int seguro = valor == null ? maximo : valor;
        return Math.max(minimo, Math.min(seguro, maximo));
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String normalizarOpcional(String valor) {
        String limpio = limpiar(valor);
        return limpio.isEmpty() ? null : limpio;
    }

    public record EstadisticasServicio(
            long total,
            long activos,
            long borradores,
            long inactivos,
            long publicados
    ) {
    }
}
