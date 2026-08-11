package com.upgrade.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EliminarMantenimientoForm {

    @NotNull(message = "No fue posible identificar la orden.")
    private Long ordenId;

    @NotBlank(message = "Escribe ELIMINAR para confirmar.")
    private String confirmacion;
}
