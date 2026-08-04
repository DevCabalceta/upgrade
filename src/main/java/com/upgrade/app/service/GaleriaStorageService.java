package com.upgrade.app.service;

import com.upgrade.app.domain.TipoMediaGaleria;
import com.upgrade.app.exception.ArchivoGaleriaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class GaleriaStorageService {

    private static final Set<String> EXTENSIONES_IMAGEN = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> EXTENSIONES_VIDEO = Set.of("mp4", "webm");
    private static final Set<String> MIME_IMAGEN = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> MIME_VIDEO = Set.of("video/mp4", "video/webm");
    private static final long MAX_IMAGEN = 10L * 1024L * 1024L;
    private static final long MAX_VIDEO = 100L * 1024L * 1024L;
    private static final String PREFIJO_PUBLICO = "/uploads/gallery/";

    private final Path directorio;

    public GaleriaStorageService(@Value("${app.upload.gallery-dir:uploads/gallery}") String directorio) {
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
    }

    public ArchivoGuardado guardarMedia(MultipartFile archivo, TipoMediaGaleria tipo) {
        Set<String> extensiones = tipo == TipoMediaGaleria.VIDEO ? EXTENSIONES_VIDEO : EXTENSIONES_IMAGEN;
        Set<String> tiposMime = tipo == TipoMediaGaleria.VIDEO ? MIME_VIDEO : MIME_IMAGEN;
        long maximo = tipo == TipoMediaGaleria.VIDEO ? MAX_VIDEO : MAX_IMAGEN;
        return guardar(archivo, extensiones, tiposMime, maximo, tipo == TipoMediaGaleria.VIDEO ? "video" : "imagen");
    }

    public ArchivoGuardado guardarPortada(MultipartFile archivo) {
        return guardar(archivo, EXTENSIONES_IMAGEN, MIME_IMAGEN, MAX_IMAGEN, "portada");
    }

    public void eliminarSiEsAdministrado(String rutaPublica) {
        if (!esRutaAdministrada(rutaPublica)) {
            return;
        }
        String nombre = rutaPublica.substring(PREFIJO_PUBLICO.length());
        Path destino = directorio.resolve(nombre).normalize();
        if (!destino.startsWith(directorio)) {
            return;
        }
        try {
            Files.deleteIfExists(destino);
        } catch (IOException exception) {
            throw new ArchivoGaleriaException("No fue posible eliminar el archivo anterior.", exception);
        }
    }

    public boolean esRutaAdministrada(String ruta) {
        return ruta != null && ruta.startsWith(PREFIJO_PUBLICO);
    }

    private ArchivoGuardado guardar(
            MultipartFile archivo,
            Set<String> extensionesPermitidas,
            Set<String> tiposMimePermitidos,
            long tamanoMaximo,
            String descripcion
    ) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ArchivoGaleriaException("Selecciona un archivo de " + descripcion + ".");
        }
        if (archivo.getSize() > tamanoMaximo) {
            throw new ArchivoGaleriaException(
                    "El archivo de " + descripcion + " supera el límite de " + (tamanoMaximo / 1024 / 1024) + " MB."
            );
        }

        String original = StringUtils.cleanPath(
                archivo.getOriginalFilename() == null ? "archivo" : archivo.getOriginalFilename()
        );
        String extension = obtenerExtension(original);
        String tipoMime = archivo.getContentType() == null
                ? ""
                : archivo.getContentType().toLowerCase(Locale.ROOT);
        if (!extensionesPermitidas.contains(extension) || !tiposMimePermitidos.contains(tipoMime)) {
            throw new ArchivoGaleriaException(
                    "Formato no permitido. Usa: " + String.join(", ", extensionesPermitidas) + "."
            );
        }

        String nombreSeguro = UUID.randomUUID() + "." + extension;
        Path destino = directorio.resolve(nombreSeguro).normalize();
        if (!destino.startsWith(directorio)) {
            throw new ArchivoGaleriaException("La ruta del archivo no es válida.");
        }

        try {
            Files.createDirectories(directorio);
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return new ArchivoGuardado(PREFIJO_PUBLICO + nombreSeguro, original, archivo.getSize());
        } catch (IOException exception) {
            throw new ArchivoGaleriaException("No fue posible guardar el archivo de " + descripcion + ".", exception);
        }
    }

    private String obtenerExtension(String nombre) {
        int punto = nombre.lastIndexOf('.');
        if (punto < 0 || punto == nombre.length() - 1) {
            return "";
        }
        return nombre.substring(punto + 1).toLowerCase(Locale.ROOT);
    }

    public record ArchivoGuardado(String rutaPublica, String nombreOriginal, long tamano) {
    }
}
