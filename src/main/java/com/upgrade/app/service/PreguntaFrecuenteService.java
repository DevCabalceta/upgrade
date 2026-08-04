package com.upgrade.app.service;

import com.upgrade.app.domain.CategoriaPregunta;
import com.upgrade.app.domain.PreguntaFrecuente;
import com.upgrade.app.dto.PreguntaFrecuenteForm;
import com.upgrade.app.exception.PreguntaFrecuenteNoEncontradaException;
import com.upgrade.app.repository.PreguntaFrecuenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PreguntaFrecuenteService {

    private final PreguntaFrecuenteRepository preguntaRepository;

    public List<PreguntaFrecuente> listar(String buscar, Boolean activa, CategoriaPregunta categoria) {
        return preguntaRepository.buscar(limpiarOpcional(buscar), activa, categoria);
    }

    public List<PreguntaFrecuente> listarPublicadas() {
        return preguntaRepository.findAllByActivaTrueOrderByOrdenAscIdAsc();
    }

    public PreguntaFrecuente obtenerPorId(Long id) {
        return preguntaRepository.findById(id)
                .orElseThrow(() -> new PreguntaFrecuenteNoEncontradaException(id));
    }

    public boolean preguntaOcupada(String pregunta, Long idActual) {
        String normalizada = limpiar(pregunta);
        return idActual == null
                ? preguntaRepository.existsByPreguntaIgnoreCase(normalizada)
                : preguntaRepository.existsByPreguntaIgnoreCaseAndIdNot(normalizada, idActual);
    }

    public Map<String, Long> obtenerEstadisticas() {
        Map<String, Long> estadisticas = new LinkedHashMap<>();
        estadisticas.put("total", preguntaRepository.count());
        estadisticas.put("publicadas", preguntaRepository.countByActivaTrue());
        estadisticas.put("ocultas", preguntaRepository.countByActivaFalse());
        estadisticas.put("categorias", preguntaRepository.contarCategoriasUtilizadas());
        return estadisticas;
    }

    @Transactional
    public PreguntaFrecuente crear(PreguntaFrecuenteForm form) {
        PreguntaFrecuente pregunta = new PreguntaFrecuente();
        copiarFormulario(form, pregunta);
        pregunta.setOrden(preguntaRepository.obtenerOrdenMaximo() + 1);
        return preguntaRepository.save(pregunta);
    }

    @Transactional
    public PreguntaFrecuente actualizar(PreguntaFrecuenteForm form) {
        PreguntaFrecuente pregunta = obtenerPorId(form.getId());
        copiarFormulario(form, pregunta);
        return preguntaRepository.save(pregunta);
    }

    @Transactional
    public PreguntaFrecuente cambiarPublicacion(Long id) {
        PreguntaFrecuente pregunta = obtenerPorId(id);
        pregunta.setActiva(!Boolean.TRUE.equals(pregunta.getActiva()));
        return preguntaRepository.save(pregunta);
    }

    @Transactional
    public void moverArriba(Long id) {
        PreguntaFrecuente actual = obtenerPorId(id);
        preguntaRepository.findFirstByOrdenLessThanOrderByOrdenDesc(actual.getOrden())
                .ifPresent(anterior -> intercambiarOrden(actual, anterior));
    }

    @Transactional
    public void moverAbajo(Long id) {
        PreguntaFrecuente actual = obtenerPorId(id);
        preguntaRepository.findFirstByOrdenGreaterThanOrderByOrdenAsc(actual.getOrden())
                .ifPresent(siguiente -> intercambiarOrden(actual, siguiente));
    }

    @Transactional
    public void eliminar(Long id) {
        PreguntaFrecuente pregunta = obtenerPorId(id);
        preguntaRepository.delete(pregunta);
        preguntaRepository.flush();
        normalizarOrdenes();
    }

    private void intercambiarOrden(PreguntaFrecuente primera, PreguntaFrecuente segunda) {
        Integer ordenTemporal = primera.getOrden();
        primera.setOrden(segunda.getOrden());
        segunda.setOrden(ordenTemporal);
        preguntaRepository.saveAll(List.of(primera, segunda));
    }

    private void normalizarOrdenes() {
        List<PreguntaFrecuente> preguntas = preguntaRepository.findAllByOrderByOrdenAscIdAsc();
        for (int indice = 0; indice < preguntas.size(); indice++) {
            preguntas.get(indice).setOrden(indice + 1);
        }
        preguntaRepository.saveAll(preguntas);
    }

    private void copiarFormulario(PreguntaFrecuenteForm form, PreguntaFrecuente pregunta) {
        pregunta.setPregunta(limpiar(form.getPregunta()));
        pregunta.setRespuesta(limpiar(form.getRespuesta()));
        pregunta.setCategoria(form.getCategoria());
        pregunta.setActiva(Boolean.TRUE.equals(form.getActiva()));
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String limpiarOpcional(String valor) {
        String limpio = limpiar(valor);
        return limpio.isEmpty() ? null : limpio;
    }
}
