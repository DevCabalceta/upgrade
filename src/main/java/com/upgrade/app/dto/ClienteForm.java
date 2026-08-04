package com.upgrade.app.dto;

import com.upgrade.app.domain.TipoCliente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClienteForm {

    private Long id;

    @NotNull(message = "Selecciona el tipo de cliente.")
    private TipoCliente tipoCliente = TipoCliente.EMPRESA;

    @NotBlank(message = "La identificación es obligatoria.")
    @Size(max = 50, message = "La identificación no puede superar 50 caracteres.")
    private String identificacion;

    @NotBlank(message = "El nombre del contacto es obligatorio.")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres.")
    private String nombre;

    @Size(max = 150, message = "La empresa no puede superar 150 caracteres.")
    private String empresa;

    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "Ingresa un correo electrónico válido.")
    @Size(max = 100, message = "El correo no puede superar 100 caracteres.")
    private String correo;

    @NotBlank(message = "El teléfono es obligatorio.")
    @Size(max = 30, message = "El teléfono no puede superar 30 caracteres.")
    @Pattern(regexp = "^[0-9+()\\-\\s]{7,30}$", message = "Ingresa un teléfono válido.")
    private String telefono;

    @Size(max = 255, message = "La dirección no puede superar 255 caracteres.")
    private String direccion;

    @Size(max = 50, message = "El segmento no puede superar 50 caracteres.")
    private String segmento;

    @Size(max = 100, message = "Las condiciones de pago no pueden superar 100 caracteres.")
    private String condicionesPago;

    @Size(max = 2000, message = "Las notas no pueden superar 2000 caracteres.")
    private String notas;

    @NotNull(message = "Indica el estado del cliente.")
    private Boolean activo = true;

}