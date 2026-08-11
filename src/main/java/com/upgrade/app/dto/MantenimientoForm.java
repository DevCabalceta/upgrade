package com.upgrade.app.dto;

import com.upgrade.app.domain.EstadoMantenimiento;
import com.upgrade.app.domain.PrioridadMantenimiento;
import com.upgrade.app.domain.TipoMantenimiento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class MantenimientoForm {

    private Long id;

    @NotNull(message = "Selecciona el equipo.")
    private Long inventarioId;

    private Long clienteId;

    @NotNull(message = "Selecciona el técnico responsable.")
    private Long tecnicoId;

    @NotNull(message = "Selecciona el tipo de mantenimiento.")
    private TipoMantenimiento tipo;

    @NotNull(message = "Selecciona la prioridad.")
    private PrioridadMantenimiento prioridad;

    @NotNull(message = "Selecciona el estado.")
    private EstadoMantenimiento estado = EstadoMantenimiento.PROGRAMADO;

    @Size(max = 180, message = "La ubicación no puede superar 180 caracteres.")
    private String ubicacion;

    @NotNull(message = "Selecciona la fecha programada.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaProgramada = LocalDate.now();

    @NotNull(message = "Selecciona la hora programada.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime horaProgramada = LocalTime.of(9, 0);

    @NotNull(message = "Indica el costo estimado.")
    @DecimalMin(value = "0.00", message = "El costo no puede ser negativo.")
    @Digits(integer = 10, fraction = 2, message = "El costo tiene un formato inválido.")
    private BigDecimal costoEstimado = BigDecimal.ZERO;

    @NotNull(message = "Indica la duración estimada.")
    @DecimalMin(value = "0.50", message = "La duración mínima es 0.5 horas.")
    @Digits(integer = 4, fraction = 2, message = "La duración tiene un formato inválido.")
    private BigDecimal duracionEstimada = new BigDecimal("2.00");

    @NotBlank(message = "Describe el trabajo que se realizará.")
    @Size(max = 4000, message = "La descripción no puede superar 4000 caracteres.")
    private String descripcion;
}
