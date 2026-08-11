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
public class NuevoColaboradorForm {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres.")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio.")
    @Size(max = 100, message = "El apellido no puede superar 100 caracteres.")
    private String apellido;

    @NotBlank(message = "El nombre de usuario es obligatorio.")
    @Pattern(regexp = "^[a-zA-Z0-9._-]{4,50}$", message = "Usa entre 4 y 50 caracteres: letras, números, punto, guion o guion bajo.")
    private String username;

    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "Ingresa un correo válido.")
    @Size(max = 100, message = "El correo no puede superar 100 caracteres.")
    private String email;

    @Size(max = 30, message = "El teléfono no puede superar 30 caracteres.")
    private String telefono;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Incluye al menos una mayúscula, una minúscula y un número."
    )
    private String password;

    @NotBlank(message = "Confirma la contraseña.")
    private String confirmarPassword;

    @NotNull(message = "Selecciona un rol.")
    private Long rolId;

    @NotNull(message = "Selecciona un cargo.")
    private Long cargoId;

    @NotNull(message = "Selecciona un departamento.")
    private Long departamentoId;

    @NotNull(message = "Selecciona el estado operativo.")
    private EstadoColaborador estadoColaborador = EstadoColaborador.DISPONIBLE;

    private Boolean enviarInvitacion = true;
}
