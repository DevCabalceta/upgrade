package com.upgrade.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsignarTecnicoMantenimientoForm {

    @NotNull(message = "No fue posible identificar la orden.")
    private Long ordenId;

    @NotNull(message = "Selecciona un técnico.")
    private Long tecnicoId;
}
