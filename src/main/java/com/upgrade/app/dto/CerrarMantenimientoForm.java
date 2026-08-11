package com.upgrade.app.dto;

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

@Getter
@Setter
public class CerrarMantenimientoForm {

    @NotNull(message = "No fue posible identificar la orden.")
    private Long ordenId;

    @NotBlank(message = "Escribe el resumen del cierre.")
    @Size(max = 4000, message = "El resumen no puede superar 4000 caracteres.")
    private String resumen;

    @NotNull(message = "Indica el tiempo real utilizado.")
    @DecimalMin(value = "0.50", message = "El tiempo mínimo es 0.5 horas.")
    @Digits(integer = 4, fraction = 2, message = "El tiempo tiene un formato inválido.")
    private BigDecimal horasReales = new BigDecimal("2.00");

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate proximaRevision;
}
