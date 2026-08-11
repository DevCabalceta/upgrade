package com.upgrade.app.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ServicioForm {

    private Long id;

    @NotBlank(message = "El nombre del servicio es obligatorio.")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres.")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede superar 255 caracteres.")
    private String descripcion;

    @NotNull(message = "Indica el precio base del servicio.")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio base no puede ser negativo.")
    @Digits(integer = 10, fraction = 2, message = "Ingresa un precio válido (máximo 2 decimales).")
    private BigDecimal precioBase;

    @NotNull(message = "Indica el estado del servicio.")
    private Boolean activo = true;

}