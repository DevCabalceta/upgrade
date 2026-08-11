package com.upgrade.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificacionPrestamoForm {

    @NotNull(message = "No fue posible identificar el préstamo.")
    private Long id;

    @NotBlank(message = "Escribe el mensaje del recordatorio.")
    @Size(max = 1000, message = "El mensaje no puede superar 1000 caracteres.")
    private String mensaje = "Hola, te recordamos que el equipo en préstamo se encuentra vencido. Por favor confirma la fecha de devolución.";
}
