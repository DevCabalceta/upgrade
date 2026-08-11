package com.upgrade.app.dto;

import com.upgrade.app.domain.EstadoInventario;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
public class InventarioForm {

    private Long id;

    @NotBlank(message = "El código interno es obligatorio.")
    @Size(max = 50, message = "El código no puede superar 50 caracteres.")
    private String codigo;

    @NotBlank(message = "El nombre del equipo es obligatorio.")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres.")
    private String nombre;

    @Size(max = 2000, message = "La descripción no puede superar 2000 caracteres.")
    private String descripcion;

    @Size(max = 100, message = "La serie no puede superar 100 caracteres.")
    private String numeroSerie;

    @Size(max = 100, message = "La marca no puede superar 100 caracteres.")
    private String marca;

    @Size(max = 100, message = "El modelo no puede superar 100 caracteres.")
    private String modelo;

    @Size(max = 100, message = "La bodega no puede superar 100 caracteres.")
    private String bodega;

    @Size(max = 150, message = "La ubicación no puede superar 150 caracteres.")
    private String ubicacion;

    @NotNull(message = "Indica el costo unitario.")
    @DecimalMin(value = "0.00", message = "El costo no puede ser negativo.")
    private BigDecimal valorUnitario = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "El precio de venta no puede ser negativo.")
    private BigDecimal precioVenta;

    @NotNull(message = "Indica la cantidad total.")
    @Min(value = 0, message = "La cantidad total no puede ser negativa.")
    private Integer cantidadTotal = 1;

    @NotNull(message = "Indica la cantidad disponible.")
    @Min(value = 0, message = "La cantidad disponible no puede ser negativa.")
    private Integer cantidadDisponible = 1;

    @NotNull(message = "Selecciona un estado.")
    private EstadoInventario estado = EstadoInventario.EN_BODEGA;

    @NotNull(message = "Selecciona una categoría.")
    private Long categoriaId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaAdquisicion;
}
