package com.upgrade.app.service;

import com.upgrade.app.exception.ArchivoPerfilException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class PerfilStorageService {
    private static final long MAX_BYTES = 3L * 1024 * 1024;
    private static final Set<String> TIPOS = Set.of("image/jpeg", "image/png", "image/webp");
    private final Path directorio;

    public PerfilStorageService(@Value("${app.upload.profile-dir:uploads/profile}") String ruta) {
        this.directorio = Path.of(ruta).toAbsolutePath().normalize();
    }

    public String guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) return null;
        if (archivo.getSize() > MAX_BYTES) throw new ArchivoPerfilException("La fotografía no puede superar 3 MB.");
        String tipo = archivo.getContentType() == null ? "" : archivo.getContentType().toLowerCase(Locale.ROOT);
        if (!TIPOS.contains(tipo)) throw new ArchivoPerfilException("La fotografía debe ser JPG, PNG o WebP.");
        String extension = switch (tipo) { case "image/png" -> ".png"; case "image/webp" -> ".webp"; default -> ".jpg"; };
        try {
            Files.createDirectories(directorio);
            String nombre = UUID.randomUUID() + extension;
            Path destino = directorio.resolve(nombre).normalize();
            if (!destino.startsWith(directorio)) throw new ArchivoPerfilException("Nombre de archivo inválido.");
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/profile/" + nombre;
        } catch (IOException e) {
            throw new ArchivoPerfilException("No fue posible guardar la fotografía.", e);
        }
    }

    public void eliminar(String rutaPublica) {
        if (rutaPublica == null || !rutaPublica.startsWith("/uploads/profile/")) return;
        try {
            Path archivo = directorio.resolve(rutaPublica.substring("/uploads/profile/".length())).normalize();
            if (archivo.startsWith(directorio)) Files.deleteIfExists(archivo);
        } catch (IOException ignored) {
            // La actualización de la cuenta no debe fallar por una fotografía antigua huérfana.
        }
    }
}
