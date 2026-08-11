package com.upgrade.app.dto;

import com.upgrade.app.domain.CondicionEquipo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class DevolucionPrestamoForm {

    @NotNull(message = "No fue posible identificar el préstamo.")
    private Long id;

    @NotNull(message = "Selecciona la fecha de devolución.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaDevolucion = LocalDate.now();

    @NotNull(message = "Selecciona el estado del equipo al regresar.")
    private CondicionEquipo condicionDevolucion = CondicionEquipo.OPTIMO;

    @Size(max = 2000, message = "Las observaciones no pueden superar 2000 caracteres.")
    private String observaciones;
}
