package com.upgrade.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ObservacionMantenimientoForm {

    @NotNull(message = "No fue posible identificar la orden.")
    private Long ordenId;

    @NotBlank(message = "Escribe la observación.")
    @Size(max = 3000, message = "La observación no puede superar 3000 caracteres.")
    private String texto;
}
