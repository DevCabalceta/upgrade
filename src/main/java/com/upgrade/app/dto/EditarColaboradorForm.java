package com.upgrade.app.dto;

import com.upgrade.app.domain.EstadoColaborador;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditarColaboradorForm {

    @NotNull(message = "Falta el identificador del colaborador.")
    private Long id;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 100)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio.")
    @Size(max = 100)
    private String apellido;

    @NotBlank(message = "El usuario es obligatorio.")
    @Pattern(regexp = "^[a-zA-Z0-9._-]{4,50}$", message = "El usuario debe tener entre 4 y 50 caracteres válidos.")
    private String username;

    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "Ingresa un correo válido.")
    @Size(max = 100)
    private String email;

    @Size(max = 30)
    private String telefono;

    @NotNull(message = "Selecciona un rol.")
    private Long rolId;

    @NotNull(message = "Selecciona un cargo.")
    private Long cargoId;

    @NotNull(message = "Selecciona un departamento.")
    private Long departamentoId;

    @NotNull(message = "Selecciona un estado.")
    private EstadoColaborador estadoColaborador;

}
