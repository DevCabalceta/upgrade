package com.upgrade.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EliminarRolForm {
    @NotNull private Long id;
    @NotBlank private String confirmacion;
    public Long getId() { return id; } public void setId(Long v) { id = v; }
    public String getConfirmacion() { return confirmacion; } public void setConfirmacion(String v) { confirmacion = v; }
}
