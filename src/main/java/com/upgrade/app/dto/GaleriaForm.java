package com.upgrade.app.dto;

import com.upgrade.app.domain.DisenoGaleria;
import com.upgrade.app.domain.FuenteMediaGaleria;
import com.upgrade.app.domain.TipoMediaGaleria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
public class GaleriaForm {

    private Long id;

    @NotNull(message = "Selecciona el tipo de contenido.")
    private TipoMediaGaleria tipo = TipoMediaGaleria.IMAGEN;

    @NotNull(message = "Selecciona el origen del contenido.")
    private FuenteMediaGaleria fuente = FuenteMediaGaleria.ARCHIVO;

    @Size(max = 500, message = "La URL no puede superar 500 caracteres.")
    private String mediaUrl;

    @Size(max = 500, message = "La URL de portada no puede superar 500 caracteres.")
    private String portadaUrl;

    private MultipartFile archivoMedia;
    private MultipartFile archivoPortada;

    @NotBlank(message = "El título es obligatorio.")
    @Size(max = 150, message = "El título no puede superar 150 caracteres.")
    private String titulo;

    @NotNull(message = "Selecciona una categoría.")
    private Long categoriaId;

    @Size(max = 600, message = "La descripción no puede superar 600 caracteres.")
    private String descripcion;

    @NotBlank(message = "El texto alternativo es obligatorio.")
    @Size(max = 200, message = "El texto alternativo no puede superar 200 caracteres.")
    private String textoAlternativo;

    @NotNull(message = "Selecciona un diseño.")
    private DisenoGaleria diseno = DisenoGaleria.ESTANDAR;

    private Boolean publicado = true;
    private Boolean destacado = false;

    @NotNull(message = "La fecha del evento es obligatoria.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaEvento = LocalDate.now();
}
