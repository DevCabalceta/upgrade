package com.upgrade.app.dto;

import com.upgrade.app.domain.EstadoInventario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambioEstadoInventarioForm {

    @NotNull(message = "No fue posible identificar el equipo.")
    private Long id;

    @NotNull(message = "Selecciona el nuevo estado.")
    private EstadoInventario nuevoEstado;

    @NotBlank(message = "El motivo del cambio es obligatorio.")
    @Size(max = 220, message = "El motivo no puede superar 220 caracteres.")
    private String motivo;
}
