package com.upgrade.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EliminarColaboradorForm {

    @NotNull(message = "Falta el colaborador.")
    private Long id;

    @NotBlank(message = "Escribe ELIMINAR para confirmar.")
    private String confirmacion;
}
