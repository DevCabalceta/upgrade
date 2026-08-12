package com.upgrade.app.dto;

import com.upgrade.app.domain.EstadoServicio;
import com.upgrade.app.domain.UnidadServicio;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
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

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres.")
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria.")
    @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres.")
    private String descripcion;

    @NotNull(message = "El precio base es obligatorio.")
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo.")
    @Digits(integer = 10, fraction = 2, message = "Usa un importe válido con máximo dos decimales.")
    private BigDecimal precioBase;

    @NotNull(message = "Selecciona una categoría.")
    private Long categoriaId;

    @NotNull(message = "Selecciona una unidad.")
    private UnidadServicio unidad = UnidadServicio.SERVICIO;

    @Size(max = 80, message = "La duración no puede superar 80 caracteres.")
    private String duracionEstimada;

    @NotNull(message = "Selecciona un estado.")
    private EstadoServicio estado = EstadoServicio.ACTIVO;

    @NotNull(message = "Indica la cantidad de equipos.")
    @Min(value = 0, message = "La cantidad de equipos no puede ser negativa.")
    private Integer equiposRequeridos = 0;

    @NotNull(message = "Selecciona un icono.")
    @Size(max = 50, message = "El icono no es válido.")
    private String icono = "sparkles";

    @NotNull(message = "Indica el orden de publicación.")
    @Min(value = 1, message = "El orden debe ser mayor o igual a 1.")
    private Integer orden = 1;

    private Boolean visibleLanding = true;
}
