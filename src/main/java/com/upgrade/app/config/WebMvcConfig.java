package com.upgrade.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final Path directorioGaleria;
    private final Path directorioMantenimiento;
    private final Path directorioPerfil;

    public WebMvcConfig(
            @Value("${app.upload.gallery-dir:uploads/gallery}") String directorioGaleria,
            @Value("${app.upload.maintenance-dir:uploads/maintenance}") String directorioMantenimiento,
            @Value("${app.upload.profile-dir:uploads/profile}") String directorioPerfil
    ) {
        this.directorioGaleria = Path.of(directorioGaleria).toAbsolutePath().normalize();
        this.directorioMantenimiento = Path.of(directorioMantenimiento).toAbsolutePath().normalize();
        this.directorioPerfil = Path.of(directorioPerfil).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String ubicacion = directorioGaleria.toUri().toString();
        if (!ubicacion.endsWith("/")) {
            ubicacion += "/";
        }
        registry.addResourceHandler("/uploads/gallery/**")
                .addResourceLocations(ubicacion);

        String ubicacionMantenimiento = directorioMantenimiento.toUri().toString();
        if (!ubicacionMantenimiento.endsWith("/")) {
            ubicacionMantenimiento += "/";
        }
        registry.addResourceHandler("/uploads/maintenance/**")
                .addResourceLocations(ubicacionMantenimiento);

        String ubicacionPerfil = directorioPerfil.toUri().toString();
        if (!ubicacionPerfil.endsWith("/")) ubicacionPerfil += "/";
        registry.addResourceHandler("/uploads/profile/**").addResourceLocations(ubicacionPerfil);
    }
}
