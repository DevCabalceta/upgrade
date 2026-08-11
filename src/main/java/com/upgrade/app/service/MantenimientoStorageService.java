package com.upgrade.app.service;

import com.upgrade.app.exception.ArchivoMantenimientoException;
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
public class MantenimientoStorageService {

    private static final Set<String> EXTENSIONES = Set.of("jpg", "jpeg", "png", "pdf");
    private static final Set<String> TIPOS_MIME = Set.of("image/jpeg", "image/png", "application/pdf");
    private static final long MAXIMO = 20L * 1024L * 1024L;
    private static final String PREFIJO_PUBLICO = "/uploads/maintenance/";

    private final Path directorio;

    public MantenimientoStorageService(
            @Value("${app.upload.maintenance-dir:uploads/maintenance}") String directorio
    ) {
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
    }

    public ArchivoMantenimientoGuardado guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ArchivoMantenimientoException("Selecciona al menos un archivo.");
        }
        if (archivo.getSize() > MAXIMO) {
            throw new ArchivoMantenimientoException("Cada evidencia puede pesar como máximo 20 MB.");
        }

        String original = StringUtils.cleanPath(
                archivo.getOriginalFilename() == null ? "evidencia" : archivo.getOriginalFilename()
        );
        String extension = extension(original);
        String tipoMime = archivo.getContentType() == null
                ? ""
                : archivo.getContentType().toLowerCase(Locale.ROOT);
        if (!EXTENSIONES.contains(extension) || !TIPOS_MIME.contains(tipoMime)) {
            throw new ArchivoMantenimientoException("Formato no permitido. Usa JPG, PNG o PDF.");
        }

        String nombreSeguro = UUID.randomUUID() + "." + extension;
        Path destino = directorio.resolve(nombreSeguro).normalize();
        if (!destino.startsWith(directorio)) {
            throw new ArchivoMantenimientoException("La ruta del archivo no es válida.");
        }

        try {
            Files.createDirectories(directorio);
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return new ArchivoMantenimientoGuardado(
                    PREFIJO_PUBLICO + nombreSeguro,
                    original,
                    tipoMime,
                    archivo.getSize()
            );
        } catch (IOException exception) {
            throw new ArchivoMantenimientoException("No fue posible guardar la evidencia.", exception);
        }
    }

    public void eliminar(String rutaPublica) {
        if (rutaPublica == null || !rutaPublica.startsWith(PREFIJO_PUBLICO)) {
            return;
        }
        Path archivo = directorio.resolve(rutaPublica.substring(PREFIJO_PUBLICO.length())).normalize();
        if (!archivo.startsWith(directorio)) {
            return;
        }
        try {
            Files.deleteIfExists(archivo);
        } catch (IOException ignored) {
            // La evidencia en base de datos conserva la trazabilidad aunque falle la limpieza.
        }
    }

    private String extension(String nombre) {
        int punto = nombre.lastIndexOf('.');
        return punto < 0 || punto == nombre.length() - 1
                ? ""
                : nombre.substring(punto + 1).toLowerCase(Locale.ROOT);
    }

    public record ArchivoMantenimientoGuardado(
            String rutaPublica,
            String nombreOriginal,
            String tipoMime,
            long tamano
    ) {
    }
}
