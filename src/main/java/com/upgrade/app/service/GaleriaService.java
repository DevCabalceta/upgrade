package com.upgrade.app.service;

import com.upgrade.app.domain.CategoriaGaleria;
import com.upgrade.app.domain.DisenoGaleria;
import com.upgrade.app.domain.ElementoGaleria;
import com.upgrade.app.domain.FuenteMediaGaleria;
import com.upgrade.app.domain.TipoMediaGaleria;
import com.upgrade.app.dto.GaleriaForm;
import com.upgrade.app.exception.ArchivoGaleriaException;
import com.upgrade.app.exception.CategoriaGaleriaNoEncontradaException;
import com.upgrade.app.exception.ElementoGaleriaNoEncontradoException;
import com.upgrade.app.repository.CategoriaGaleriaRepository;
import com.upgrade.app.repository.ElementoGaleriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GaleriaService {

    private final ElementoGaleriaRepository elementoRepository;
    private final CategoriaGaleriaRepository categoriaRepository;
    private final GaleriaStorageService storageService;

    public List<ElementoGaleria> listar(
            String buscar,
            TipoMediaGaleria tipo,
            Boolean publicado,
            Long categoriaId,
            String criterioOrden
    ) {
        return elementoRepository.buscar(
                normalizarTextoNullable(buscar),
                tipo,
                publicado,
                categoriaId,
                crearOrden(criterioOrden)
        );
    }

    public List<ElementoGaleria> listarPublicados() {
        return elementoRepository.findAllByPublicadoTrueOrderByOrdenAscIdAsc();
    }

    public List<CategoriaGaleria> listarCategorias() {
        return categoriaRepository.findAllByActivaTrueOrderByNombreAsc();
    }

    public ElementoGaleria obtenerPorId(Long id) {
        return elementoRepository.findById(id)
                .orElseThrow(() -> new ElementoGaleriaNoEncontradoException(id));
    }

    public boolean tituloOcupado(String titulo, Long idActual) {
        if (titulo == null || titulo.isBlank()) {
            return false;
        }
        String limpio = titulo.trim();
        return idActual == null
                ? elementoRepository.existsByTituloIgnoreCase(limpio)
                : elementoRepository.existsByTituloIgnoreCaseAndIdNot(limpio, idActual);
    }

    @Transactional
    public ElementoGaleria crear(GaleriaForm form) {
        validarFormularioCondicional(form, true, null);
        CategoriaGaleria categoria = obtenerCategoria(form.getCategoriaId());
        MediaResuelta media = null;
        String portada = null;
        try {
            media = resolverMediaNueva(form);
            portada = resolverPortadaNueva(form);
            ElementoGaleria elemento = ElementoGaleria.builder()
                    .tipo(form.getTipo())
                    .fuente(form.getFuente())
                    .rutaMedia(media.ruta())
                    .rutaPortada(form.getTipo() == TipoMediaGaleria.VIDEO ? portada : null)
                    .titulo(form.getTitulo().trim())
                    .categoria(categoria)
                    .descripcion(normalizarTextoNullable(form.getDescripcion()))
                    .textoAlternativo(form.getTextoAlternativo().trim())
                    .diseno(form.getDiseno() == null ? DisenoGaleria.ESTANDAR : form.getDiseno())
                    .publicado(Boolean.TRUE.equals(form.getPublicado()))
                    .destacado(Boolean.TRUE.equals(form.getDestacado()))
                    .fechaEvento(form.getFechaEvento() == null ? LocalDate.now() : form.getFechaEvento())
                    .orden(elementoRepository.obtenerOrdenMaximo() + 1)
                    .nombreArchivoOriginal(media.nombreOriginal())
                    .tamanoBytes(media.tamano())
                    .build();
            return elementoRepository.save(elemento);
        } catch (RuntimeException exception) {
            if (media != null) {
                storageService.eliminarSiEsAdministrado(media.ruta());
            }
            storageService.eliminarSiEsAdministrado(portada);
            throw exception;
        }
    }

    @Transactional
    public ElementoGaleria actualizar(GaleriaForm form) {
        ElementoGaleria elemento = obtenerPorId(form.getId());
        validarFormularioCondicional(form, false, elemento);

        String mediaAnterior = elemento.getRutaMedia();
        String portadaAnterior = elemento.getRutaPortada();
        MediaResuelta media = resolverMediaEdicion(form, elemento);
        String portada = resolverPortadaEdicion(form, elemento);

        elemento.setTipo(form.getTipo());
        elemento.setFuente(form.getFuente());
        elemento.setRutaMedia(media.ruta());
        elemento.setRutaPortada(form.getTipo() == TipoMediaGaleria.VIDEO ? portada : null);
        elemento.setTitulo(form.getTitulo().trim());
        elemento.setCategoria(obtenerCategoria(form.getCategoriaId()));
        elemento.setDescripcion(normalizarTextoNullable(form.getDescripcion()));
        elemento.setTextoAlternativo(form.getTextoAlternativo().trim());
        elemento.setDiseno(form.getDiseno());
        elemento.setPublicado(Boolean.TRUE.equals(form.getPublicado()));
        elemento.setDestacado(Boolean.TRUE.equals(form.getDestacado()));
        elemento.setFechaEvento(form.getFechaEvento());
        elemento.setNombreArchivoOriginal(media.nombreOriginal());
        elemento.setTamanoBytes(media.tamano());

        ElementoGaleria guardado = elementoRepository.save(elemento);
        eliminarSiFueReemplazado(mediaAnterior, guardado.getRutaMedia());
        eliminarSiFueReemplazado(portadaAnterior, guardado.getRutaPortada());
        return guardado;
    }

    @Transactional
    public ElementoGaleria cambiarPublicacion(Long id) {
        ElementoGaleria elemento = obtenerPorId(id);
        elemento.setPublicado(!Boolean.TRUE.equals(elemento.getPublicado()));
        return elementoRepository.save(elemento);
    }

    @Transactional
    public ElementoGaleria cambiarDestacado(Long id) {
        ElementoGaleria elemento = obtenerPorId(id);
        elemento.setDestacado(!Boolean.TRUE.equals(elemento.getDestacado()));
        return elementoRepository.save(elemento);
    }

    @Transactional
    public void moverArriba(Long id) {
        ElementoGaleria actual = obtenerPorId(id);
        elementoRepository.findFirstByOrdenLessThanOrderByOrdenDescIdDesc(actual.getOrden())
                .ifPresent(anterior -> intercambiarOrden(actual, anterior));
    }

    @Transactional
    public void moverAbajo(Long id) {
        ElementoGaleria actual = obtenerPorId(id);
        elementoRepository.findFirstByOrdenGreaterThanOrderByOrdenAscIdAsc(actual.getOrden())
                .ifPresent(siguiente -> intercambiarOrden(actual, siguiente));
    }

    @Transactional
    public void eliminar(Long id) {
        ElementoGaleria elemento = obtenerPorId(id);
        Integer orden = elemento.getOrden();
        String media = elemento.getRutaMedia();
        String portada = elemento.getRutaPortada();
        elementoRepository.delete(elemento);
        elementoRepository.flush();
        elementoRepository.cerrarHuecoDeOrden(orden);
        storageService.eliminarSiEsAdministrado(media);
        storageService.eliminarSiEsAdministrado(portada);
    }

    @Transactional
    public CategoriaGaleria crearCategoria(String nombre) {
        String limpio = normalizarTextoNullable(nombre);
        if (limpio == null || limpio.length() < 2 || limpio.length() > 100) {
            throw new IllegalArgumentException("La categoría debe tener entre 2 y 100 caracteres.");
        }
        if (categoriaRepository.existsByNombreIgnoreCase(limpio)) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre.");
        }
        return categoriaRepository.save(CategoriaGaleria.builder().nombre(limpio).activa(true).build());
    }

    @Transactional
    public void eliminarCategoria(Long id) {
        CategoriaGaleria categoria = obtenerCategoria(id);
        if (elementoRepository.countByCategoriaId(id) > 0) {
            throw new IllegalStateException("No puedes eliminar una categoría que tiene elementos asociados.");
        }
        categoriaRepository.delete(categoria);
    }

    public Map<Long, Long> obtenerConteosPorCategoria() {
        Map<Long, Long> conteos = new LinkedHashMap<>();
        listarCategorias().forEach(categoria ->
                conteos.put(categoria.getId(), elementoRepository.countByCategoriaId(categoria.getId()))
        );
        return conteos;
    }

    public EstadisticasGaleria obtenerEstadisticas() {
        return new EstadisticasGaleria(
                elementoRepository.count(),
                elementoRepository.countByPublicadoTrue(),
                elementoRepository.countByTipo(TipoMediaGaleria.IMAGEN),
                elementoRepository.countByTipo(TipoMediaGaleria.VIDEO)
        );
    }

    public void validarFormularioCondicional(GaleriaForm form, boolean creando, ElementoGaleria actual) {
        if (form.getFuente() == FuenteMediaGaleria.URL) {
            validarUrl(form.getMediaUrl(), "La URL del contenido es obligatoria.");
        } else if (form.getFuente() == FuenteMediaGaleria.ARCHIVO) {
            boolean conservaArchivo = !creando
                    && actual != null
                    && actual.getFuente() == FuenteMediaGaleria.ARCHIVO
                    && actual.getTipo() == form.getTipo()
                    && archivoVacio(form.getArchivoMedia());
            if (!conservaArchivo && archivoVacio(form.getArchivoMedia())) {
                throw new ArchivoGaleriaException("Selecciona el archivo principal.");
            }
        }
        if (form.getTipo() == TipoMediaGaleria.VIDEO
                && form.getPortadaUrl() != null
                && !form.getPortadaUrl().isBlank()) {
            validarUrl(form.getPortadaUrl(), "La URL de portada no es válida.");
        }
    }

    private MediaResuelta resolverMediaNueva(GaleriaForm form) {
        if (form.getFuente() == FuenteMediaGaleria.URL) {
            return new MediaResuelta(form.getMediaUrl().trim(), null, null);
        }
        GaleriaStorageService.ArchivoGuardado archivo = storageService.guardarMedia(form.getArchivoMedia(), form.getTipo());
        return new MediaResuelta(archivo.rutaPublica(), archivo.nombreOriginal(), archivo.tamano());
    }

    private MediaResuelta resolverMediaEdicion(GaleriaForm form, ElementoGaleria actual) {
        if (form.getFuente() == FuenteMediaGaleria.URL) {
            return new MediaResuelta(form.getMediaUrl().trim(), null, null);
        }
        if (!archivoVacio(form.getArchivoMedia())) {
            GaleriaStorageService.ArchivoGuardado archivo = storageService.guardarMedia(form.getArchivoMedia(), form.getTipo());
            return new MediaResuelta(archivo.rutaPublica(), archivo.nombreOriginal(), archivo.tamano());
        }
        return new MediaResuelta(actual.getRutaMedia(), actual.getNombreArchivoOriginal(), actual.getTamanoBytes());
    }

    private String resolverPortadaNueva(GaleriaForm form) {
        if (form.getTipo() != TipoMediaGaleria.VIDEO) {
            return null;
        }
        if (!archivoVacio(form.getArchivoPortada())) {
            return storageService.guardarPortada(form.getArchivoPortada()).rutaPublica();
        }
        return normalizarUrlOpcional(form.getPortadaUrl());
    }

    private String resolverPortadaEdicion(GaleriaForm form, ElementoGaleria actual) {
        if (form.getTipo() != TipoMediaGaleria.VIDEO) {
            return null;
        }
        if (!archivoVacio(form.getArchivoPortada())) {
            return storageService.guardarPortada(form.getArchivoPortada()).rutaPublica();
        }
        String url = normalizarUrlOpcional(form.getPortadaUrl());
        return url != null ? url : actual.getRutaPortada();
    }

    private CategoriaGaleria obtenerCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaGaleriaNoEncontradaException(id));
    }

    private void intercambiarOrden(ElementoGaleria primero, ElementoGaleria segundo) {
        Integer temporal = primero.getOrden();
        primero.setOrden(segundo.getOrden());
        segundo.setOrden(temporal);
        elementoRepository.save(segundo);
        elementoRepository.save(primero);
    }

    private void eliminarSiFueReemplazado(String anterior, String nuevo) {
        if (anterior != null && !anterior.equals(nuevo)) {
            storageService.eliminarSiEsAdministrado(anterior);
        }
    }

    private Sort crearOrden(String criterio) {
        if ("recientes".equalsIgnoreCase(criterio)) {
            return Sort.by(Sort.Order.desc("fechaEvento"), Sort.Order.desc("id"));
        }
        if ("antiguos".equalsIgnoreCase(criterio)) {
            return Sort.by(Sort.Order.asc("fechaEvento"), Sort.Order.asc("id"));
        }
        if ("titulo".equalsIgnoreCase(criterio)) {
            return Sort.by(Sort.Order.asc("titulo").ignoreCase());
        }
        return Sort.by(Sort.Order.asc("orden"), Sort.Order.asc("id"));
    }

    private void validarUrl(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new ArchivoGaleriaException(mensaje);
        }
        try {
            URI uri = new URI(valor.trim());
            if (uri.getHost() == null || !("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))) {
                throw new ArchivoGaleriaException(mensaje);
            }
        } catch (URISyntaxException exception) {
            throw new ArchivoGaleriaException(mensaje);
        }
    }

    private String normalizarUrlOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        validarUrl(valor, "La URL de portada no es válida.");
        return valor.trim();
    }

    private boolean archivoVacio(MultipartFile archivo) {
        return archivo == null || archivo.isEmpty();
    }

    private String normalizarTextoNullable(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    public record EstadisticasGaleria(long total, long publicadas, long imagenes, long videos) {
    }

    private record MediaResuelta(String ruta, String nombreOriginal, Long tamano) {
    }
}
