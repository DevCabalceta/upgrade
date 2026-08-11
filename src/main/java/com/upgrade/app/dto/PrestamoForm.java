package com.upgrade.app.dto;

import com.upgrade.app.domain.CondicionEquipo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class PrestamoForm {

    @NotNull(message = "Selecciona un equipo.")
    private Long inventarioId;

    @NotNull(message = "Indica la cantidad.")
    @Min(value = 1, message = "La cantidad mínima es 1.")
    private Integer cantidad = 1;

    @NotNull(message = "Selecciona un cliente.")
    private Long clienteId;

    @Email(message = "Ingresa un correo válido.")
    @Size(max = 120, message = "El correo no puede superar 120 caracteres.")
    private String correoContacto;

    @NotNull(message = "Selecciona un responsable interno.")
    private Long responsableId;

    @NotNull(message = "Selecciona el estado de salida.")
    private CondicionEquipo condicionSalida = CondicionEquipo.OPTIMO;

    @NotNull(message = "Selecciona la fecha de salida.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaSalida = LocalDate.now();

    @NotNull(message = "Selecciona la devolución estimada.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaDevolucionEstimada = LocalDate.now().plusDays(7);

    @Size(max = 2000, message = "Las observaciones no pueden superar 2000 caracteres.")
    private String observaciones;
}
