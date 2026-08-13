package com.upgrade.app.dto;

import jakarta.validation.constraints.*;

public class EditarRolForm {
    @NotNull private Long id;
    @NotBlank @Size(max = 100) private String nombre;
    @NotBlank @Size(max = 255) private String descripcion;
    public Long getId() { return id; } public void setId(Long v) { id = v; }
    public String getNombre() { return nombre; } public void setNombre(String v) { nombre = v; }
    public String getDescripcion() { return descripcion; } public void setDescripcion(String v) { descripcion = v; }
}
