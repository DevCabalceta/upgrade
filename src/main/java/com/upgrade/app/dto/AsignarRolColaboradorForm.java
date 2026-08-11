package com.upgrade.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsignarRolColaboradorForm {

    @NotNull(message = "Falta el colaborador.")
    private Long colaboradorId;

    @NotNull(message = "Selecciona un rol.")
    private Long rolId;
}
