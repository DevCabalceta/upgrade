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

    public WebMvcConfig(
            @Value("${app.upload.gallery-dir:uploads/gallery}") String directorioGaleria,
            @Value("${app.upload.maintenance-dir:uploads/maintenance}") String directorioMantenimiento
    ) {
        this.directorioGaleria = Path.of(directorioGaleria).toAbsolutePath().normalize();
        this.directorioMantenimiento = Path.of(directorioMantenimiento).toAbsolutePath().normalize();
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
    }
}
