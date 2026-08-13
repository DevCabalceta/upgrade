package com.upgrade.app.dto;

import jakarta.validation.constraints.*;

public class NuevoRolForm {
    @NotBlank @Size(max = 100) private String nombre;
    @NotBlank @Pattern(regexp = "[a-zA-Z0-9_]+", message = "Usa solamente letras, números y guiones bajos.") @Size(max = 50) private String identificador;
    @NotBlank @Size(max = 255) private String descripcion;
    private Long plantillaId;
    public String getNombre() { return nombre; } public void setNombre(String v) { nombre = v; }
    public String getIdentificador() { return identificador; } public void setIdentificador(String v) { identificador = v; }
    public String getDescripcion() { return descripcion; } public void setDescripcion(String v) { descripcion = v; }
    public Long getPlantillaId() { return plantillaId; } public void setPlantillaId(Long v) { plantillaId = v; }
}
