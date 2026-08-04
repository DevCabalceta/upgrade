package com.upgrade.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final Path directorioGaleria;

    public WebMvcConfig(@Value("${app.upload.gallery-dir:uploads/gallery}") String directorioGaleria) {
        this.directorioGaleria = Path.of(directorioGaleria).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String ubicacion = directorioGaleria.toUri().toString();
        if (!ubicacion.endsWith("/")) {
            ubicacion += "/";
        }
        registry.addResourceHandler("/uploads/gallery/**")
                .addResourceLocations(ubicacion);
    }
}
