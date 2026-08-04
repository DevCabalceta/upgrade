package com.upgrade.app.dto;

import com.upgrade.app.domain.CategoriaPregunta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PreguntaFrecuenteForm {

    private Long id;

    @NotBlank(message = "La pregunta es obligatoria.")
    @Size(max = 160, message = "La pregunta no puede superar 160 caracteres.")
    private String pregunta;

    @NotBlank(message = "La respuesta es obligatoria.")
    @Size(max = 600, message = "La respuesta no puede superar 600 caracteres.")
    private String respuesta;

    @NotNull(message = "Selecciona una categoría.")
    private CategoriaPregunta categoria;

    @NotNull(message = "Indica si la pregunta estará publicada.")
    private Boolean activa = true;
}
